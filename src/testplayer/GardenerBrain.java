package testplayer;

import java.util.*;
import battlecode.common.*;
import testplayer.res.*;
import static testplayer.res.Utils.*;

@SuppressWarnings("unused")
public class GardenerBrain implements Brain {

	private Routine last;
	private Routine current;
	private RobotController rc;
	private MapLocation lastLoc;
	private double radius;
	private int radiusInc;
	private boolean space;
	private boolean builtScout; // might be bad
	private Direction[] direction = Directions.d6();
	private boolean startbuilding;
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

	private void runTurn() throws GameActionException {
		if (!startbuilding){
			move();
		}
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld = rc.senseNearbyRobots();
		// find some way to balance economy of scout it works
		
		if (builtScout == false) {
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
					rc.buildRobot(RobotType.SCOUT, dir);
					builtScout = true;
					break;
				}
			}
		}
		 
		{
			float mh = 51;
			Integer tid = null;
			for (TreeInfo t : treeinfo) {
				if (t.getTeam() == rc.getTeam() && rc.canInteractWithTree(t.ID))
					if  (t.getHealth() < mh) {
						mh = t.getHealth();
						tid = t.getID();
					}

			}
			if (tid != null)
				rc.water(tid);
		}
		for (Direction d : shuffle(direction)) {
			if (rc.canPlantTree(d)) {
				rc.plantTree(d);
				break;
			}
		}


	}

	private void move() throws GameActionException {
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld = rc.senseNearbyRobots();
		MapLocation nearestGardner = null;
		for (RobotInfo x : nejworld) {
			if (x.getType().equals(RobotType.GARDENER)) {
				if (x.getLocation().distanceTo(rc.getLocation()) < 5) {
					nearestGardner = x.getLocation();
				}
			}
		}
		/*if (nearestGardner == null) {
			startbuilding = true;
		}else {
			if (rc.canMove(rc.getLocation().directionTo(nearestGardner).opposite())) {
				rc.move(rc.getLocation().directionTo(nearestGardner).opposite());
			} else {
				startbuilding = true;
			}
		}*/
		for (Direction dir : shuffle(direction)){
			if (!rc.hasMoved() && rc.canMove(dir))
				rc.move(dir);
		}
		/*Direction dir;
		do {
			dir = new Direction((float) (Math.random()*2*Math.PI));
		} while (!rc.canMove(dir));
		rc.move(dir);*/
	}

	private void initialize() throws GameActionException {
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 8;
		radiusInc = 7;
		space = true;
		builtScout = false;
		MapLocation hehe = new MapLocation(0, 0);
		for (int i = 0; i < 6; i++) {
			MapLocation heheNew = new MapLocation(hehe.x + (float) Math.cos(Math.PI / 3 * i),
					hehe.y + (float) Math.sin(Math.PI / 3 * i));
			direction[i] = hehe.directionTo(heheNew);
		}
		startbuilding = false;
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