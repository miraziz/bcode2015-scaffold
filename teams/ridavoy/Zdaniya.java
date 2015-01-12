package ridavoy;

import battlecode.common.*;

public abstract class Zdaniya
    extends Soveti
{
    protected Direction[] spawnDirs;
    protected Direction   toEnemy;


    public Zdaniya(RobotController rc)
    {
        super(rc);
        mLocation = rc.getLocation();

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

        for (int i = 0; i < 8; i++)
        {
            System.out.println("Direction: " + spawnDirs[i]);
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


    boolean spawnToEnemy(RobotType type)
        throws GameActionException
    {
        return spawn(rc.getLocation().directionTo(this.enemyHQ), type);
    }


    boolean spawn(Direction dir, RobotType type)
        throws GameActionException
    {
        if (rc.hasSpawnRequirements(type))
        {
            int count = 0;
            while (!rc.canSpawn(dir, type) && count < 8)
            {
                dir = dir.rotateRight();
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
