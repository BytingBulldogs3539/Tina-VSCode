/**
 * Example logic for firing and managing motion profiles.
 * This example sends MPs, waits for them to finish
 * Although this code uses a CANTalon, nowhere in this module do we changeMode() or call set() to change the output.
 * This is done in Robot.java to demonstrate how to change control modes on the fly.
 * 
 * The only routines we call on Talon are....
 * 
 * changeMotionControlFramePeriod
 * 
 * getMotionProfileStatus		
 * clearMotionProfileHasUnderrun     to get status and potentially clear the error flag.
 * 
 * pushMotionProfileTrajectory
 * clearMotionProfileTrajectories
 * processMotionProfileBuffer,   to push/clear, and process the trajectory points.
 * 
 * getControlMode, to check if we are in Motion Profile Control mode.
 * 
 * Example of advanced features not demonstrated here...
 * [1] Calling pushMotionProfileTrajectory() continuously while the Talon executes the motion profile, thereby keeping it going indefinitely.
 * [2] Instead of setting the sensor position to zero at the start of each MP, the program could offset the MP's position based on current position. 
 */

package frc.robot.motionprofile;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.IMotorController;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;

import com.ctre.phoenix.motion.*;
import com.ctre.phoenix.motion.TrajectoryPoint.TrajectoryDuration;

public class MotionProfile
{

	/**
	 * The status of the motion profile executer and buffer inside the Talon.
	 * Instead of creating a new one every time we call getMotionProfileStatus, keep
	 * one copy.
	 */
	public boolean finished = false;
	private MotionProfileStatus _status = new MotionProfileStatus();
	/** additional cache for holding the active trajectory point */
	double _pos = 0, _vel = 0, _heading = 0;

	double[][] points;
	int numPoints;
	/**
	 * reference to the talon we plan on manipulating. We will not changeMode() or
	 * call set(), just get motion profile status and make decisions based on motion
	 * profile.
	 */
	private IMotorController _motorController;
	/**
	 * State machine to make sure we let enough of the motion profile stream to
	 * talon before we fire it.
	 */
	private int _state = 0;
	/**
	 * Any time you have a state machine that waits for external events, its a good
	 * idea to add a timeout. Set to -1 to disable. Set to nonzero to count down to
	 * '0' which will print an error message. Counting loops is not a very accurate
	 * method of tracking timeout, but this is just conservative timeout. Getting
	 * time-stamps would certainly work too, this is just simple (no need to worry
	 * about timer overflows).
	 */
	private int _loopTimeout = -1;
	/**
	 * If start() gets called, this flag is set and in the control() we will service
	 * it.
	 */
	private boolean _bStart = false;

	private boolean _bForward = false;

	/**
	 * Since the CANTalon.set() routine is mode specific, deduce what we want the
	 * set value to be and let the calling module apply it whenever we decide to
	 * switch to MP mode.
	 */
	private SetValueMotionProfile _setValue = SetValueMotionProfile.Disable;
	/**
	 * How many trajectory points do we wait for before firing the motion profile.
	 */
	private static final int kMinPointsInTalon = 5;
	/**
	 * Just a state timeout to make sure we don't get stuck anywhere. Each loop is
	 * about 20ms.
	 */
	private static final int kNumLoopsTimeout = 10;

	/**
	 * Lets create a periodic task to funnel our trajectory points into our talon.
	 * It doesn't need to be very accurate, just needs to keep pace with the motion
	 * profiler executer. Now if you're trajectory points are slow, there is no need
	 * to do this, just call _talon.processMotionProfileBuffer() in your teleop
	 * loop. Generally speaking you want to call it at least twice as fast as the
	 * duration of your trajectory points. So if they are firing every 20ms, you
	 * should call every 10ms.
	 */
	class PeriodicRunnable implements java.lang.Runnable
	{
		public void run()
		{
			_motorController.processMotionProfileBuffer();
		}
	}

	Notifier _notifer = new Notifier(new PeriodicRunnable());

	/**
	 * C'tor
	 * 
	 * @param talon
	 *                  reference to Talon object to fetch motion profile status
	 *                  from.
	 */
	public MotionProfile(IMotorController motorController, double[][] profile)
	{
		_motorController = motorController;
		points = profile;
		numPoints = profile.length;
		/*
		 * since our MP is 10ms per point, set the control frame rate and the notifer to
		 * half that
		 */
		_motorController.changeMotionControlFramePeriod(5);
	}

