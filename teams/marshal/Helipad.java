package marshal;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Helipad class.
 * 
 * @author Amit Bachchan
 */
public class Helipad
    extends ProductionBuilding
{

    public Helipad(RobotController rc)
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
        int shouldSpawnDrone = rc.readBroadcast(Channels.shouldSpawnDrone);
        rc.setIndicatorString(0, "Should spawn drone: " + shouldSpawnDrone);
        if (shouldSpawnDrone == 1)
        {
            spawnToEnemy(RobotType.DRONE);
        }
    }

}
