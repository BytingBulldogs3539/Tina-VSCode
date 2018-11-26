/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.motionprofile;

import edu.wpi.first.wpilibj.command.Command;
import frc.robot.Robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FollowerType;

public class AutonDrivePathCommand extends Command
{

  boolean bMoveForward;
  MotionProfile motion;
  double[][] Profile;

  public AutonDrivePathCommand(double[][] Profile, boolean forward)
  {
    this.Profile = Profile;
    requires(Robot.driveTrain);
    bMoveForward = forward;
    System.out.println("ENNABLED");
  }

  @Override
  protected void initialize()
  {
    motion = new MotionProfile(Robot.driveTrain.fr, Profile);
    motion.reset();
    Robot.driveTrain.initMotionProfile();
    motion.start(bMoveForward);
  }

  @Override
  protected void execute()
  {
    Robot.driveTrain.fr.set(ControlMode.MotionProfileArc, motion.getSetValue().value);
    Robot.driveTrain.fl.follow(Robot.driveTrain.fr, FollowerType.AuxOutput1);

    /*
     * call this periodically, and catch the output. Only apply it if user wants to
     * run MP.
     */
    motion.control();
  }

  @Override
  protected boolean isFinished()
  {
    return motion.finished;
  }

  @Override
  protected void end()
  {
    Robot.driveTrain.neutralMotors();
  }

  @Override
  protected void interrupted()
  {
    end();
  }
}
