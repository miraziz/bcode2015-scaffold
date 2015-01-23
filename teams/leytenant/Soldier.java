package leytenant;

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
        mTypeChannel = Channels.soldierCount;
    }


    public void run()
        throws GameActionException
    {
        super.run();

    }

}
