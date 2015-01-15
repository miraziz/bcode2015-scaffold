package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Helipad class.
 * 
 * @author Miraziz
 */
public class Ploshchadka
    extends Proizvodstvennoye
{

    public Ploshchadka(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


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
