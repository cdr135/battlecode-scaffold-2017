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
		Double mr = move();
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld = rc.senseNearbyRobots();

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
					} else if (distance(rc.getLocation(), t.location) <= 3.1)
						spt = false;
				}
			}
			if (tid != null)
				rc.water(tid);
		}
		if (mr == null) spt = true;	//i cant move
		else {
			for (RobotInfo robot : nejworld)
				if (robot.getTeam() == rc.getTeam() &&
				robot.getType() == RobotType.ARCHON) {
					if (mr < 0.5)
						spt = false;
				}
		}

		if (spt) for (Direction d : shuffle(direction)) {
			if (rc.canPlantTree(d)) {
				for (Direction di : direction) {
					rc.setIndicatorDot(rc.getLocation().add(di,2), di==d?0:255, 0, di==d?255:0);
				}
				rc.plantTree(d);
				break;
			}
		}

		// find some way to balance economy of scout it works


		if (!builtScout && 4 * nejworld.length + treeinfo.length < 27) {
			for (Direction dir : shuffle(direction)) {
				if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
					rc.buildRobot(RobotType.SCOUT, dir);
					builtScout = true;
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
		double mp = 1;
		Map<Direction, Double> moveDirs = new HashMap<Direction, Double>();
		for (Direction d : Directions.d12()) if(rc.canMove(d)) moveDirs.put(d, 1.);
		TreeInfo[] treeinfo = rc.senseNearbyTrees();
		RobotInfo[] nejworld = rc.senseNearbyRobots();

		if (moveDirs.size() > 0) {
			for (TreeInfo tree : treeinfo) treecheck: {
				if (!tree.team.equals(rc.getTeam()))
					continue;
				boolean t_hg = hasOtherGardener(tree, nejworld);
				Direction b = direction(rc.getLocation(),tree.location);
				if (rc.canInteractWithTree(tree.ID)) {
					if (t_hg) {
						mp += 0.2;
						for (Direction d : moveDirs.keySet())
							if (Math.abs(d.degreesBetween(b)) < 40)
								moveDirs.put(d, moveDirs.get(d)-1);
							else
								moveDirs.put(d, moveDirs.get(d)-.5+
										d.radiansBetween(b)/2);
					} else {
						mp = Math.sqrt(mp * 0.6);
						for (Direction d : moveDirs.keySet())
							if (Math.abs(d.degreesBetween(b)) > 140)
								moveDirs.put(d, moveDirs.get(d)-1);
							else 
								moveDirs.put(d, moveDirs.get(d)-.5+
										d.radiansBetween(b)/2);
					}
				} else {
					if (!t_hg) {
						mp += 0.1;
						for (Direction d : moveDirs.keySet())
							if (Math.abs(d.degreesBetween(b)) > 60)
								moveDirs.put(d, moveDirs.get(d)-
										distance(rc.getLocation(),
												tree.location)/2);
							else moveDirs.put(d, moveDirs.get(d)+
									(75+tree.health)/(95+d.degreesBetween(b)));
					}
				}
			}
			for (RobotInfo robot : nejworld)
				if (robot.type == RobotType.ARCHON)
					for (Direction d : moveDirs.keySet()){
						Direction b = direction(rc.getLocation(),robot.location);
						if (Math.abs(d.degreesBetween(b)) < 30)
							moveDirs.put(d, moveDirs.get(d) -
									(3-d.radiansBetween(b))/Math.sqrt(
											distance(rc.getLocation(),
													robot.location)));
						else
							moveDirs.put(d, moveDirs.get(d) -
									(d.radiansBetween(b)-0.5)/Math.sqrt(
											distance(rc.getLocation(),
													robot.location)));
					}

			for (Direction d : moveDirs.keySet())
				rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(d, 1+moveDirs.get(d).floatValue()), 0, 0, 0);


			double m = Double.MAX_VALUE, s = 0;
			Direction md = null;
			for (Double d : moveDirs.values()) s += d;
			Direction[] mvdir = moveDirs.keySet().toArray(new Direction[0]);
			if (s < 0) {
				for (Direction d : mvdir)
					moveDirs.put(d, -1*moveDirs.get(d));
				s *= -1;
			}
			for (Direction dir : shuffle(mvdir))
				if (moveDirs.get(dir) < m) {
					md = dir;
					m = moveDirs.get(dir);
				}
			s -= moveDirs.size() * m;
			if (m < s / 99) {
				for (Direction d : moveDirs.keySet())
					moveDirs.put(d, (moveDirs.get(d) - m) + s / 99);
			}
			double[] thr = new double[moveDirs.size()];
			thr[0] = moveDirs.get(mvdir[0])/s;
			int i;
			for (i = 1; i < thr.length; i++)
				thr[i] = thr[i-1] + moveDirs.get(mvdir[i])/s;
			double rand = Math.random();
			while (i > 0) {
				if (thr[--i] > rand)
					if (!rc.hasMoved()){
						if (Math.random() < mp)
							rc.move(mvdir[i]);
						return m;
					}
			}
		}
		/**/
		return null;
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
}