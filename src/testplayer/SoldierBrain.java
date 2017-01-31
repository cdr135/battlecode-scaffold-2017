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
	private Direction roam;
	// private List<MapLocation> knownEnemyArchons;

	private Map<Integer, RobotInfo> robots;

	private boolean isLeader;

	private void runTurn() throws GameActionException {
		//TODO: add dodging
		//fires triad at closest robot if <4 away
		move();
		if(rc.canFireTriadShot()) {
			RobotInfo[] enemies = rc.senseNearbyRobots((float)radius,rc.getTeam().opponent());
			if(enemies.length>0) {
				RobotInfo closest = enemies[0];
				double length = rc.getLocation().distanceTo(closest.getLocation());
				for(RobotInfo r: enemies) {
					double newLength = rc.getLocation().distanceTo(r.getLocation());
					if(newLength<length) {
						closest=r;
						length=newLength;
					}
				}
				if(length<9) {
					rc.fireTriadShot(rc.getLocation().directionTo(closest.getLocation()));
				}
			}
		}
			
		}
	private void move() throws GameActionException{
		if (rc.senseNearbyBullets().length!= 0){
			dodge();
		}
		RobotInfo[] hostile = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam());
		RobotInfo closest = null;
		for (RobotInfo x: hostile){
			if (x.getType().equals(RobotType.SOLDIER) || x.getType().equals(RobotType.LUMBERJACK)){
				if (closest == null){
					closest = x;
				}
				else{
					if (rc.getLocation().distanceTo(x.getLocation()) < rc.getLocation().distanceTo(closest.getLocation())){
						closest = x;
					}
				}

			}
			
		}
		if (closest == null){
			roam();
		}
		else{
			if (rc.getLocation().distanceTo(closest.getLocation())< 5){
				moveAround(rc.getLocation().directionTo(closest.location).opposite());
			}
			else if (rc.getLocation().distanceTo(closest.getLocation()) > 7.5){
				moveAround(rc.getLocation().directionTo(closest.location));
			}
			else{
				moveAround(rc.getLocation().directionTo(closest.location).rotateRightDegrees(90));
			}
			
		}
		if (!rc.hasMoved()){
			roam();
		}
		
	}
	private void roam() throws GameActionException{
		if (!rc.hasMoved()){
			for (int i = 0; i < 360; i++){
				roam.rotateRightDegrees(1);
				if (rc.canMove(roam)){
					rc.move(roam);
					break;
				}
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