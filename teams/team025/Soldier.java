package team025;

import java.util.LinkedList;
import battlecode.common.*;

/**
 * Soldier class.
 * 
 * @author Amit Bachchan
 */
public class Soldier
    extends Fighter
{

    public Soldier(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.soldierCount,
            rc.readBroadcast(Channels.soldierCount) + 1);
        super.run();

    }

}
