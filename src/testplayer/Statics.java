package testplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Statics {
	
	public static MapLocation referenceLocation;

	public static int maxCord = 80;

	public static MapLocation[] combineLocs(MapLocation[]... arrs) {
		int length = 0;
		for (MapLocation[] arr : arrs)
			length += arr.length;
		int i = 0;
		MapLocation[] all = new MapLocation[length];
		for (MapLocation[] arr : arrs)
			for (MapLocation loc : arr)
				all[i++] = loc;

		return all;
	}

	public static RobotInfo[] combineRobotInfo(RobotInfo[]... arrs) {
		int length = 0;
		for (RobotInfo[] arr : arrs)
			length += arr.length;
		int i = 0;
		RobotInfo[] all = new RobotInfo[length];
		for (RobotInfo[] arr : arrs)
			for (RobotInfo loc : arr)
				all[i++] = loc;

		return all;
	}

	@Deprecated
	public static Object[] combineArrays(Object[]... arrs) {
		int length = 0;
		for (Object[] arr : arrs) {
			length += arr.length;
		}
		System.out.println("length: = " + length);
		Object[] hi = new Object[length];
		int i = 0;
		for (Object[] arr : arrs) {
			for (Object o : arr) {
				hi[i++] = o;
			}
		}
		System.out.println("final i: =" + i);
		if (length == 0 && i == 0) {
			hi = new MapLocation[] { new MapLocation(0, 0) };
			System.out.println("test: " + hi);
		}
		return hi;
	}

	public static boolean contains(Object obj, Object[] arr) {
		for (Object i : arr)
			if (i.equals(obj))
				return true;
		return false;
	}

	
}