package testplayer;

import java.util.*;
import battlecode.common.*;
import testplayer.res.*;

@SuppressWarnings("unused")
public class SoldierBrain implements Brain {

	private Routine last;
	private Routine current;
	private RobotController rc;
	private MapLocation lastLoc;
	private double radius;
	private int radiusInc;
	private boolean space;
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

	private void runTurn() throws GameActionException {
		//TODO: add dodging
		//fires triad at closest robot if <4 away
		if(rc.canFireTriadShot()) {
			RobotInfo[] enemies = rc.senseNearbyRobots((float)radius,rc.getTeam().opponent());
			if(enemies.length>0) {
				RobotInfo closest = enemies[0];
				double length = rc.getLocation().distanceSquaredTo(closest.getLocation());
				for(RobotInfo r: enemies) {
					double newLength = rc.getLocation().distanceSquaredTo(r.getLocation());
					if(newLength<length) {
						closest=r;
						length=newLength;
					}
				}
				if(length<16) {
					rc.fireTriadShot(rc.getLocation().directionTo(closest.getLocation()));
				}
			}
		}
			
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