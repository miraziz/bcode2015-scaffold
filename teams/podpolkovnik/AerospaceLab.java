package podpolkovnik;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class AerospaceLab
    extends ProductionBuilding
{

    public AerospaceLab(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.aerospaceCount,
            rc.readBroadcast(Channels.aerospaceCount) + 1);
        this.spawnToEnemy(RobotType.LAUNCHER);
    }

}
