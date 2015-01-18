package yefreytor;

import battlecode.common.*;

/**
 * Building super class
 * 
 * @author Amit Bachchan
 */
public abstract class Zdaniya
    extends Soveti
{
    protected Direction toEnemy;
    protected int       pathId;


    /**
     * Saves direction to enemy HQ.
     * 
     * @param rc
     */
    public Zdaniya(RobotController rc)
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
        int totSupply = (int)rc.getSupplyLevel();
        if (totSupply > 0)
        {
            // Checks the broadcast channel to see if there is another building
// in
            // the supply chain.
            MapLocation supplyTo = getLocation(Channels.buildPath + pathId + 1);
            if (supplyTo != null
                && mLocation.distanceSquaredTo(supplyTo) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED)
            {
                RobotInfo supplyee = rc.senseRobotAtLocation(supplyTo);
                if (supplyee != null && supplyee.team == myTeam)
                {
                    rc.transferSupplies(totSupply, supplyTo);
                    return;
                }
            }

            // TODO Improve distribution to give out all supply faster. Include
// supply divided by min(num of nearby mobile units, number of remaining bytes
// codes / 511)
            // TODO ake 511 a constant variable

            // Evenly distributes most of the remaining supply
            RobotInfo[] nearbyAllies =
                rc.senseNearbyRobots(
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    myTeam);
            int unitNum = nearbyAllies.length;
            if (unitNum > 0)
            {
                int unitSupply = totSupply / unitNum;
                for (RobotInfo robot : nearbyAllies)
                {
                    // TODO Improve beaver restrictions
                    if (isAttackingUnit(robot.type)
                        && robot.type != RobotType.BEAVER)
                    {
                        if (Clock.getBytecodesLeft() < 511)
                        {
                            return;
                        }
                        rc.transferSupplies(unitSupply, robot.location);
                        totSupply -= unitSupply;
                    }
                    else
                    {
                        unitSupply = totSupply / unitNum;
                    }
                    unitNum--;
                }

                for (RobotInfo robot : nearbyAllies)
                {
                    if (!isAttackingUnit(robot.type))
                    {
                        if (Clock.getBytecodesLeft() < 511)
                        {
                            return;
                        }
                        rc.transferSupplies(unitSupply, robot.location);
                    }
                }
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
            while (!rc.canSpawn(dir, type))
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
            }
        }
        return false;
    }
}