	/**
	 * Called to clear Motion profile buffer and reset state info during disabled
	 * and when Talon is not in MP control mode.
	 */
	public void reset()
	{
		/*
		 * Let's clear the buffer just in case user decided to disable in the middle of
		 * an MP, and now we have the second half of a profile just sitting in memory.
		 */
		_motorController.clearMotionProfileTrajectories();
		/* When we do re-enter motionProfile control mode, stay disabled. */
		_setValue = SetValueMotionProfile.Disable;
		/* When we do start running our state machine start at the beginning. */
		_state = 0;
		_loopTimeout = -1;
		/*
		 * If application wanted to start an MP before, ignore and wait for next button
		 * press
		 */
		_bStart = false;
	}

	boolean IsMotionProfile(ControlMode controlMode)
	{
		if (controlMode == ControlMode.MotionProfile)
			return true;
		if (controlMode == ControlMode.MotionProfileArc)
			return true;
		return false;
	}

	/**
	 * Called every loop.
	 */
	public void control()
	{
		/* Get the motion profile status every loop */
		_motorController.getMotionProfileStatus(_status);

		/*
		 * track time, this is rudimentary but that's okay, we just want to make sure
		 * things never get stuck.
		 */
		if (_loopTimeout < 0)
		{
			/* do nothing, timeout is disabled */
		}
		else
		{
			/* our timeout is nonzero */
			if (_loopTimeout == 0)
			{

				/*
				 * something is wrong. Talon is not present, unplugged, breaker tripped
				 */
			}
			else
			{
				--_loopTimeout;
			}
		}

		/* first check if we are in MP mode */
		if (false == IsMotionProfile(_motorController.getControlMode()))
		{
			/*
			 * we are not in MP mode. We are probably driving the robot around using
			 * gamepads or some other mode.
			 */
			_state = 0;
			_loopTimeout = -1;
		}
		else
		{
			/*
			 * we are in MP control mode. That means: starting Mps, checking Mp progress,
			 * and possibly interrupting MPs if thats what you want to do.
			 */
			switch (_state)
			{
			case 0: /* wait for application to tell us to start an MP */
				if (_bStart)
				{
					_bStart = false;

					_setValue = SetValueMotionProfile.Disable;
					startFilling();
					_notifer.startPeriodic(0.005);

					/*
					 * MP is being sent to CAN bus, wait a small amount of time
					 */
					_state = 1;
					_loopTimeout = kNumLoopsTimeout;
				}
				break;
			case 1: /*
					 * wait for MP to stream to Talon, really just the first few points
					 */
				/* do we have a minimum numberof points in Talon */
				if (_status.btmBufferCnt > kMinPointsInTalon)
				{
					/* start (once) the motion profile */
					_setValue = SetValueMotionProfile.Enable;
					/* MP will start once the control frame gets scheduled */
					_state = 2;
					_loopTimeout = kNumLoopsTimeout;
				}
				break;
			case 2: /* check the status of the MP */
				/*
				 * if talon is reporting things are good, keep adding to our timeout. Really
				 * this is so that you can unplug your talon in the middle of an MP and react to
				 * it.
				 */
				if (_status.isUnderrun == false)
				{
					_loopTimeout = kNumLoopsTimeout;
				}
				if (_status.isUnderrun)
				{
					System.out.println("Motion Profile Is Under Run (this is good).");

				}
				/*
				 * If we are executing an MP and the MP finished, start loading another. We will
				 * go into hold state so robot servo's position.
				 */
				if (_status.activePointValid && _status.isLast)
				{
					/*
					 * because we set the last point's isLast to true, we will get here when the MP
					 * is done
					 */
					_notifer.stop();
					finished = true;
					_setValue = SetValueMotionProfile.Hold;
					_state = 0;
					_loopTimeout = -1;

				}
				break;
			}

			/* Get the motion profile status every loop */
			_motorController.getMotionProfileStatus(_status);
			_heading = _motorController.getActiveTrajectoryHeading();
			_pos = _motorController.getActiveTrajectoryPosition();
			_vel = _motorController.getActiveTrajectoryVelocity();

			// We are going to report all of these values to the smartdash for tuning the
			// pid.

			SmartDashboard.putBoolean("Last Point", _status.isLast);
			SmartDashboard.putNumber("BTM Buffer Count", _status.btmBufferCnt);
			SmartDashboard.putNumber("Top Buffer Count", _status.topBufferCnt);
			SmartDashboard.putBoolean("Is Under Run", _status.isUnderrun);

			SmartDashboard.putNumber("_pos", _pos);
			SmartDashboard.putNumber("_vel", _vel);
			SmartDashboard.putNumber("_heading", _heading);
		}
	}

