package ridavoy;

import battlecode.common.*;

public abstract class Zdaniya
    extends Soveti
{
    public Zdaniya(RobotController rc)
    {
        super(rc);
        mLocation = rc.getLocation();
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


    boolean spawn(RobotType type)
        throws GameActionException
    {
        if (rc.hasSpawnRequirements(type))
        {
            int count = 0;
            Direction dir = rc.getLocation().directionTo(this.enemyHQ);
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
