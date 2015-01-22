package leytenant;

import battlecode.common.*;

public class CombatDrone
    extends Fighter
{

    public CombatDrone(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        super.run();
    }


    protected boolean bug()
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return false;
        }
        dest = getLocation(Channels.rallyLoc);
        Direction dir =
            this.getFreeStrafeDirection(rc.getLocation().directionTo(dest));
        if (dir == null)
        {
            return false;
        }
        else
        {
            rc.move(dir);
            return true;
        }
    }
}
