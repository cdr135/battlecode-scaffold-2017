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
		Double moved = move();
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] robotinfo = rc.senseNearbyRobots();

		boolean shouldPlantTree = true;
		{
			float minHP = 51;
			Integer treeID = null;
			for (TreeInfo t : treeinfo) {
				if (t.getTeam().equals(rc.getTeam())) {
					if (rc.canInteractWithTree(t.ID)) {
						if  (t.getHealth() < minHP) {
							minHP = t.getHealth();
							treeID = t.getID();
						}
					} else if (distance(rc.getLocation(), t.location) <= 3.1)
						shouldPlantTree = false;
				}
			}
			if (treeID != null)
				rc.water(treeID);
		}
		if (moved == null) shouldPlantTree = true;	//i cant move
		else {
			for (RobotInfo robot : robotinfo)
				if (robot.getTeam() == rc.getTeam() &&
				robot.getType() == RobotType.ARCHON) {
					if (moved < 0.5)
						shouldPlantTree = false;
				}
		}

		if (shouldPlantTree) for (Direction d : shuffle(direction)) {
			if (rc.canPlantTree(d)) {
				for (Direction di : direction) {
					rc.setIndicatorDot(rc.getLocation().add(di,2), di==d?0:255, 0, di==d?255:0);
				}
				rc.plantTree(d);
				break;
			}
		}

		// find some way to balance economy of scout it works


		if (!builtScout && 3 * robotinfo.length + treeinfo.length < 10) {
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
					rc.buildRobot(RobotType.SCOUT, dir);
					builtScout = true;
					break;
				}
			}
		}
		RobotInfo[] allied = rc.senseNearbyRobots(20, rc.getTeam());
		int numSoldiers = 0;
		for (RobotInfo x: allied){
			if (x.getType().equals(RobotType.SOLDIER)){
				numSoldiers++;
			}
		}
		if (numSoldiers < 3){
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

	private Double move() throws GameActionException {
		Map<Direction, Double> directionWeights = new HashMap<Direction, Double>();
		for (Direction d : Directions.d12()) if(rc.canMove(d)) directionWeights.put(d, 1.);
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld = rc.senseNearbyRobots();

		if (directionWeights.size() > 0) {
			Direction[] directions = directionWeights.keySet().toArray(new Direction[0]);
			for (TreeInfo tree : treeinfo) treecheck: {
				if (!tree.team.equals(rc.getTeam()))
					continue;
				boolean t_hasGardener = hasOtherGardener(tree, nejworld);
				Direction bearing = direction(rc.getLocation(),tree.location);
				//if (rc.canInteractWithTree(tree.ID)) {
				if (t_hasGardener) {
					for (Direction d : directions)
						if (Math.abs(d.degreesBetween(bearing)) < 40)
							directionWeights.put(d, directionWeights.get(d)-1);
						else
							directionWeights.put(d, directionWeights.get(d)-.3+
									Math.abs(d.radiansBetween(bearing))/2);
				} else {
					for (Direction d : directions)
						if (Math.abs(d.degreesBetween(bearing)) > 140)
							directionWeights.put(d, directionWeights.get(d)-1.1);
						else 
							directionWeights.put(d, directionWeights.get(d)-.3+
									Math.abs(d.radiansBetween(bearing))/2 + 9 / (1 +
											d.radiansBetween(bearing))/2);
				}
				/*} else {
					if (!t_hg) {
						for (Direction d : moveDirs.keySet())
							if (Math.abs(d.degreesBetween(b)) > 60)
								moveDirs.put(d, moveDirs.get(d)-
										distance(rc.getLocation(),
												tree.location)/2);
							else moveDirs.put(d, moveDirs.get(d)+
									(75+tree.health)/(95+d.degreesBetween(b)));
					}
				}*/
			}
			for (RobotInfo robot : nejworld){
				if (!(robot.team == rc.getTeam()))
					continue;
				Direction bearing = direction(rc.getLocation(), robot.location);
				switch (robot.type) {
				case ARCHON:
					for (Direction d : directions){
						if (Math.abs(d.degreesBetween(bearing)) < 46)
							directionWeights.put(d, directionWeights.get(d) -
									(3-Math.abs(d.radiansBetween(bearing))/2)/Math.sqrt(
											distance(rc.getLocation(),
													robot.location)));
						else
							directionWeights.put(d, directionWeights.get(d) +
									(Math.abs(d.radiansBetween(bearing))-0.5)/Math.sqrt(
											distance(rc.getLocation(),
													robot.location)));
					}
					break;
				case GARDENER:
					for (Direction d : directions) {
						// do something
					}
					break;
				default:
					break;
				}
			}


			double min = Double.MAX_VALUE, sum = 0;
			for (Direction d : directions) {
				sum += directionWeights.get(d);
				if (directionWeights.get(d) < min) {
					min = directionWeights.get(d);
				}
			}

			if (min < sum / 99){
				for (Direction d : directions) {
					directionWeights.put(d, directionWeights.get(d) - min);
				}
				sum -= directions.length * min;
				for (Direction d : directionWeights.keySet())
					directionWeights.put(d, (directionWeights.get(d)) + sum / 97);
				sum += directions.length/97 * sum;
			}

			for (Direction d : directionWeights.keySet())
				rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(d,
						(float) Math.min(1 + Math.abs(directionWeights.get(d).floatValue()),4)),
						125*(int)(1-Math.signum(directionWeights.get(d))),
						125*(int)(Math.signum(directionWeights.get(d))+1),
						0);

			double[] threshold = new double[directions.length];
			threshold[0] = directionWeights.get(directions[0])/sum;
			int i;
			for (i = 1; i < threshold.length; i++)
				threshold[i] = threshold[i-1] + directionWeights.get(directions[i])/sum;
			double rand = Math.random();
			System.out.println(threshold[threshold.length-1]);
			while (i > 0) {
				if (threshold[--i] > rand)
					if (!rc.hasMoved()){
						rc.move(directions[i]);
						return min;		//returns min if you moved
					}
			}
		}
		/**/
		return null;		//didn't move
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