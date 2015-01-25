package kapitan;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Basher
    extends Fighter
{

    public Basher(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.basherCount;
    }


    public void run()
        throws GameActionException
    {
        super.run();
    }

}
