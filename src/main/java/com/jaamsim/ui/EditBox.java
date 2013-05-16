/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2005-2013 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.jaamsim.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.jaamsim.input.InputAgent;
import com.jaamsim.input.InputGroup;
import com.jaamsim.input.Parser;
import com.jaamsim.math.Color4d;
import com.sandwell.JavaSimulation.ColourInput;
import com.sandwell.JavaSimulation.Entity;
import com.sandwell.JavaSimulation.Input;
import com.sandwell.JavaSimulation.InputErrorException;
import com.sandwell.JavaSimulation.Keyword;
import com.sandwell.JavaSimulation.ListInput;
import com.sandwell.JavaSimulation.StringListInput;
import com.sandwell.JavaSimulation3D.GUIFrame;

/**
 * Class to display information about model objects. <br>
 * displays the results of getInfo for an object
 */
public class EditBox extends FrameBox {

	private static EditBox myInstance;  // only one instance allowed to be open
	private static final int ROW_HEIGHT=20;
	private static final int VALUE_COLUMN=2;

	private Entity currentEntity;
	private final JTabbedPane jTabbedPane;

	private final TableCellRenderer columnRender = new EditBoxColumnRenderer();

	/**
	 * Widths of columns in the table of keywords as modified by the user in an edit
	 * session. When the EditBox is re-opened for the next edit (during the same session),
	 * the last modified column widths are used.
	 * <pre>
	 * userColWidth[ 0 ] = user modified width of column 0 (keywords column)
	 * userColWidth[ 1 ] = user modified width of column 1 (defaults units column)
	 * userColWidth[ 2 ] = user modified width of column 2 (values column)
	 */

	private EditBox() {

		super( "Input Editor" );

		setDefaultCloseOperation(FrameBox.DISPOSE_ON_CLOSE);

		// Set the preferred size of the panes
		jTabbedPane = new JTabbedPane();
		jTabbedPane.setPreferredSize( new Dimension( 700, 400 ) );
		jTabbedPane.addChangeListener(new TabListener());
		getContentPane().add(jTabbedPane);

		pack();
		setLocation(220, 710);
		setSize(530, 290);
	}

	public synchronized static EditBox getInstance() {
		if (myInstance == null)
			myInstance = new EditBox();

		return myInstance;
	}

	public static final String scanClassForInputDesc(Class<?> curClass, Object obj, Input<?> in) {
		for (Field f : curClass.getDeclaredFields()) {
			if (!Input.class.isAssignableFrom(f.getType())) {
				continue;
			}

			try {
				f.setAccessible(true);
				if (in != f.get(obj)) {
					continue;
				}

				Keyword key = f.getAnnotation(Keyword.class);
				if (key == null)
					return null;

				StringBuilder build = new StringBuilder();
				build.append("<HTML>");
				build.append("<b>Keyword:</b>  ");
				build.append(in.getKeyword());
				build.append("<BR>");
				build.append("<b>Category:</b> ");
				build.append(in.getCategory());
				build.append("<BR><BR>");
				build.append("<b>Description:</b> ");
				for (String line : key.desc().split("\n", 0)) {
					// Replace all <> for html parsing
					String tempLine = line.replaceAll("&", "&amp;");
					tempLine = tempLine.replaceAll("<", "&lt;");
					tempLine = tempLine.replaceAll(">", "&gt;");

					int len = 0;
					build.append("<BR>");
					// Break the line at 100-char boundaries
					for (String word : tempLine.split(" ", -1)) {
						build.append(word).append(" ");
						len += word.length() + 1;
						if (len > 100) {
							build.append("<BR>");
							len = 0;
						}
					}
				}
				if (key.example().length() > 0) {
					build.append("<BR><BR><b>Example:</b> ");
					for (String each : key.example().split("\n", 0))
						build.append("<BR>").append(each);
				}

				return build.toString();
			}
			catch (IllegalArgumentException e) {}
			catch (IllegalAccessException e) {}
			catch (SecurityException e) {}
		}
		return null;

	}

