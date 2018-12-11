/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap
{
  // The drive talon numbers
  public static int fr = 4;
  public static int fl = 7;
  public static int br = 3;
  public static int bl = 6;
  // The intake talon numbers
  public static int intake1 = 5;
  // The shooter talon numbers
  public static int shooter1 = 1;
  public static int shooter2 = 2;
  // The wheel Circumference
  public static double wheelCir = 12.56;

}
