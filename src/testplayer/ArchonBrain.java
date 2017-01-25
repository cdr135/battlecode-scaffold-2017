package testplayer;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {

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
	private Direction[] blah  = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

	private void runTurn() throws GameActionException {
		float numPoints = rc.getTeamBullets();
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld =  rc.senseNearbyRobots();
		double numGardeners = 0;
		double numTrees = 0;
		for (int i = 0; i < nejworld.length; i++){
			if (nejworld[i].getType().equals(RobotType.GARDENER)){
				numGardeners++;
			}
		}
		for (int i = 0 ; i < treeinfo.length; i++){
			if (treeinfo[i].team.equals(rc.getTeam())){
				numTrees++;
			}
		}
		
		if (numTrees/numGardeners >= 4 || numGardeners < 3){ //placeholder
			for (int j = 0; j < 4; j++){
				if (rc.canBuildRobot(RobotType.GARDENER, blah[j])){
					rc.buildRobot(RobotType.GARDENER, blah[j]);
					break;
				}
			}
				
		}
		if (numTrees > 10 && rc.getTeamBullets() > 100){
			rc.donate(rc.getTeamBullets());
		}
		BulletInfo[] bleh = rc.senseNearbyBullets();
		//insert dodge script here/movement
		}
	
	private void move() throws GameActionException{
		
	}
	private void initialize() throws GameActionException {
		current = Routine.GROUP;
		robots = new TreeMap<Integer, RobotInfo>();
		radius = 8;
		radiusInc = 7;
		space = true;
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