	public static final String getInputDesc(Entity ent, Input<?> in) {
		Class<?> current = ent.getClass();

		while (true) {
			String desc = scanClassForInputDesc(current, ent, in);
			if (desc != null) {
				return desc;
			}

			current = current.getSuperclass();
			if (current == Object.class)
				break;
		}

		// Try all the InputGroups in this class
		for (InputGroup grp : ent.getInputGroups()) {
			Class<? extends InputGroup> grpClass = grp.getClass();

			String desc = scanClassForInputDesc(grpClass, grp, in);
			if (desc != null) {
				return desc;
			}
		}
		return null;
	}

	Entity getCurrentEntity() {
		return currentEntity;
	}

	@Override
	public void setEntity(Entity entity) {
		if(entity != null && entity.testFlag(Entity.FLAG_GENERATED))
			entity = null;

		if(currentEntity == entity)
			return;

		jTabbedPane.removeAll();
		currentEntity = entity;

		// no entity is selected
		if(currentEntity == null) {
			setTitle("Input Editor");
			return;
		}

		for (CategoryInputs each : getInputs(currentEntity)) {
			EditTableModel mod = new EditTableModel(each);
			JTable propTable = new EditTable(mod, columnRender);
			JScrollPane jScrollPane = new JScrollPane(propTable);
			jScrollPane.getVerticalScrollBar().setUnitIncrement(ROW_HEIGHT);
			jScrollPane.setColumnHeaderView( propTable.getTableHeader());

			jTabbedPane.addTab(each.category, null, jScrollPane, null);
		}

		if (jTabbedPane.getTabCount() > 0)
			jTabbedPane.setSelectedIndex(0);

		setTitle(String.format("Input Editor - %s", currentEntity.getInputName()));
	}

	@Override
	public void updateValues(double simTime) {
		if(currentEntity == null)
			return;

		JTable propTable = (JTable)(((JScrollPane)jTabbedPane.getSelectedComponent()).getViewport().getComponent(0));
		((EditTableModel)propTable.getModel()).fireTableDataChanged();
	}

	private synchronized static void killInstance() {
		myInstance = null;
	}

	@Override
	public void dispose() {
		killInstance();
		super.dispose();
	}

	public static class StringEditor extends CellEditor implements TableCellEditor {
		private final JTextField text;

		public StringEditor(JTable table) {
			super(table);
			text = new JTextField();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			input = (Input<?>)value;
			text.setText( input.getValueString() );
			return text;
		}

		@Override
		public String getValue() {
			return text.getText();
		}
	}

