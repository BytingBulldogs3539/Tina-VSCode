/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.RemoteFeedbackDevice;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.ParamEnum;

import frc.robot.RobotMap;
import frc.robot.commands.*;
import frc.robot.motionprofile.MotionProfile;
import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.motionprofile.*;

import frc.robot.utilities.Drive;

public class DriveTrain extends Subsystem {

  public TalonSRX fr, fl, br, bl;
  public Drive drive;

  public PigeonIMU _imu;

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
    srx.enableCurrentLimit(true);
    srx.configContinuousCurrentLimit(37, 0);
    srx.configPeakCurrentLimit(42, 0);
    srx.configPeakCurrentDuration(100, 0);
  }

  public void arcadeDrive(double speed, double turn) {
    drive.driveArcade(speed, turn);
  }

  public void initMotionProfile() {
    fr.set(ControlMode.PercentOutput, 0);

    // ------------ talons -----------------//
    fl.setInverted(true);
    fl.setSensorPhase(false);
    fr.setInverted(false);
    fr.setSensorPhase(false);

    // ------------ setup filters -----------------//
    /* other side is quad */
    fl.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, MotionProfileConstants.PID_PRIMARY,
        MotionProfileConstants.kTimeoutMs);

    /* Remote 0 will be the other side's Talon */
    fr.configRemoteFeedbackFilter(fl.getDeviceID(), RemoteSensorSource.TalonSRX_SelectedSensor,
        MotionProfileConstants.REMOTE_0, MotionProfileConstants.kTimeoutMs);
    /* Remote 1 will be a pigeon */
    fr.configRemoteFeedbackFilter(_imu.getDeviceID(), RemoteSensorSource.GadgeteerPigeon_Yaw,
        MotionProfileConstants.REMOTE_1, MotionProfileConstants.kTimeoutMs);
    /* setup sum and difference signals */
    fr.configSensorTerm(SensorTerm.Sum0, FeedbackDevice.RemoteSensor0, MotionProfileConstants.kTimeoutMs);
    fr.configSensorTerm(SensorTerm.Sum1, FeedbackDevice.QuadEncoder, MotionProfileConstants.kTimeoutMs);
    fr.configSensorTerm(SensorTerm.Diff1, FeedbackDevice.RemoteSensor0, MotionProfileConstants.kTimeoutMs);
    fr.configSensorTerm(SensorTerm.Diff0, FeedbackDevice.QuadEncoder, MotionProfileConstants.kTimeoutMs);
    /* select sum for distance(0), different for turn(1) */
    fr.configSelectedFeedbackSensor(FeedbackDevice.SensorSum, MotionProfileConstants.PID_PRIMARY,
        MotionProfileConstants.kTimeoutMs);

    if (MotionProfileConstants.kHeadingSensorChoice == 0) {

      fr.configSelectedFeedbackSensor(FeedbackDevice.SensorDifference, MotionProfileConstants.PID_TURN,
          MotionProfileConstants.kTimeoutMs);

      /* do not scale down the primary sensor (distance) */
      fr.configSelectedFeedbackCoefficient(1, MotionProfileConstants.PID_PRIMARY, MotionProfileConstants.kTimeoutMs);

      /*
       * scale empirically measured units to 3600units, this gives us - 0.1 deg
       * resolution - scales to human-readable units - keeps target away from ovefrlow
       * (12bit)
       *
       * Heading units should be scaled to ~4000 per 360 deg, due to the following
       * limitations... - Target param for aux PID1 is 18bits with a range of
       * [-131072,+131072] units. - Target for aux PID1 in motion profile is 14bits
       * with a range of [-8192,+8192] units. ... so at 3600 units per 360', that
       * ensures 0.1 deg precision in firmware closed-loop and motion profile
       * trajectory points can range +-2 rotations.
       */
      fr.configSelectedFeedbackCoefficient(
          MotionProfileConstants.kTurnTravelUnitsPerRotation / MotionProfileConstants.kEncoderUnitsPerRotation,
          MotionProfileConstants.PID_TURN, MotionProfileConstants.kTimeoutMs);
    } else {

      /*
       * do not scale down the primary sensor (distance). If selected sensor is going
       * to be a sensorSum user can pass 0.5 to produce an average.
       */
      fr.configSelectedFeedbackCoefficient(1.0, MotionProfileConstants.PID_PRIMARY, MotionProfileConstants.kTimeoutMs);

      fr.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor1, MotionProfileConstants.PID_TURN,
          MotionProfileConstants.kTimeoutMs);

      fr.configSelectedFeedbackCoefficient(
          MotionProfileConstants.kTurnTravelUnitsPerRotation / MotionProfileConstants.kPigeonUnitsPerRotation,
          MotionProfileConstants.PID_TURN, MotionProfileConstants.kTimeoutMs);
    }

    // ------------ telemetry-----------------//
    fr.setStatusFramePeriod(StatusFrame.Status_12_Feedback1, 20, MotionProfileConstants.kTimeoutMs);
    fr.setStatusFramePeriod(StatusFrame.Status_13_Base_PIDF0, 20, MotionProfileConstants.kTimeoutMs);
    fr.setStatusFramePeriod(StatusFrame.Status_14_Turn_PIDF1, 20, MotionProfileConstants.kTimeoutMs);
    fr.setStatusFramePeriod(StatusFrame.Status_10_Targets, 20, MotionProfileConstants.kTimeoutMs);
    /* speed up the left since we are polling it's sensor */
    fl.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 5, MotionProfileConstants.kTimeoutMs);

    fl.configNeutralDeadband(MotionProfileConstants.kNeutralDeadband, MotionProfileConstants.kTimeoutMs);
    fr.configNeutralDeadband(MotionProfileConstants.kNeutralDeadband, MotionProfileConstants.kTimeoutMs);

    fr.configMotionAcceleration(1000, MotionProfileConstants.kTimeoutMs);
    fr.configMotionCruiseVelocity(1000, MotionProfileConstants.kTimeoutMs);

    /*
     * max out the peak output (for all modes). However you can limit the output of
     * a given PID object with configClosedLoopPeakOutput().
     */
    fl.configPeakOutputForward(+1.0, MotionProfileConstants.kTimeoutMs);
    fl.configPeakOutputReverse(-1.0, MotionProfileConstants.kTimeoutMs);
    fr.configPeakOutputForward(+1.0, MotionProfileConstants.kTimeoutMs);
    fr.configPeakOutputReverse(-1.0, MotionProfileConstants.kTimeoutMs);

    /* distance servo */
    fr.config_kP(MotionProfileConstants.kSlot_Distanc, MotionProfileConstants.kGains_Distanc.kP,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kI(MotionProfileConstants.kSlot_Distanc, MotionProfileConstants.kGains_Distanc.kI,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kD(MotionProfileConstants.kSlot_Distanc, MotionProfileConstants.kGains_Distanc.kD,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kF(MotionProfileConstants.kSlot_Distanc, MotionProfileConstants.kGains_Distanc.kF,
        MotionProfileConstants.kTimeoutMs);
    fr.config_IntegralZone(MotionProfileConstants.kSlot_Distanc, (int) MotionProfileConstants.kGains_Distanc.kIzone,
        MotionProfileConstants.kTimeoutMs);
    fr.configClosedLoopPeakOutput(MotionProfileConstants.kSlot_Distanc,
        MotionProfileConstants.kGains_Distanc.kPeakOutput, MotionProfileConstants.kTimeoutMs);

    /* turn servo */
    fr.config_kP(MotionProfileConstants.kSlot_Turning, MotionProfileConstants.kGains_Turning.kP,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kI(MotionProfileConstants.kSlot_Turning, MotionProfileConstants.kGains_Turning.kI,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kD(MotionProfileConstants.kSlot_Turning, MotionProfileConstants.kGains_Turning.kD,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kF(MotionProfileConstants.kSlot_Turning, MotionProfileConstants.kGains_Turning.kF,
        MotionProfileConstants.kTimeoutMs);
    fr.config_IntegralZone(MotionProfileConstants.kSlot_Turning, (int) MotionProfileConstants.kGains_Turning.kIzone,
        MotionProfileConstants.kTimeoutMs);
    fr.configClosedLoopPeakOutput(MotionProfileConstants.kSlot_Turning,
        MotionProfileConstants.kGains_Turning.kPeakOutput, MotionProfileConstants.kTimeoutMs);

    /* magic servo */
    fr.config_kP(MotionProfileConstants.kSlot_MotProf, MotionProfileConstants.kGains_MotProf.kP,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kI(MotionProfileConstants.kSlot_MotProf, MotionProfileConstants.kGains_MotProf.kI,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kD(MotionProfileConstants.kSlot_MotProf, MotionProfileConstants.kGains_MotProf.kD,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kF(MotionProfileConstants.kSlot_MotProf, MotionProfileConstants.kGains_MotProf.kF,
        MotionProfileConstants.kTimeoutMs);
    fr.config_IntegralZone(MotionProfileConstants.kSlot_MotProf, (int) MotionProfileConstants.kGains_MotProf.kIzone,
        MotionProfileConstants.kTimeoutMs);
    fr.configClosedLoopPeakOutput(MotionProfileConstants.kSlot_MotProf,
        MotionProfileConstants.kGains_MotProf.kPeakOutput, MotionProfileConstants.kTimeoutMs);

    /* velocity servo */
    fr.config_kP(MotionProfileConstants.kSlot_Velocit, MotionProfileConstants.kGains_Velocit.kP,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kI(MotionProfileConstants.kSlot_Velocit, MotionProfileConstants.kGains_Velocit.kI,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kD(MotionProfileConstants.kSlot_Velocit, MotionProfileConstants.kGains_Velocit.kD,
        MotionProfileConstants.kTimeoutMs);
    fr.config_kF(MotionProfileConstants.kSlot_Velocit, MotionProfileConstants.kGains_Velocit.kF,
        MotionProfileConstants.kTimeoutMs);
    fr.config_IntegralZone(MotionProfileConstants.kSlot_Velocit, (int) MotionProfileConstants.kGains_Velocit.kIzone,
        MotionProfileConstants.kTimeoutMs);
    fr.configClosedLoopPeakOutput(MotionProfileConstants.kSlot_Velocit,
        MotionProfileConstants.kGains_Velocit.kPeakOutput, MotionProfileConstants.kTimeoutMs);

    fl.setNeutralMode(NeutralMode.Brake);
    fr.setNeutralMode(NeutralMode.Brake);

    /*
     * 1ms per loop. PID loop can be slowed down if need be. For example, - if
     * sensor updates are too slow - sensor deltas are very small per update, so
     * derivative error never gets large enough to be useful. - sensor movement is
     * very slow causing the derivative error to be near zero.
     */
    int closedLoopTimeMs = 1;
    fr.configSetParameter(ParamEnum.ePIDLoopPeriod, closedLoopTimeMs, 0x00, MotionProfileConstants.PID_PRIMARY,
        MotionProfileConstants.kTimeoutMs);
    fr.configSetParameter(ParamEnum.ePIDLoopPeriod, closedLoopTimeMs, 0x00, MotionProfileConstants.PID_TURN,
        MotionProfileConstants.kTimeoutMs);

    /**
     * false means talon's local output is PID0 + PID1, and other side Talon is PID0
     * - PID1 true means talon's local output is PID0 - PID1, and other side Talon
     * is PID0 + PID1
     */
    fr.configAuxPIDPolarity(false, MotionProfileConstants.kTimeoutMs);

    zeroSensors();
  }

  void zeroSensors() {

    fl.getSensorCollection().setQuadraturePosition(0, MotionProfileConstants.kTimeoutMs);
    fr.getSensorCollection().setQuadraturePosition(0, MotionProfileConstants.kTimeoutMs);
    _imu.setYaw(0, MotionProfileConstants.kTimeoutMs);
    _imu.setYaw(0, MotionProfileConstants.kTimeoutMs);

    _imu.setAccumZAngle(0, MotionProfileConstants.kTimeoutMs);
    System.out.println("        [Sensors] All sensors are zeroed.\n");
  }

  public void neutralMotors() {
    fl.neutralOutput();
    fr.neutralOutput();
  }

  @Override
  public void initDefaultCommand() {
    setDefaultCommand(new DriveCommand());
  }
}
