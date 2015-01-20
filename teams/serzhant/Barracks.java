package serzhant;

import battlecode.common.*;

/**
 * Barracks class.
 * 
 * @author Amit Bachchan
 */
public class Barracks
    extends ProductionBuilding
{

    public Barracks(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    /**
     * Builds a soldier whenever possible.
     */
    @Override
    public void run()
        throws GameActionException
    {
        // TODO smarter barracks
        if (rc.readBroadcast(Channels.shouldSpawnSoldier) == 1)
        {
            spawnToEnemy(RobotType.SOLDIER);
        }
        else if (rc.readBroadcast(Channels.shouldSpawnBasher) == 1)
        {
            spawnToEnemy(RobotType.BASHER);
        }
    }

}
