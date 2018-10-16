/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import frc.robot.RobotMap;
import frc.robot.commands.*;
import edu.wpi.first.wpilibj.command.Subsystem;

import frc.robot.utilities.Drive;

/**
 * Add your docs here.
 */
public class DriveTrain extends Subsystem {
  // Put methods for controlling this subsystem
  // here. Call these from Commands.

  public TalonSRX fr, fl, br, bl;
  public Drive drive;

  public DriveTrain() {
    fr = new TalonSRX(RobotMap.fr);
    fl = new TalonSRX(RobotMap.fl);
    br = new TalonSRX(RobotMap.br);
    bl = new TalonSRX(RobotMap.bl);

    setupMotorController(fr);
    setupMotorController(fl);
    setupMotorController(br);
    setupMotorController(bl);

    br.follow(fr);
    bl.follow(fl);

    fl.setInverted(true);
    bl.setInverted(true);
    drive = new Drive(fr, fl);
    drive.driveArcade(0, 0);
  }

  private void setupMotorController(TalonSRX srx) {
    srx.configNominalOutputForward(0, 0);
    srx.configNominalOutputReverse(0, 0);
    srx.configPeakOutputForward(1, 0);
    srx.configPeakOutputReverse(-1, 0);
    // srx.enableCurrentLimit(true);
    // srx.configContinuousCurrentLimit(37, 0);
    // srx.configPeakCurrentLimit(42, 0);
    // srx.configPeakCurrentDuration(100, 0);
  }

  public void arcadeDrive(double speed, double turn) {
    drive.driveArcade(speed, turn);
  }

  @Override
  public void initDefaultCommand() {
    setDefaultCommand(new DriveCommand());
  }
}
