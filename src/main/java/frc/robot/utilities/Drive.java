/**
 * Created by Cameron Coesens in the 2018 FIRST Power Up Season.
 * This allows FIRST teams to use the old style of controlling the
 *  talonsrxs as a drivetrain with arcadedrive this class assumes that the
 *  right and left values both require positive values to move the robot forward.
 */

package frc.robot.utilities;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public final class Drive
{
	// TODO - Add mecanum drive, holonomic drive

	TalonSRX talon1, talon2, talon3, talon4, talon5, talon6;
	DriveMode driveMode;

	private static enum DriveMode
	{
		TWO, FOUR, SIX;
	}

	/**
	 * The constructor for the drive method allowing the user to control the right
	 * and left side of robot with a total of <b>2</b> motors.
	 *
	 * @param right
	 *                  the motor controller that controls the right motor.
	 * @param left
	 *                  the motor controller that controls the left motor.
	 */
	public Drive(TalonSRX right, TalonSRX left)
	{
		driveMode = DriveMode.TWO;

		talon1 = right;
		talon2 = left;
	}

	/**
	 * The constructor for the drive method allowing the user to control the right
	 * and left side of robot with a total of <b>4</b> motors.
	 *
	 * @param rightFront
	 *                       the motor controller that controls the right front
	 *                       motor.
	 * 
	 * @param rightBack
	 *                       the motor controller that controls the right back
	 *                       motor.
	 * @param leftFront
	 *                       the motor controller that controls the left front
	 *                       motor.
	 * @param leftBack
	 *                       the motor controller that controls the left back motor.
	 */

	public Drive(TalonSRX rightFront, TalonSRX rightBack, TalonSRX leftFront, TalonSRX leftBack)
	{
		driveMode = DriveMode.FOUR;
		talon1 = rightFront;
		talon2 = rightBack;
		talon3 = leftFront;
		talon4 = leftBack;
	}

	/**
	 * The constructor for the drive method allowing the user to control the right
	 * and left side of robot with a total of <b>6</b> motors.
	 *
	 * @param rightFront
	 *                        the motor controller that controls the right front
	 *                        motor.
	 * @param rightMiddle
	 *                        the motor controller that controls the right middle
	 *                        motor.
	 * @param rightBack
	 *                        the motor controller that controls the right back
	 *                        motor.
	 * @param leftFront
	 *                        the motor controller that controls the left front
	 *                        motor.
	 * @param leftMiddle
	 *                        the motor controller that controls the left middle
	 *                        motor.
	 * @param leftBack
	 *                        the motor controller that controls the left back
	 *                        motor.
	 */

	public Drive(TalonSRX rightFront, TalonSRX rightMiddle, TalonSRX rightBack, TalonSRX leftFront, TalonSRX leftMiddle,
			TalonSRX leftBack)
	{
		driveMode = DriveMode.SIX;
		talon1 = rightFront;
		talon2 = rightMiddle;
		talon3 = rightBack;
		talon4 = leftFront;
		talon5 = leftMiddle;
		talon6 = leftBack;
	}

	/**
	 * Allows the user to give this method two inputs from a controller or two value
	 * inorder to control turn speed and forward and reverse speed.
	 *
	 * @param moveValue
	 *                        the joystick value or value that will control the
	 *                        forward and reverse speed of the robot.
	 * @param rotateValue
	 *                        the joystick value or value that will control the
	 *                        rotate speed of the robot.
	 */

	public void driveArcade(double moveValue, double rotateValue)
	{
		double leftMotorSpeed;
		double rightMotorSpeed;

		moveValue = limitValue(moveValue);
		rotateValue = limitValue(rotateValue);

		if (moveValue >= 0.0)
		{
			if (rotateValue >= 0.0)
			{
				leftMotorSpeed = moveValue - rotateValue;
				rightMotorSpeed = Math.max(moveValue, rotateValue);
			}
			else
			{
				leftMotorSpeed = Math.max(moveValue, -rotateValue);
				rightMotorSpeed = moveValue + rotateValue;
			}
		}
		else
		{
			if (rotateValue >= 0.0)
			{
				leftMotorSpeed = -Math.max(-moveValue, rotateValue);
				rightMotorSpeed = moveValue + rotateValue;
			}
			else
			{
				leftMotorSpeed = moveValue - rotateValue;
				rightMotorSpeed = -Math.max(-moveValue, -rotateValue);
			}
		}
		setMotorOutputs(-rightMotorSpeed, -leftMotorSpeed);
	}

	/**
	 * Allows the user to set the motor speed of both the right and left side at the
	 * same time it will also set all of the motors on that side of the drivetrain
	 * to that speed.
	 *
	 * @param right
	 *                  the right motor speed must be between -1 and 1
	 * @param left
	 *                  the left motor speed must be between -1 and
	 */

	public void setMotorOutputs(double right, double left)
	{
		if (driveMode == DriveMode.TWO)
		{
			talon1.set(ControlMode.PercentOutput, right);
			talon2.set(ControlMode.PercentOutput, left);
		}
		if (driveMode == DriveMode.FOUR)
		{
			talon1.set(ControlMode.PercentOutput, right);
			talon2.set(ControlMode.PercentOutput, right);
			talon3.set(ControlMode.PercentOutput, left);
			talon4.set(ControlMode.PercentOutput, left);
		}
		if (driveMode == DriveMode.SIX)
		{
			talon1.set(ControlMode.PercentOutput, right);
			talon2.set(ControlMode.PercentOutput, right);
			talon3.set(ControlMode.PercentOutput, right);
			talon4.set(ControlMode.PercentOutput, left);
			talon5.set(ControlMode.PercentOutput, left);
			talon6.set(ControlMode.PercentOutput, left);
		}
	}

	/**
	 * Allows the user to give this method a input from a controller and limit it to
	 * only be between one and negative 1 it does this by if the value is bigger
	 * than 1 or less than -1 it will just make it 1 or -1.
	 *
	 * @param value
	 *                  the joystick value or value to be limited
	 * @return the limited value always between 1 and negative 1
	 */
	public double limitValue(double value)
	{
		if (value > 1.0)
			value = 1.0;
		if (value < -1.0)
			value = -1.0;
		return value;
	}
}
