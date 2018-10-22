/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.RobotMap;

/**
 * Add your docs here..
 */
public class Intake extends Subsystem
{

  TalonSRX intake1, intake2;

  public Intake()
  {
    intake1 = new TalonSRX(RobotMap.intake1);
    intake2 = new TalonSRX(RobotMap.intake2);

    setupMotorController(intake1);
    setupMotorController(intake2);

    intake2.follow(intake1);
  }

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

  public void setIntakeSpeed(double PercentOutput)
  {
    intake1.set(ControlMode.PercentOutput, PercentOutput);
  }

  @Override
  public void initDefaultCommand()
  {

  }
}
