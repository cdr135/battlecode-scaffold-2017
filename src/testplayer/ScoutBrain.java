package testplayer;

import java.util.*;

import com.sun.jdi.Location;

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
	private Direction roam;
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

	private void runTurn() throws GameActionException {
		move();
		
		}
	private void move() throws GameActionException{
		//try to dodge anything killing us
		if (rc.senseNearbyBullets().length!= 0){
			dodge();
		}
		
		//if necessary, move out of range of enemies
		stayOutOfRange();
		
		//if we're already close to a gardener just continue shooting
		RobotInfo[] nearby = rc.senseNearbyRobots();
		boolean nearbyGardener = false;
		MapLocation gardener = null;
		for (RobotInfo x : nearby){
			if (x.team.equals(rc.getTeam().opponent()) && x.type.equals(RobotType.GARDENER)){

				if (rc.getLocation().distanceTo(x.getLocation()) < 10){
					nearbyGardener = true;
					gardener = x.getLocation();
				}
			}
		}
		if (nearbyGardener == true){
			rc.move(rc.getLocation().directionTo(gardener));
			rc.fireSingleShot(rc.getLocation().directionTo(gardener));
		}
		//try to farm;
		TreeInfo[] blah = rc.senseNearbyTrees();
		boolean treesExist = false;
		for (TreeInfo x : blah){
			if (x.getTeam().equals(Team.NEUTRAL)){
				if (x.containedBullets != 0){
					treesExist = true;
				}
			}
		}
		if (treesExist == true){
			farm();
		}
		
		roam();
	}
	
	private void roam() throws GameActionException{
		if (rc.canMove(roam)){
			rc.move(roam);
		}
		else{
			roam = new Direction (roam.radians + (float) Math.PI/2);
			if(rc.canMove(roam)){
				rc.move(roam);
			}
		}
		
	}
	private void dodge() throws GameActionException{
		BulletInfo[] bullets = rc.senseNearbyBullets();
		ArrayList<BulletInfo> dangerousBullets=  new ArrayList<BulletInfo>();
		for (BulletInfo x : bullets){
			if (x.getLocation().distanceTo(rc.getLocation())< 20 ){
				Direction angleToBullet = rc.getLocation().directionTo(x.getLocation());
				float angleBetween = x.getDir().degreesBetween(angleToBullet);
				if (angleBetween < 90){
					dangerousBullets.add(x);
				}
			}
		
		}
		if (dangerousBullets.size() == 0){
		}
		else if (dangerousBullets.size() == 1){
			//maximize angle between bbullet trajectory and nextlocation
			BulletInfo naw = dangerousBullets.get(0);
			Direction dir = new Direction ((float) ((naw.getDir().getAngleDegrees() * (Math.PI/180)) + (Math.PI/2)));
			MapLocation loc1 = new MapLocation((float)(rc.getLocation().x + rc.getType().strideRadius * Math.cos(dir.radians)),(float) (rc.getLocation().y + rc.getType().strideRadius * Math.sin(dir.radians)));
			Direction dir2 = dir.opposite();
			MapLocation loc2 = new MapLocation((float)(rc.getLocation().x + rc.getType().strideRadius * Math.cos(dir2.radians)),(float) (rc.getLocation().y + rc.getType().strideRadius * Math.sin(dir2.radians)));
			float angle1 = naw.dir.degreesBetween(naw.getLocation().directionTo(loc1));
			float angle2 = naw.dir.degreesBetween(naw.getLocation().directionTo(loc2));
			if (angle1 > angle2){
				moveAround(dir);
			}
			else{
				moveAround(dir2);
			}
		}
		else{
			//coloumb dodge#1
			float x  = 0;
			float y = 0;
			for (BulletInfo j: bullets){
				float angle = j.getLocation().directionTo(rc.getLocation()).getAngleDegrees();
				float distance  =  j.getLocation().distanceSquaredTo(rc.getLocation());
				x += j.speed * (Math.cos(angle/180 * Math.PI))/distance;
				y += j.speed * (Math.sin(angle/180 * Math.PI))/distance;
			}
			MapLocation destination = new MapLocation(rc.getLocation().x + x, rc.getLocation().y + y);
			moveAround(rc.getLocation().directionTo(destination));
		}
	}
	private void stayOutOfRange(){
		RobotInfo[] robots = rc.senseNearbyRobots();
		
		//collect list of dangerous robots, based on their individual sight radius'
		ArrayList<RobotInfo> dangerousRobots = new ArrayList<RobotInfo>();
		for(RobotInfo r: robots){
			if(r.getTeam() != rc.getTeam()){
				RobotType robotType = r.getType();
				if (robotType == RobotType.SCOUT || robotType == RobotType.SOLDIER || robotType == RobotType.LUMBERJACK
						|| robotType == RobotType.TANK) {
					if(robotType.sensorRadius < rc.getLocation().distanceTo(r.getLocation())){
						dangerousRobots.add(r);
						rc.setIndicatorLine(rc.getLocation(), r.getLocation(), 0, 0, 0); 
					}
				}
			}
		}
		if (dangerousRobots.size() != 0) {
			// find best place to dodge by considering possible locations and
			// maximizing the inverse square of the distances.
			Direction[] directions = Directions.d12();
			float smallestSumOfInverseSquares = 99999;
			float scratch;
			MapLocation destination = null;
			MapLocation testLocation = null;
			for (int n = 0; n < directions.length; n++) {
				scratch = 0;
				testLocation = rc.getLocation().add(directions[n], RobotType.SCOUT.strideRadius);
				for (RobotInfo robotInfo : dangerousRobots) {
					scratch += 1 / (Math.pow(robotInfo.getLocation().distanceTo(rc.getLocation()), 2));
				}
				//Only consider it as a move location if we can move one full stride, helps against being pinned down.
				if (rc.canMove(rc.getLocation().directionTo(testLocation)) && scratch < smallestSumOfInverseSquares) {
					destination = testLocation;
					smallestSumOfInverseSquares = scratch;
				}
			}
			rc.setIndicatorLine(rc.getLocation(), destination, 255, 255, 255);
			if(rc.canMove(rc.getLocation().directionTo(testLocation))){
				try {
					rc.move(rc.getLocation().directionTo(testLocation));
				} catch (GameActionException e) {
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
		roam = (Directions.d12()[(int)(Math.random()*12)]);
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
		//Attempt to move straight towards nearest tree
		if(!rc.hasMoved() && rc.canMove(rc.getLocation().directionTo(closestTree))){
			try {
				moveAround(rc.getLocation().directionTo(closestTree));
			} catch (GameActionException e) {
				
			}
		}
		//Try to move as close as possible to tree
		else{
			for(float n = RobotType.SCOUT.strideRadius; n > 0; n -= RobotType.SCOUT.strideRadius/5){
				if(!rc.hasMoved() && rc.canMove(rc.getLocation().directionTo(closestTree), n)){
					try {
						rc.move(rc.getLocation().directionTo(closestTree), n);
					} catch (GameActionException e) {
					}
				}
			}
			//If still blocked, wander around to try to find other path
			if(!rc.hasMoved()){
				try {
					roam();
				} catch (GameActionException e) {
					
				}
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