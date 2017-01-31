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
		RobotInfo[] robotinfo = rc.senseNearbyRobots();
		//finds the nearby allied tree with the least hp and waters it
		float minHP = 51;
		Integer treeID = null;
		for (TreeInfo t : treeinfo) {
			if (t.getTeam().equals(rc.getTeam())) {
				if (rc.canInteractWithTree(t.ID)) {
					if  (t.getHealth() < minHP) {
						minHP = t.getHealth();
						treeID = t.getID();
					}
				}
			}
		}
		if (treeID != null)
			rc.water(treeID);
		if (startbuilding){
			for (Direction d : shuffle(direction)) {
				if (rc.canPlantTree(d)) {
					for (Direction di : direction)
						//helps with debugging
						rc.setIndicatorDot(rc.getLocation().add(di,2), di==d?0:255, 0, di==d?255:0);
					rc.plantTree(d);
					break;
				}
			}




			//!builtScout && 3 * robotinfo.length + treeinfo.length < 10

		}


	}
	private void build() throws GameActionException{
		if (!builtScout) {
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
					rc.buildRobot(RobotType.SCOUT, dir);
					builtScout = true;
					break;
				}
			}
		}
		RobotInfo[] allied = rc.senseNearbyRobots(7, rc.getTeam());
		TreeInfo[] ntrees = rc.senseNearbyTrees(7, Team.NEUTRAL),
				atrees = rc.senseNearbyTrees(7, rc.getTeam());
		int numSoldiers = 0;
		int numLumb = 0;
		for (RobotInfo x: allied){
			if (x.getType().equals(RobotType.SOLDIER)){
				numSoldiers++;
			}
			if (x.getType().equals(RobotType.LUMBERJACK)){
				numLumb++;
			}
		}
		if (numLumb < 1 + ntrees.length/3 && ntrees.length > 0){
			//arbitrary
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
					rc.buildRobot(RobotType.LUMBERJACK, dir);
					break;
				}
			}
		}
		if (numSoldiers < 2 && atrees.length > 0){
			//arbitrary
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
					rc.buildRobot(RobotType.SOLDIER, dir);
					break;
				}
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
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(7, rc.getTeam());
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees(7, rc.getTeam());
		int closeAllies = 0;
		RobotInfo closestAlly = null;
		for (RobotInfo x : nearbyAllies){
			if (x.getType().equals(RobotType.ARCHON) || x.getType().equals(RobotType.GARDENER)){
				closeAllies++;
				if (closestAlly == null || distance(rc.getLocation(),x.location) < distance(rc.getLocation(),closestAlly.location))
					closestAlly = x;
			}
		}
		/*
		TreeInfo closestTree = null;
		for (TreeInfo x : nearbyTrees){
			if (closestTree == null){
					closestTree = x;
			}
			else if (rc.getLocation().distanceTo(closestAlly.getLocation()) > rc.getLocation().distanceTo(x.getLocation())){
				closestTree = x;
			}
		}
		 */	

		if (closeAllies < 2){
			// && closestTree == null){
			startbuilding = true;
		}
		/*
		else if (closestAlly == null && closestTree != null){
			moveAround(closestTree.getLocation().directionTo(rc.getLocation()));
		}
		else if (closestTree == null && closestAlly!=null){
			moveAround(closestAlly.getLocation().directionTo(rc.getLocation()));
		}
		 */
		if (closestAlly != null) {
			/*
			if (rc.getLocation().distanceTo(closestTree.getLocation()) < rc.getLocation().distanceSquaredTo(closestAlly.getLocation())){
				moveAround(closestTree.getLocation().directionTo(rc.getLocation()));
			}
			else{
			 */
			moveAround(closestAlly.getLocation().directionTo(rc.getLocation()));
		}
		build();
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
	public void moveAround (Direction dir) throws GameActionException{
		for (int i = 0; i < 12; i++){
			Direction newDir = new Direction ((float) (dir.radians + Math.PI/24));
			if (rc.canMove(newDir)){
				rc.move(newDir);
				break;
			}
			Direction newDir2 = new Direction ((float) ((dir.radians) - Math.PI/24));
			if (rc.canMove(newDir2)){
				rc.move(newDir2);
				break;
			}
		}
	}
}