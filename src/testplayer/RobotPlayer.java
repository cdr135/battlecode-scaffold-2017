package testplayer;

import battlecode.common.*;

public class RobotPlayer {

	private static Brain brain;

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies! @throws
	 **/

	public static void run(RobotController rc) {

		switch (rc.getType()) {
		case ARCHON:
			brain = new ArchonBrain();
			break;

		case SOLDIER:
			brain = new SoldierBrain();
			break;

		case GARDENER:
			brain = new GardenerBrain();
			break;

		case SCOUT:
			brain = new ScoutBrain();
			break;

		case TANK:
			brain = new TankBrain();
			break;
		case LUMBERJACK:
			brain = new LumberjackBrain();
			break;

		default:
			break;
		}

		brain.run(rc);
	}

}
