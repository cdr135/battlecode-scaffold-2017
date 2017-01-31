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
					} else if (distance(rc.getLocation(), t.location) <= 3.01)
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
				if (distance(r.location, tree.location) <= 1.01 + GameConstants.INTERACTION_DIST_FROM_EDGE)
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

		for (Direction dir : shuffle(direction)){
			if(!rc.hasMoved() && rc.canMove(dir)){
				rc.move(dir);
			}
		}	
		/**/
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
		/*MapLocation hehe = new MapLocation(0, 0);
		for (int i = 0; i < 6; i++) {
			MapLocation heheNew = new MapLocation(hehe.x + (float) Math.cos(Math.PI / 3 * i),
					hehe.y + (float) Math.sin(Math.PI / 3 * i));
			direction[i] = hehe.directionTo(heheNew);
		}*/
		startbuilding = false;
		{
			Direction[] nd = new Direction[5];
			for (Direction d : shuffle(direction)) {
				RobotInfo r = rc.senseRobotAtLocation(rc.getLocation().add(d));
				if (r != null && r.type == RobotType.ARCHON && r.getTeam() == rc.getTeam()) {
					int i = 0;
					for (Direction d1 : direction)
						if (d1 != d) nd[i++] = d1;
					direction = nd;
					break;
				}
			}
		}
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