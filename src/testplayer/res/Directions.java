package testplayer.res;
import battlecode.common.*;

public enum Directions {
	D0(0),
	D30(30),
	D60(60),
	D90(90),
	D120(120),
	D150(150),
	D180(180),
	D210(210),
	D240(240),
	D270(270),
	D300(300),
	D330(330);	//some constants - in 30 degree increments
	public final Direction dir;
	public final float angle;
	private Directions(int deg) {
		angle = (float) (deg*Math.PI/180);
		dir = new Direction(angle);
	}
	public float getAngleRadians() {
		return angle;
	}
	public float getAngleDegrees() {
		return (float) (angle*180/Math.PI);
	}
	public Direction getDirection() {
		return dir;
	}
	public static Direction[] d6() {
		Direction[] r = {
				D0.dir, D60.dir, D120.dir, D180.dir, D240.dir, D300.dir
		};
		return r;
	}
	public static Direction dir(int deg) {
		return new Direction((float) (deg*Math.PI/180));
	}

}
