package supremecommander;

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
        mTypeChannel = Channels.tankCount;
    }


    public void run()
        throws GameActionException
    {
        super.run();
    }
}