	/**
	 * Find enum value if supported.
	 * 
	 * @param durationMs
	 * @return enum equivalent of durationMs
	 */
	private TrajectoryDuration GetTrajectoryDuration(int durationMs)
	{
		/* create return value */
		TrajectoryDuration retval = TrajectoryDuration.Trajectory_Duration_0ms;
		/* convert duration to supported type */
		retval = retval.valueOf(durationMs);
		/* check that it is valid */
		if (retval.value != durationMs)
		{
			DriverStation.reportError(
					"Trajectory Duration not supported - use configMotionProfileTrajectoryPeriod instead", false);
		}
		/* pass to caller */
		return retval;
	}

	/** Start filling the MPs to all of the involved Talons. */
	private void startFilling()
	{
		startFilling(points, numPoints);
	}

	private void startFilling(double[][] profile, int totalCnt)
	{
		TrajectoryPoint point = new TrajectoryPoint();

		/* did we get an underrun condition since last time we checked ? */
		if (_status.hasUnderrun)
		{
			/* better log it so we know about it */
			/*
			 * clear the error. This flag does not auto clear, this way we never miss
			 * logging it.
			 */
			_motorController.clearMotionProfileHasUnderrun(MotionProfileConstants.kTimeoutMs);
		}
		/*
		 * just in case we are interrupting another MP and there is still buffer points
		 * in memory, clear it.
		 */
		_motorController.clearMotionProfileTrajectories();

		/*
		 * set the base trajectory period to zero, use the individual trajectory period
		 * below
		 */
		_motorController.configMotionProfileTrajectoryPeriod(MotionProfileConstants.kBaseTrajPeriodMs,
				MotionProfileConstants.kTimeoutMs);

		/* This is fast since it's just into our TOP buffer */
		for (int i = 0; i < totalCnt; ++i)
		{
			double direction = _bForward ? +1 : -1;
			double positionRot = profile[i][0];
			double velocityRPM = profile[i][1];
			double heading = profile[i][3];

			/* for each point, fill our structure and pass it to API */
			point.position = direction * positionRot * MotionProfileConstants.kSensorUnitsPerRotation * 2; // Convert
																											// Revolutions
																											// to Units
			point.velocity = direction * velocityRPM * MotionProfileConstants.kSensorUnitsPerRotation / 600.0; // Convert
																												// RPM
																												// to
																												// Units/100ms
			point.auxiliaryPos = heading * 10; /* scaled such that 3600 => 360 deg */
			point.profileSlotSelect0 = MotionProfileConstants.kSlot_MotProf; /*
																				 * which set of gains would you like to
																				 * use [0,3]?
																				 */
			point.profileSlotSelect1 = MotionProfileConstants.kSlot_Turning; /* auxiliary PID [0,1], leave zero */
			point.timeDur = GetTrajectoryDuration((int) profile[i][2]);
			point.zeroPos = false;
			if (i == 0)
				point.zeroPos = true; /* set this to true on the first point */

			point.isLastPoint = false;
			if ((i + 1) == totalCnt)
				point.isLastPoint = true; /* set this to true on the last point */

			_motorController.pushMotionProfileTrajectory(point);
		}
	}

	public void zeroSensors()
	{
		Robot.driveTrain.fl.getSensorCollection().setQuadraturePosition(0, MotionProfileConstants.kTimeoutMs);
		Robot.driveTrain.fl.getSensorCollection().setQuadraturePosition(0, MotionProfileConstants.kTimeoutMs);
		Robot.driveTrain._imu.setYaw(0, MotionProfileConstants.kTimeoutMs);
		Robot.driveTrain._imu.setAccumZAngle(0, MotionProfileConstants.kTimeoutMs);
		System.out.println("        [Sensors] All sensors are zeroed.\n");
	}

	/**
	 * Called by application to signal Talon to start the buffered MP (when it's
	 * able to).
	 */
	void start(boolean bForward)
	{
		_bStart = true;
		_bForward = bForward;
	}

	/**
	 * 
	 * @return the output value to pass to Talon's set() routine. 0 for disable
	 *         motion-profile output, 1 for enable motion-profile, 2 for hold
	 *         current motion profile trajectory point.
	 */
	SetValueMotionProfile getSetValue()
	{
		return _setValue;
	}
}