	public static class ColorEditor extends CellEditor
	implements TableCellEditor, ActionListener {

		private final JPanel jPanel;
		private final JTextField text;
		private final JButton colorButton;
		private JColorChooser colorChooser;
		private JDialog dialog;

		public ColorEditor(JTable table) {
			super(table);

			jPanel = new JPanel(new BorderLayout());

			text = new JTextField();
			jPanel.add(text, BorderLayout.WEST);

			colorButton = new JButton(new ImageIcon(
				GUIFrame.class.getResource("/resources/images/dropdown.png")));
			colorButton.addActionListener(this);
			colorButton.setActionCommand("button");
			colorButton.setContentAreaFilled(false);
			jPanel.add(colorButton, BorderLayout.EAST);
		}

		@Override
		public String getValue() {
			return text.getText();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if("button".equals(e.getActionCommand())) {
				if(colorChooser == null || dialog == null) {
					colorChooser = new JColorChooser();
					dialog = JColorChooser.createDialog(jPanel,
							"Pick a Color",
							true,  //modal
							colorChooser,
							this,  //OK button listener
							null); //no CANCEL button listener
					dialog.setIconImage(GUIFrame.getWindowIcon());
					dialog.setAlwaysOnTop(true);
				}

				Color4d col = ((ColourInput)input).getValue();
				colorChooser.setColor(new Color((float)col.r, (float)col.g, (float)col.b));
				dialog.setLocationRelativeTo((Component)e.getSource());
				dialog.setVisible(true);

				// Apply editing
				stopCellEditing();

				// Focus the cell
				propTable.requestFocusInWindow();
			}
			else {
				Color color = colorChooser.getColor();
				text.setText( String.format("%d %d %d", color.getRed(),
						color.getGreen(), color.getBlue() ) );
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			// set the value
			input = (ColourInput)value;
			text.setText( input.getValueString() );

			// right size for jPanel and its components
			Dimension dim = new Dimension(
				  table.getColumnModel().getColumn( VALUE_COLUMN ).getWidth() -
				  table.getColumnModel().getColumnMargin(),
				  table.getRowHeight());
			jPanel.setPreferredSize(dim);
			dim = new Dimension(dim.width - (dim.height), dim.height);
			text.setPreferredSize(dim);
			dim = new Dimension(dim.height, dim.height);
			colorButton.setPreferredSize(dim);

			return jPanel;
		}
	}

	public static class DropDownMenuEditor extends CellEditor
	implements TableCellEditor, ActionListener {

		private final JComboBox dropDown;

		public DropDownMenuEditor(JTable table, ArrayList<String> aList) {
			super(table);

			dropDown = new JComboBox();
			dropDown.setEditable(true);
			DefaultComboBoxModel model = (DefaultComboBoxModel) dropDown.getModel();

			// Populate the items in the combo box
			for(String each: aList) {

				// Space inside the font name
				if( each.contains(" ") )
					each = String.format("'%s'", each);
				model.addElement(each);
			}

			dropDown.setActionCommand("comboBoxChanged");
			dropDown.addActionListener(this); // Now it is safe to listen
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			// If combo box is edited, this method invokes twice
			if(e.getActionCommand().equals("comboBoxChanged"))
				stopCellEditing();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			input = (Input<?>)value;
			String text = input.getValueString();

			dropDown.setSelectedItem(text);
			return dropDown;
		}

		@Override
		public String getValue() {
			return (String)dropDown.getSelectedItem();
		}
	}

	public static class ListEditor extends CellEditor
	implements TableCellEditor, ActionListener {

		private final JPanel jPanel;
		private final JTextField text;
		private final JButton listButton;
		private JDialog dialog;
		private JScrollPane jScroll;
		private JList list;

		private ArrayList<String> tokens;
		private DefaultListModel listModel;
		private CheckBoxMouseAdapter checkBoxMouseAdapter;
		private int i;
		private boolean caseSensitive;

		public ListEditor(JTable table) {
			super(table);

			jPanel = new JPanel(new BorderLayout());

			text = new JTextField();
			jPanel.add(text, BorderLayout.WEST);

			listButton = new JButton(new ImageIcon(
				GUIFrame.class.getResource("/resources/images/dropdown.png")));
			listButton.addActionListener(this);
			listButton.setActionCommand("button");
			listButton.setContentAreaFilled(false);
			jPanel.add(listButton, BorderLayout.EAST);
			caseSensitive = true;
		}

		@Override
		public String getValue() {
			return text.getText();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			// OK button
			if("OK".equals(e.getActionCommand())) {
				dialog.setVisible(false);
				String value = "";
				for(int i = 0; i < list.getModel().getSize(); i++) {
					if(!((JCheckBox)list.getModel().getElementAt(i)).isSelected())
						continue;
					value = String.format("%s%s ", value,
						((JCheckBox)list.getModel().getElementAt(i)).getText());
				}
				text.setText(String.format(" %s", value));
			}

			if(! "button".equals(e.getActionCommand())) {
				return;
			}

			if(dialog == null) {
				dialog = new JDialog(EditBox.getInstance(), "Select items",
						true); // model
				dialog.setSize(190, 300);
				jScroll = new JScrollPane(list);
				dialog.getContentPane().add(jScroll); // top of the JDialog
				JButton okButton = new JButton("Ok");
				okButton.setActionCommand("OK");
				okButton.addActionListener(this);
				dialog.getContentPane().add("South", okButton);
				dialog.setIconImage(GUIFrame.getWindowIcon());
				dialog.setAlwaysOnTop(true);
				tokens = new ArrayList<String>();
			}

			// break the value into single options
			tokens.clear();
			Parser.tokenize(tokens, text.getText());
			if( !caseSensitive ) {
				for(i = 0; i < tokens.size(); i++ ) {
					tokens.set(i, tokens.get(i).toUpperCase() );
				}
			}

			// checkmark according to the value input
			for(i = 0; i < list.getModel().getSize(); i++) {
				JCheckBox box = (JCheckBox)list.getModel().getElementAt(i);
				box.setSelected(tokens.contains(box.getText()));
			}
			dialog.setLocationRelativeTo((Component)e.getSource());
			dialog.setVisible(true);

			// Apply editing
			stopCellEditing();

			// Focus the cell
			propTable.requestFocusInWindow();
		}

		// Set the items in the list
		public void setListData(ArrayList<String> aList) {
			if(list == null) {
				listModel = new DefaultListModel();
				list = new JList(listModel);

				// render items as JCheckBox and make clicking work for them
				list.setCellRenderer( new ListRenderer() );
				checkBoxMouseAdapter = new CheckBoxMouseAdapter();
				list.addMouseListener(checkBoxMouseAdapter);
			}
			listModel.clear();
			for(String each: aList) {
				JCheckBox checkBox = new JCheckBox(each);
				listModel.addElement(checkBox);
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			input = (Input<?>)value;
			text.setText( input.getValueString() );

			// right size for jPanel and its components in the cell
			Dimension dim = new Dimension(
					table.getColumnModel().getColumn( VALUE_COLUMN ).getWidth() -
					table.getColumnModel().getColumnMargin(),
					table.getRowHeight());
			jPanel.setPreferredSize(dim);
			dim = new Dimension(dim.width - (dim.height), dim.height);
			text.setPreferredSize(dim);
			dim = new Dimension(dim.height, dim.height);
			listButton.setPreferredSize(dim);

			return jPanel;
		}

		public void setCaseSensitive(boolean bool) {
			caseSensitive = bool;
		}
	}

	/*
	 * renderer for the JList so it shows its items as JCheckBoxes
	 */
	public static class ListRenderer implements ListCellRenderer {
		private JCheckBox checkBox;
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			checkBox = (JCheckBox)value;
			if (isSelected) {
				checkBox.setBackground(list.getSelectionBackground());
				checkBox.setForeground(list.getSelectionForeground());
			}
			else {
				checkBox.setBackground(list.getBackground());
				checkBox.setForeground(list.getForeground());
			}
			return checkBox;
		}
	}

	/*
	 * pressing mouse in the JList should select/unselect JCheckBox
	 */
	public static class CheckBoxMouseAdapter extends MouseAdapter {
		private int i;
		private Object obj;
		@Override
		public void mousePressed(MouseEvent e) {
			i = ((JList)e.getSource()).locationToIndex(e.getPoint());
			if(i == -1)
				return;

			obj = ((JList)e.getSource()).getModel().getElementAt(i);
			if (obj instanceof JCheckBox) {
				JCheckBox checkbox = (JCheckBox) obj;
				checkbox.setSelected(!checkbox.isSelected());
				((JList)e.getSource()).repaint();
			}
		}
	}

	public static class CellEditor extends AbstractCellEditor {
		protected final JTable propTable;
		protected Input<?> input;

		public CellEditor(JTable table) {
			propTable = table;
			this.addCellEditorListener(new CellListener());
		}

		@Override
		public Object getCellEditorValue() {
			return input;
		}

		public String getValue() {
			return "";
		}
	}

	public static class CellListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent evt) {}

