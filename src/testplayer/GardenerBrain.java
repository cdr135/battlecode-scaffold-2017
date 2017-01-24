package testplayer;

import java.util.*;
import battlecode.common.*;

public class GardenerBrain implements Brain {

	private enum Routine {
		CLUSTER, GROUP, NONE;
	}

	private Routine last;
	private Routine current;
	private RobotController rc;
	private MapLocation lastLoc;
	private double radius;
	private int radiusInc;
	private boolean space;
	private boolean builtScout; // might be bad
	private Direction[] blah  = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

	private void runTurn() throws GameActionException {
		rc.readBroadcast(arg0) //find some way to balance economy of scout it works 
		for (int j = 0; j < 4; j++){
			if (rc.canBuildRobot(RobotType.SCOUT, blah[j])){
				rc.buildRobot(RobotType.SCOUT, blah[j]);
				break;
			}
		}
		
		direction.
		
		
		rc.buildRobot(RobotType.SCOUT, direction);
		if (builtScout == false){
			
		}
			
		
		}
		
	private void initialize() throws GameActionException {
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 8;
		radiusInc = 7;
		space = true;
		builtScout = false;
	}

	@Override
	public void run(RobotController rcI) {
		rc = rcI;
		try {
			initialize();
		} catch (GameActionException e1) {
			e1.printStackTrace();
		}

		while (true) {
			Clock.yield();
			try {
				runTurn();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

}