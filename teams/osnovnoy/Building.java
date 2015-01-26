package osnovnoy;

import battlecode.common.*;

/**
 * Building super class
 * 
 * @author Amit Bachchan
 */
public abstract class Building
    extends Soveti
{
    protected Direction toEnemy;
    protected int       pathId;


    /**
     * Saves direction to enemy HQ.
     * 
     * @param rc
     */
    public Building(RobotController rc)
        throws GameActionException
    {
        super(rc);

        toEnemy = mLocation.directionTo(enemyHQ);
    }


    /**
     * Transfers supply away from HQ. Supplies nearby units and maintains a
     * minor stock pile.
     */
    @Override
    public void transferSupplies()
        throws GameActionException
    {
        RobotInfo[] nearby =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);
        for (RobotInfo r : nearby)
        {
            if (Clock.getBytecodesLeft() < 550)
            {
                return;
            }
            if (isSupplyingUnit(r.type))
            {
                rc.transferSupplies((int)(rc.getSupplyLevel() * .9), r.location);
            }

        }
        for (RobotInfo r : nearby)
        {
            if (Clock.getBytecodesLeft() < 550)
            {
                return;
            }
            if (r.type.canSpawn() && rc.getSupplyLevel() > r.supplyLevel)
            {
                rc.transferSupplies(
                    (int)((rc.getSupplyLevel() - r.supplyLevel) / 2),
                    r.location);
            }
        }
    }


    /**
     * Spawns a <code>type</code> robot in the enemy HQ's direction.
     * 
     * @param type
     *            Robot to spawn
     * @return True if spawn was successful, false otherwise.
     * @throws GameActionException
     */
    boolean spawnToEnemy(RobotType type)
        throws GameActionException
    {
        return spawn(toEnemy, type);
    }


    /**
     * Spawns a <code>type</code> robot in the direction <code>dir</code>.
     * 
     * @param dir
     *            Direction to spawn to.
     * @param type
     *            Type of robot to spawn.
     * @return True if spawn was successful, false otherwise.
     * @throws GameActionException
     */
    boolean spawn(Direction dir, RobotType type)
        throws GameActionException
    {
        if (rc.hasSpawnRequirements(type) && rc.isCoreReady())
        {
            Direction right = dir;
            Direction left = dir;
            int count = 0;
            while (!rc.canSpawn(dir, type) && count < 8)
            {
                if (count % 2 == 0)
                {
                    right = right.rotateRight();
                    dir = right;
                }
                else
                {
                    left = left.rotateLeft();
                    dir = left;
                }
                count++;
            }
            if (count < 8)
            {
                rc.spawn(dir, type);
                return true;
            }
        }
        return false;
    }
}
