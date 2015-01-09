package minerdraft;

import battlecode.common.*;

public class RobotPlayer {

	public static void run(RobotController rc) {
		try {
			BaseBot bot = null;
			if (rc.getType() == RobotType.HQ) {
				bot = new HQ(rc);
			} else if (rc.getType() == RobotType.TOWER) {
				bot = new Tower(rc);
			} else {
				bot = new Tower(rc);
			}
			while (true) {
				bot.runBot();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

abstract class BaseBot {
	protected RobotController rc;
	protected Team myTeam;
	protected Team enemyTeam;
	protected MapLocation allyHQ;
	protected MapLocation enemyHQ;
	protected MapLocation[] enemyTowers;
	protected MapLocation[] allyTowers;

	abstract void runBot() throws GameActionException;

	public BaseBot(RobotController myRc) throws GameActionException {
		rc = myRc;
		allyHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();
		enemyTowers = rc.senseEnemyTowerLocations();
		allyTowers = rc.senseTowerLocations();
	}

	public boolean spawnUnit(RobotType type) throws GameActionException {
		Direction dir = Direction.SOUTH;
		int count = 0;
		if (!rc.isCoreReady()) {
			return false;
		}
		while (!rc.canSpawn(dir, type) && count < 8) {
			dir = dir.rotateRight();
			count++;
		}
		if (count < 8) {
			rc.spawn(dir, type);
			return true;
		}
		return false;
	}

	void transferSupplies() throws GameActionException {
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),
				GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
		double lowestSupply = rc.getSupplyLevel();
		double transferAmount = 0;
		MapLocation suppliesToThisLocation = null;
		for (RobotInfo ri : nearbyAllies) {
			if (ri.supplyLevel < lowestSupply) {
				lowestSupply = ri.supplyLevel;
				transferAmount = (rc.getSupplyLevel() - ri.supplyLevel) / 2;
				suppliesToThisLocation = ri.location;
			}
		}
		if (suppliesToThisLocation != null) {
			rc.transferSupplies((int) transferAmount, suppliesToThisLocation);
		}
	}

	public void attack() throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		RobotInfo target = null;

		if (nearbyEnemies.length > 0 && rc.isWeaponReady()) {
			for (RobotInfo ri : nearbyEnemies) {
				if (target == null || ri.health < target.health) {
					target = ri;
				}
			}
			if (rc.canAttackLocation(target.location)) {
				rc.attackLocation(target.location);
			}
		}
	}
}