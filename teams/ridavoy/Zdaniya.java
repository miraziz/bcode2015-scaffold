package ridavoy;

import battlecode.common.*;

/**
 * Building super class
 * 
 * @author Miraziz
 */
public abstract class Zdaniya
    extends Soveti
{
    protected Direction[] spawnDirs;
    protected Direction   toEnemy;
    protected int         pathId;


    /**
     * Saves direction to enemy HQ and sets spawnDirs to be a radial pattern
     * rotating away from the enemy HQ's direction.
     * 
     * @param rc
     */
    public Zdaniya(RobotController rc)
        throws GameActionException
    {
        super(rc);

        toEnemy = mLocation.directionTo(enemyHQ);
        rc.setIndicatorString(1, "My ID: " + pathId);
        spawnDirs = new Direction[8];
        spawnDirs[0] = toEnemy;
        spawnDirs[1] = toEnemy.rotateRight();
        for (int i = 2; i < 8; i++)
        {
            if (i % 2 == 0)
            {
                spawnDirs[i] = spawnDirs[i - 2].rotateLeft();
            }
            else
            {
                spawnDirs[i] = spawnDirs[i - 2].rotateRight();
            }
        }
    }


    /**
     * Transfers supply away from HQ. Supplies nearby units and maintains a
     * minor stock pile.
     */
    @Override
    public void transferSupplies()
        throws GameActionException
    {
        double totSupply = rc.getSupplyLevel();

        // Checks the broadcast channel to see if there is another building in
        // the supply chain.
        MapLocation supplyTo = getLocation(Channels.buildPath + pathId + 1);
        if (supplyTo != null
            && mLocation.distanceSquaredTo(supplyTo) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED)
        {
            RobotInfo supplyee = rc.senseRobotAtLocation(supplyTo);
            if (supplyee != null && supplyee.team == myTeam)
            {
                rc.transferSupplies(
                    (int)(totSupply * Constants.SUPPLY_CHAIN_PERC),
                    supplyTo);
            }
        }

        totSupply = rc.getSupplyLevel();
        // Evenly distributes most of the remaining supply
        RobotInfo[] nearbyAllies =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);
        if (nearbyAllies.length > 0)
        {
            int unitSupply =
                (int)(totSupply * Constants.SUPPLY_CHAIN_PERC / nearbyAllies.length);
            for (RobotInfo robot : nearbyAllies)
            {
                if (isAttackingUnit(robot.type)
                    && robot.type != RobotType.BEAVER)
                {
                    if (Clock.getBytecodesLeft() < 511)
                    {
                        return;
                    }
                    rc.setIndicatorString(0, "Round: " + Clock.getRoundNum()
                        + ", Location: " + robot.location);
                    rc.transferSupplies(unitSupply, robot.location);
                }
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
        return spawn(rc.getLocation().directionTo(this.enemyHQ), type);
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