		@Override
		public void editingStopped(ChangeEvent evt) {

			CellEditor editor = (CellEditor)evt.getSource();
			Input<?> in = (Input<?>)editor.getCellEditorValue();

			// The value has not changed
			if ( in.getValueString().equals(editor.getValue()) )
				return;

			try {

				String str = editor.getValue();

				if( str.isEmpty() ) {

					// Back to default value
					str = in.getDefaultString();
				}
				InputAgent.processEntity_Keyword_Value(EditBox.getInstance().getCurrentEntity(), in, str);
			} catch (InputErrorException exep) {
				JOptionPane.showMessageDialog(EditBox.getInstance(),
				   String.format( "%s; value will be cleared", exep.getMessage() ),
				   "Input Error", JOptionPane.ERROR_MESSAGE);

				FrameBox.valueUpdate();
				return;

			}

		}
	}

private static class TabListener implements ChangeListener {
	@Override
	public void stateChanged(ChangeEvent e) {
		FrameBox.valueUpdate();
	}
}

private static class CategoryInputs {
	final String category;
	final ArrayList<Input<?>> inputs;

	CategoryInputs(String cat, ArrayList<Input<?>> ins) {
		category = cat;
		inputs = ins;
	}
}

	private static ArrayList<CategoryInputs> getInputs(Entity ent) {

		String cat = "";
		ArrayList<CategoryInputs> catInputsList = new ArrayList<CategoryInputs>(0);
		ArrayList<Input<?>> inputs = new ArrayList<Input<?>>();

		// assuming that editable inputs of the same category are adjacent
		for (Input<?> in : ent.getEditableInputs()) {
			if (in.getHidden() || in.isLocked())
				continue;

			// the first time entering the loop
			if (cat.isEmpty())
				cat = in.getCategory();

			// new category (the previous category inputs is done so add it to the list)
			if ( !cat.equals(in.getCategory()) ) {
				add(catInputsList, inputs, cat);
				inputs = new ArrayList<Input<?>>();
				cat = in.getCategory();
			}
			inputs.add(in);
		}

		if (inputs.size() != 0) {
			add(catInputsList, inputs, cat);
		}

		return catInputsList;
	}

	private static void add(ArrayList<CategoryInputs> list, ArrayList<Input<?>> inputs, String cat) {
		CategoryInputs catInputs = new CategoryInputs(cat, inputs);

		// Ensure key inputs are always the first tab
		if (catInputs.category.equals("Key Inputs"))
			list.add(0, catInputs);
		else
			list.add(catInputs);
	}

