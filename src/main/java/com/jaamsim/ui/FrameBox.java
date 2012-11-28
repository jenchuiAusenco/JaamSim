/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2011 Ausenco Engineering Canada Inc.
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

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jaamsim.controllers.RenderManager;
import com.sandwell.JavaSimulation.Entity;
import com.sandwell.JavaSimulation3D.GUIFrame;

public class FrameBox extends JFrame {

	private static final ArrayList<FrameBox> allInstances;

	private static final FrameBoxUpdater updater;
	private static final FrameBoxValueUpdater valueUpdater;

	protected static final Color TABLE_SELECT = new Color(255, 250, 180);

	protected static final Font boldFont;
	protected static final TableCellRenderer colRenderer;

	static {
		allInstances = new ArrayList<FrameBox>();

		boldFont = UIManager.getDefaults().getFont("TabbedPane.font").deriveFont(Font.BOLD);

		updater = new FrameBoxUpdater();
		valueUpdater = new FrameBoxValueUpdater();

		colRenderer = new DefaultCellRenderer();
	}

	public FrameBox(String title) {
		super(title);
		setIconImage(GUIFrame.getWindowIcon());
		allInstances.add(this);
	}

	public static ArrayList<FrameBox> getAllFB() {
		return allInstances;
	}

	public void dispose() {
		allInstances.remove(this);
		super.dispose();
	}

	public static final void setSelectedEntity(Entity ent) {
		updater.scheduleUpdate(ent);

		if (!RenderManager.isGood()) { return; }

		RenderManager.inst().setSelection(ent);
	}

	public static final void valueUpdate() {
		valueUpdater.scheduleUpdate();
		if (RenderManager.isGood()) {
			RenderManager.inst().queueRedraw();
		}
	}

	public void setEntity(Entity ent) {}
	public void updateValues() {}

	private static class FrameBoxUpdater implements Runnable {
		private boolean scheduled;
		private Entity entity;

		FrameBoxUpdater() {
			scheduled = false;
		}

		public void scheduleUpdate(Entity ent) {
			synchronized (this) {
				entity = ent;
				if (!scheduled)
					SwingUtilities.invokeLater(this);

				scheduled = true;
			}
		}

		public void run() {
			Entity selectedEnt;
			synchronized (this) {
				selectedEnt = entity;
				entity = null;
				scheduled = false;
			}

			for (FrameBox each : allInstances) {
				each.setEntity(selectedEnt);
			}
		}
	}

	private static class FrameBoxValueUpdater implements Runnable {
		private boolean scheduled;

		FrameBoxValueUpdater() {
			scheduled = false;
		}

		public void scheduleUpdate() {
			synchronized (this) {
				if (!scheduled)
					SwingUtilities.invokeLater(this);

				scheduled = true;
			}
		}

		public void run() {
			synchronized (this) {
				scheduled = false;
			}

			for (FrameBox each : allInstances) {
				each.updateValues();
			}
		}
	}
}