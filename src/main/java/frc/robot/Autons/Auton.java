/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.Autons;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.utilities.JsonParser;
import java.io.File;

import frc.robot.Robot;
import frc.robot.motionprofile.AutonDrivePathCommand;
import frc.robot.motionprofile.MotionProfileConstants;

public class Auton extends CommandGroup
{
        public Auton()
        {
                MotionProfileConstants.kGains_Distanc = Robot.arrayToGains(SmartDashboard.getNumberArray("Distanc",
                                Robot.gainsToArray(MotionProfileConstants.kGains_Distanc)));
                MotionProfileConstants.kGains_Turning = Robot.arrayToGains(SmartDashboard.getNumberArray("Turning",
                                Robot.gainsToArray(MotionProfileConstants.kGains_Turning)));
                MotionProfileConstants.kGains_Velocit = Robot.arrayToGains(SmartDashboard.getNumberArray("Velocit",
                                Robot.gainsToArray(MotionProfileConstants.kGains_Velocit)));
                MotionProfileConstants.kGains_MotProf = Robot.arrayToGains(SmartDashboard.getNumberArray("MotProf",
                                Robot.gainsToArray(MotionProfileConstants.kGains_MotProf)));
                Robot.smartInit();

                addSequential(new AutonDrivePathCommand(
                                JsonParser.RetrieveProfileData(new File("/home/lvuser/Motion_Profiles/AUSA.json")),
                                false));
        }
}
