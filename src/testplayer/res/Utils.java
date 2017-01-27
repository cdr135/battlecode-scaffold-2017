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
}
