Define Palette { 'Calculation Objects' }

Define ObjectType {
                    Controller
					WeightedSum
					Polynomial
					Integrator
					Differentiator
					PIDController
					Lag
					MovingAverage
					Sensor
					SineWave
					SquareWave
					ConstantBoolean
					AndOperator
					OrOperator
					NotOperator
                    }

Controller              JavaClass { com.jaamsim.CalculationObjects.Controller          } Palette { 'Calculation Objects' }
WeightedSum             JavaClass { com.jaamsim.CalculationObjects.WeightedSum         } Palette { 'Calculation Objects' }
Polynomial              JavaClass { com.jaamsim.CalculationObjects.Polynomial          } Palette { 'Calculation Objects' }
Integrator              JavaClass { com.jaamsim.CalculationObjects.Integrator          } Palette { 'Calculation Objects' }
Differentiator          JavaClass { com.jaamsim.CalculationObjects.Differentiator      } Palette { 'Calculation Objects' }
PIDController           JavaClass { com.jaamsim.CalculationObjects.PIDController       } Palette { 'Calculation Objects' }
Lag                     JavaClass { com.jaamsim.CalculationObjects.Lag                 } Palette { 'Calculation Objects' }
MovingAverage           JavaClass { com.jaamsim.CalculationObjects.MovingAverage       } Palette { 'Calculation Objects' }
Sensor                  JavaClass { com.jaamsim.CalculationObjects.Sensor              } Palette { 'Calculation Objects' }
SineWave                JavaClass { com.jaamsim.CalculationObjects.SineWave            } Palette { 'Calculation Objects' }
SquareWave              JavaClass { com.jaamsim.CalculationObjects.SquareWave          } Palette { 'Calculation Objects' }
ConstantBoolean         JavaClass { com.jaamsim.CalculationObjects.ConstantBoolean     } Palette { 'Calculation Objects' }
AndOperator             JavaClass { com.jaamsim.CalculationObjects.AndOperator         } Palette { 'Calculation Objects' }
OrOperator              JavaClass { com.jaamsim.CalculationObjects.OrOperator          } Palette { 'Calculation Objects' }
NotOperator             JavaClass { com.jaamsim.CalculationObjects.NotOperator         } Palette { 'Calculation Objects' }

Define	ImageModel {
			ControllerIconModel
			WeightedSumIconModel
			PolynomialIconModel
			IntegratorIconModel
			DifferentiatorIconModel
			PIDControllerIconModel
			LagIconModel
			MovingAverageIconModel
			SensorIconModel
			SineWaveIconModel
			SquareWaveIconModel
			ConstantBooleanIconModel
			AndOperatorIconModel
			OrOperatorIconModel
			NotOperatorIconModel
		}

ControllerIconModel      ImageFile { '<res>/images/Controller-256.png' }      Transparent { TRUE }
WeightedSumIconModel     ImageFile { '<res>/images/WeightedSum-256.png' }     Transparent { TRUE }
PolynomialIconModel      ImageFile { '<res>/images/Polynomial-256.png' }      Transparent { TRUE }
IntegratorIconModel      ImageFile { '<res>/images/Integrator-256.png' }      Transparent { TRUE }
DifferentiatorIconModel  ImageFile { '<res>/images/Differentiator-256.png' }  Transparent { TRUE }
PIDControllerIconModel   ImageFile { '<res>/images/PID-256.png' }             Transparent { TRUE }
LagIconModel             ImageFile { '<res>/images/LAG-256.png' }             Transparent { TRUE }
MovingAverageIconModel   ImageFile { '<res>/images/AVG-256.png' }             Transparent { TRUE }
SensorIconModel          ImageFile { '<res>/images/Sensor-256.png' }          Transparent { TRUE }
SineWaveIconModel        ImageFile { '<res>/images/SineWave-256.png' }        Transparent { TRUE }
SquareWaveIconModel      ImageFile { '<res>/images/SquareWave-256.png' }      Transparent { TRUE }
ConstantBooleanIconModel ImageFile { '<res>/images/ConstantValue-256.png' }   Transparent { TRUE }
AndOperatorIconModel     ImageFile { '<res>/images/AND-256.png' }             Transparent { TRUE }
OrOperatorIconModel      ImageFile { '<res>/images/OR-256.png' }              Transparent { TRUE }
NotOperatorIconModel     ImageFile { '<res>/images/NOT-256.png' }             Transparent { TRUE }

Controller              DefaultDisplayModel 	{ ControllerIconModel }
WeightedSum             DefaultDisplayModel 	{ WeightedSumIconModel }
Polynomial              DefaultDisplayModel 	{ PolynomialIconModel }
Integrator              DefaultDisplayModel 	{ IntegratorIconModel }
Differentiator          DefaultDisplayModel 	{ DifferentiatorIconModel }
PIDController           DefaultDisplayModel 	{ PIDControllerIconModel }
Lag                     DefaultDisplayModel 	{ LagIconModel }
MovingAverage           DefaultDisplayModel 	{ MovingAverageIconModel }
Sensor                  DefaultDisplayModel 	{ SensorIconModel }
SineWave                DefaultDisplayModel 	{ SineWaveIconModel }
SquareWave              DefaultDisplayModel 	{ SquareWaveIconModel }
ConstantBoolean         DefaultDisplayModel 	{ ConstantBooleanIconModel }
AndOperator             DefaultDisplayModel 	{ AndOperatorIconModel }
OrOperator              DefaultDisplayModel 	{ OrOperatorIconModel }
NotOperator             DefaultDisplayModel 	{ NotOperatorIconModel }

Controller       Description { 'Signals the updating of each component in the specified sequence.' }
WeightedSum      Description { 'Calculates the weighted sum of the input values.' }
Polynomial       Description { 'Evaluates a polynomial function of the input value.' }
Integrator       Description { 'Integrates the input value over time.' }
Differentiator   Description { 'Differentiates the input value over time.' }
PIDController    Description { 'Proportional-Integral-Differential controller.' }
Lag              Description { 'Calculates the LAG operation for the input value.' }
MovingAverage    Description { 'Calculates a moving average of the input value over a specified range of time.' }
Sensor           Description { 'Allows the numerical output value for an entity to be used as an input to a calculation.' }
SineWave         Description { 'Generates a sinusoidal wave.' }
SquareWave       Description { 'Generates a square wave.' }
ConstantBoolean  Description { 'Provides a fixed Boolean value (TRUE or FALSE).' }
AndOperator      Description { 'Calculates the AND function on the Boolean inputs.' }
OrOperator       Description { 'Calculates the OR function on the Boolean inputs.' }
NotOperator      Description { 'Calculates the NOT function on a Boolean input.' }
