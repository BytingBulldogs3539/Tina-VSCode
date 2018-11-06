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
import frc.robot.commands.ShooterCommand;

/**
 * Add your docs here.
 */
public class Shooter extends Subsystem
{
  // Put methods for controlling this subsystem
  // here. Call these from Commands.

  TalonSRX shooter1, shooter2;

  public Shooter()
  {
    shooter1 = new TalonSRX(RobotMap.shooter1);
    shooter2 = new TalonSRX(RobotMap.shooter2);
    setupMotorController(shooter1);
    setupMotorController(shooter2);
    shooter2.follow(shooter1);
  }

  private void setupMotorController(TalonSRX srx)
  {
    srx.configNominalOutputForward(0, 0);
    srx.configNominalOutputReverse(0, 0);
    srx.configPeakOutputForward(1, 0);
    srx.configPeakOutputReverse(-1, 0);
    srx.enableCurrentLimit(true);
    srx.configContinuousCurrentLimit(38, 0);
    srx.configPeakCurrentLimit(42, 0);
    srx.configPeakCurrentDuration(100, 0);
    srx.setInverted(true);
  }

  public void setMotorPower(double power)
  {
    shooter1.set(ControlMode.PercentOutput, power);
  }

  @Override
  public void initDefaultCommand()
  {
    // Set the default command for a subsystem here.
    setDefaultCommand(new ShooterCommand());
  }
}
