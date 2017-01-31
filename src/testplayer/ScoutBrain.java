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
		BulletInfo[] bullets = rc.senseNearbyBullets();
		move();
		
		}
	private void move() throws GameActionException{
		//if we're already close to a gardener just continue shooting
		RobotInfo[] nearby = rc.senseNearbyRobots();
		boolean nearbyGardener = false;
		MapLocation gardener = null;
		for (RobotInfo x : nearby){
			if (x.team.equals(rc.getTeam().opponent()) && x.type.equals(RobotType.GARDENER)){
				if (rc.getLocation().distanceTo(x.getLocation()) < 1){
					nearbyGardener = true;
					gardener = x.getLocation();
				}
			}
		}
		if (nearbyGardener == true){
			rc.move(rc.getLocation().directionTo(gardener));
			rc.fireSingleShot(rc.getLocation().directionTo(gardener));
			return;
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
			return;
		}
		//try to dodge anything killing us
		if (rc.senseNearbyBullets().length!= 0){
			dodge();
			return;
		}
		
		roam();
	}
	
	private void roam() throws GameActionException{
		
	}
	private void dodge() throws GameActionException{
		BulletInfo[] bullets = rc.senseNearbyBullets();
		ArrayList<BulletInfo> dangerousBullets=  new ArrayList<BulletInfo>();
		for (BulletInfo x : bullets){
			if (x.getLocation().distanceTo(rc.getLocation())< 5 ){
				Direction angleToBullet = rc.getLocation().directionTo(x.getLocation());
				float angleBetween = x.getDir().degreesBetween(angleToBullet);
				if (angleBetween < 90){
					dangerousBullets.add(x);
				}
			}
		
		}
		if (dangerousBullets.size() == 0){
			return;
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
				rc.move(dir);
			}
			else{
				rc.move(dir2);
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
			rc.move(destination);
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
						
					}
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
		//Attempt to move straight towards nearest tree
		if(rc.canMove(rc.getLocation().directionTo(closestTree))){
			try {
				rc.move(rc.getLocation().directionTo(closestTree));
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//If still blocked, wander around to try to find other path
			if(!rc.hasMoved())
				jiggle();
		}
	}
	//moves scout in random direction
	public void jiggle(){
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