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
        pathId = rc.readBroadcast(Channels.buildPathCount);

        toEnemy = mLocation.directionTo(enemyHQ);

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
        // TODO: Implement
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
            for (int i = 0; i < spawnDirs.length; i++)
            {
                if (rc.isCoreReady() && rc.canSpawn(spawnDirs[i], type))
                {
                    rc.spawn(spawnDirs[i], type);
                    return true;
                }
            }
        }
        return false;
    }
}
