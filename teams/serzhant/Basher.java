package serzhant;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Basher
    extends Fighter
{

    public Basher(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.basherCount,
            rc.readBroadcast(Channels.basherCount) + 1);
        super.run();
    }

}
