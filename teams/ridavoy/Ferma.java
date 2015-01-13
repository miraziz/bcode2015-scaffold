package ridavoy;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Ferma
    extends Zdaniya
{

    public Ferma(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {
        if (rc.isCoreReady())
        {
            int minerCount = rc.readBroadcast(Channel.minerCount);
            rc.setIndicatorString(1, "Miner count: " + minerCount);
            if (minerCount < Constants.minerLimit)
            {
                this.spawnToEnemy(RobotType.MINER);
            }
        }

        rc.broadcast(Channel.minerCount, 0);
    }
}
