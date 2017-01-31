package testplayer;

import java.util.*;
import battlecode.common.*;
import testplayer.res.*;

@SuppressWarnings("unused")
public class LumberjackBrain implements Brain {

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
		//try to dodge anything killing us
		if (rc.senseNearbyBullets().length!= 0){
			dodge();
		}
		//looks for trees to chop
		TreeInfo[] trees = rc.senseNearbyTrees();
		if(trees.length>0){
			TreeInfo target = null;
			boolean robot = false;
			boolean bullets = false;
			int bulletCount=0;
			float distance = rc.getLocation().distanceSquaredTo(trees[0].getLocation());
			//prioritizes robot trees over bullet trees over regular trees
			for(TreeInfo t: trees){
				if(t.getContainedRobot()!=null){
					robot = true;
					target = t;
				}
				else if(!robot &&bullets&&t.getContainedBullets()>bulletCount){
					bulletCount=t.getContainedBullets();
					target=t;
				}
				else if(!robot && t.getContainedBullets()>0) {
					bullets=true;
					bulletCount=t.getContainedBullets();
					target=t;
				}
				else if(!robot && !bullets){
					if(rc.getLocation().distanceSquaredTo(t.location)<distance){
						distance=rc.getLocation().distanceSquaredTo(t.location);
						target=t;
					}
				}
			}
			if(rc.getLocation().distanceSquaredTo(target.location)>1) {
				moveAround(rc.getLocation().directionTo(target.location));
			}
			else if(rc.canChop(target.location))
				rc.chop(target.location);
				
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