public static class EditTable extends JTable {
	private ColorEditor colorEditor;
	private ListEditor listEditor;

	@Override
	public boolean isCellEditable( int row, int column ) {
		return ( column == VALUE_COLUMN ); // Only Value column is editable
	}

	public EditTable(EditTableModel mod, TableCellRenderer colRender) {
		super(mod);

		this.setRowHeight(ROW_HEIGHT);
		this.setRowSelectionAllowed(false);
		this.getTableHeader().setFont(boldFont);
		this.getTableHeader().setReorderingAllowed(false);

		TableColumn col;

		// Set keyword table column headers
		col = this.getColumnModel().getColumn(0);
		col.setWidth(150);
		col.setCellRenderer(colRender);

		col = this.getColumnModel().getColumn(1);
		col.setWidth(150);
		col.setCellRenderer(colRender);

		col = this.getColumnModel().getColumn(2);
		col.setWidth(150);
		col.setCellRenderer(colRender);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int col) {
		Input<?> in = (Input<?>)this.getValueAt(row, col);

		// 1) Colour input
		if(in instanceof ColourInput) {
			if(colorEditor == null) {
				colorEditor = new ColorEditor(this);
			}
			return colorEditor;
		}

		// 2) Normal text
		ArrayList<String> array = in.getValidOptions();
		if(array == null) {
			StringEditor stringEditor = new StringEditor(this);
			return stringEditor;
		}

		// 3) Multiple selections from a List
		if(in instanceof ListInput) {
			if(listEditor == null) {
				listEditor = new ListEditor(this);
				if(in instanceof StringListInput) {
					listEditor.setCaseSensitive(
							((StringListInput)(in)).getCaseSensitive() );
				}
			}
			listEditor.setListData(array);
			return listEditor;
		}

		// 4) Single selection from a drop down box
		return new DropDownMenuEditor(this, array);
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);

		// not necessary because reordering of tabs is not allowed
		colIndex = convertColumnIndexToModel(colIndex);

		// Only the keyword column has tooltip
		if (colIndex != 0)
			return null;

		TableModel model = getModel();
		Input<?> in = (Input<?>)model.getValueAt(rowIndex, 0);
		return EditBox.getInputDesc(EditBox.getInstance().getCurrentEntity(), in);
	}

	@Override
	public void doLayout() {
		FrameBox.fitTableToLastColumn(this);
	}
}


private static class EditTableModel extends AbstractTableModel {
	CategoryInputs inputs;

	EditTableModel(CategoryInputs ci) {
		inputs = ci;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return "Keyword";
		case 1: return "Default";
		case 2:return "Value";
		}

		return "Unknown";
	}

	@Override
	public int getRowCount() {
		return inputs.inputs.size();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return (column == VALUE_COLUMN); // Only Value column is editable
	}

	@Override
	public Object getValueAt(int row, int col) {
		return inputs.inputs.get(row);
	}
}

}