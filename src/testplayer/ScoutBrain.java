package testplayer;

import java.util.*;
import battlecode.common.*;
import testplayer.res.*;

@SuppressWarnings("unused")
public class ScoutBrain implements Brain {

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
		BulletInfo[] bullets = rc.senseNearbyBullets(); //1. dodge 2. stay out of range 3. farm 4. kill gardeners 5. move randomly
		dodge();
		farm();
		
		}
	private void move() throws GameActionException{
		dodge();
		
	}
	private void dodge(){
		BulletInfo[] bullets = rc.senseNearbyBullets();
		ArrayList<BulletInfo> dangerousBullets=  new ArrayList<BulletInfo>();
		for (BulletInfo x : bullets){
			if (x.getLocation().distanceTo(rc.getLocation())< 5 ){
				float angleToBullet = rc.getLocation().directionTo(x.getLocation()).getAngleDegrees();
				if (angleToBullet <= (x.getDir().getAngleDegrees() + 45)  && angleToBullet >= (x.getDir().getAngleDegrees() - 45)){
					dangerousBullets.add(x);
				}
			}
			
		}
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
	
	public void farm(){
		TreeInfo[] trees = rc.senseNearbyTrees();
		//shake anything that you can
		for(TreeInfo tree: trees){
			if(rc.canShake(tree.getID()))
				try {
					rc.shake(tree.getID());
				} catch (GameActionException e) {
				
					e.printStackTrace();
				}
		}
		//move towards unshaken trees
		
		MapLocation closestTree = trees[0].getLocation();
		float closestDistance = 99999;
		//look for closest unshaken trees and try to move towards them
		for(TreeInfo tree: trees){
			if(tree.getContainedBullets()!=0){
				if(rc.getLocation().distanceTo(tree.getLocation())<closestDistance){
					closestDistance = rc.getLocation().distanceTo(tree.getLocation());
					closestTree = tree.getLocation();	
				}
			}
		}
		//occasionally will get blocked from moving straight towards target - jiggle around to find way out
		if(rc.canMove(rc.getLocation().directionTo(closestTree))){
			try {
				rc.move(rc.getLocation().directionTo(closestTree));
			} catch (GameActionException e) {
			}
		}
		else{
			wander();
		}
	}
	//moves scout in random direction
	public void wander(){
		Direction[] directions = Directions.d6();
		while(!rc.hasMoved()){
			try {
				Direction rand = directions[(int)Math.random()*6];
				if(rc.canMove(rand)){
					rc.move(rand);
				}
			} catch (GameActionException e) {
			}
		}
	}
}