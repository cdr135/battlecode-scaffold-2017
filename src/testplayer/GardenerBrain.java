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

		if (builtScout == false && Globals.numScouts < Globals.MAX_SCOUTS) {
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
					rc.buildRobot(RobotType.SCOUT, dir);
					builtScout = true;
					break;
				}
			}
		}

		boolean spt = true;
		{
			float mh = 51;
			Integer tid = null;
			for (TreeInfo t : treeinfo) {
				if (t.getTeam().equals(rc.getTeam())) {
					if (rc.canInteractWithTree(t.ID)) {
						if  (t.getHealth() < mh) {
							mh = t.getHealth();
							tid = t.getID();
						}
					} else if (distance(rc.getLocation(), t.location) <= 3.02)
						spt = false;
				}
			}
			if (tid != null)
				rc.water(tid);
		}
		if (rc.getTeamBullets() >= 50) System.out.println((spt?"YES":"NO")+rc.getID());
		
		if (spt) for (Direction d : shuffle(direction)) {
			if (rc.canPlantTree(d)) {
				rc.plantTree(d);
				break;
			}
		}


	}

	private boolean hasOtherGardener(TreeInfo tree, RobotInfo[] robots) {
		for (RobotInfo r : robots)
			if (r.type == RobotType.GARDENER)
				if (distance(r.location, tree.location) <= 
				1 + GameConstants.BULLET_TREE_RADIUS +
				GameConstants.INTERACTION_DIST_FROM_EDGE)
					return true;
		return false;
	}

	private void move() throws GameActionException {
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld = rc.senseNearbyRobots();
		MapLocation nearestGardner = null;
		/*for (RobotInfo x : nejworld) {
			if (x.getType().equals(RobotType.GARDENER)) {
				if (x.getLocation().distanceTo(rc.getLocation()) < 5) {
					nearestGardner = x.getLocation();
				}
			}
		}*/

		for (TreeInfo tree : treeinfo) treecheck: {
			if (!tree.team.equals(rc.getTeam()))
				continue;
			if (rc.canInteractWithTree(tree.ID)) {
				boolean t_hg = hasOtherGardener(tree, nejworld);
				/*for (RobotInfo r : nejworld) {
					if (r.type == RobotType.GARDENER)
						if (distance(r.location,tree.location)<=1) {
							t_hg = true;
							break;
						}
				}*/
				if (!t_hg)
					return;
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
		/**/
	}

	private void initialize() throws GameActionException {
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 8;
		radiusInc = 7;
		space = true;
		builtScout = false;
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