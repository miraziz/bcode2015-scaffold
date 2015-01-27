package polkovnik;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Miner factory class.
 * 
 * @author Amit Bachchan
 */
public class MinerFactory
    extends ProductionBuilding
{

    public MinerFactory(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    /**
     * Create a miner whenever possible until a certain limit is reached.
     */
    @Override
    public void run()
        throws GameActionException
    {
        if (rc.isCoreReady())
        {
            int minerCount = rc.readBroadcast(Channels.minerCount);
            rc.setIndicatorString(1, "Miner count: " + minerCount);
            if (minerCount < Constants.minerLimit && Clock.getRoundNum() < 1250)
            {
                spawnToEnemy(RobotType.MINER);
            }
        }

        rc.broadcast(Channels.minerCount, 0);
    }
}
