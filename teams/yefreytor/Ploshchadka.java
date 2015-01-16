package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Helipad class.
 * 
 * @author Amit Bachchan
 */
public class Ploshchadka
    extends Proizvodstvennoye
{

    public Ploshchadka(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    /**
     * Builds a drone whenever possible.
     */
    @Override
    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.helipadCount,
            rc.readBroadcast(Channels.helipadCount) + 1);
        spawnToEnemy(RobotType.DRONE);
    }

}
