package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Soldier class.
 * 
 * @author Amit Bachchan
 */
public class Prizyvnik
    extends Boyets
{

    public Prizyvnik(RobotController rc)
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
