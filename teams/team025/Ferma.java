package team025;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Miner factory class.
 * 
 * @author Miraziz
 */
public class Ferma
    extends Proizvodstvennoye
{

    public Ferma(RobotController rc)
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {
        if (rc.isCoreReady())
        {
            int minerCount = rc.readBroadcast(Channels.minerCount);
            rc.setIndicatorString(1, "Miner count: " + minerCount);
            if (minerCount < Constants.minerLimit)
            {
                spawnToEnemy(RobotType.MINER);
            }
        }

        rc.broadcast(Channels.minerCount, 0);
    }
}
