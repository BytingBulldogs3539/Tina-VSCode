package frc.robot.motionprofile;

public class MotionGains
{
	public final double kP;
	public final double kI;
	public final double kD;
	public final double kF;
	public final double kIzone;
	public final double kPeakOutput;

	public MotionGains(double _kP, double _kI, double _kD, double _kF, double _kIzone, double _kPeakOutput)
	{
		kP = _kP;
		kI = _kI;
		kD = _kD;
		kF = _kF;
		kIzone = _kIzone;
		kPeakOutput = _kPeakOutput;
	}
}