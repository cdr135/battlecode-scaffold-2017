package testplayer.res;

import java.util.*;

import battlecode.common.*;

/**
 * Put general-purpose utility functions here
 *
 */
public abstract class Utils {
	public static <T> List<T> shuffle(T[] array) {
		List<T> list = Arrays.asList(array);
		Collections.shuffle(list);;
		return list;
	}
	public static float distance(MapLocation ml1, MapLocation ml2) {
		return ml1.distanceTo(ml2);
	}
	public static float distanceSquared(MapLocation ml1, MapLocation ml2) {
		return ml1.distanceSquaredTo(ml2);
	}
	/**
	 * direction from 1st to 2nd
	 */
	public static Direction direction(MapLocation ml1, MapLocation ml2) {
		return ml1.directionTo(ml2);
	}
	
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
