package team025;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Tank class.
 * 
 * @author Amit Bachchan
 */
public class Tank
    extends Fighter
{

    public Tank(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.tankCount,
            rc.readBroadcast(Channels.tankCount) + 1);
        super.run();
    }
}
