/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code
*/
/* must be accompanied by the FIRST BSD license file in the root directory of
*/
/* the project. */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.RobotMap;

public class Intake extends Subsystem
{

    TalonSRX intake1;

    public Intake()
    {
        intake1 = new TalonSRX(RobotMap.intake1);

        setupMotorController(intake1);

    }

    /**
     * Used to configure the motor controller normally when the robot starts.
     * 
     * @param srx
     *                the talon that you would like to configure.
     */
    private void setupMotorController(TalonSRX srx)
    {
        srx.configNominalOutputForward(0, 0);
        srx.configNominalOutputReverse(0, 0);
        srx.configPeakOutputForward(1, 0);
        srx.configPeakOutputReverse(-1, 0);
        srx.enableCurrentLimit(true);
        srx.configContinuousCurrentLimit(28, 0);
        srx.configPeakCurrentLimit(32, 0);
        srx.configPeakCurrentDuration(100, 0);
    }

    /**
     * Used to set the Intake speed.
     * 
     * @param output
     *                   a value between -1.0 and 1.0 that used to set the speed of
     *                   the motor.
     */
    public void setIntakeSpeed(double PercentOutput)
    {
        intake1.set(ControlMode.PercentOutput, PercentOutput);
    }

    @Override
    public void initDefaultCommand()
    {

    }
}
