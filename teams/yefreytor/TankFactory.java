package yefreytor;

import battlecode.common.*;

/**
 * Tank factory class.
 * 
 * @author Amit Bachchan
 */
public class TankFactory
    extends ProductionBuilding
{

    public TankFactory(RobotController rc)
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
