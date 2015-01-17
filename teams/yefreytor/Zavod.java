package yefreytor;

import battlecode.common.*;

/**
 * Tank factory class.
 * 
 * @author Amit Bachchan
 */
public class Zavod
    extends Proizvodstvennoye
{

    public Zavod(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.tankFactoryCount,
            rc.readBroadcast(Channels.tankFactoryCount) + 1);
        spawnToEnemy(RobotType.TANK);
    }

}
