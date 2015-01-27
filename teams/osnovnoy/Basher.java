package osnovnoy;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Basher
    extends Fighter
{

    public Basher(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.basherCount;
    }


    public void run()
        throws GameActionException
    {
        super.run();

        attacking = rc.readBroadcast(Channels.attacking) == 1;
        rc.setIndicatorString(0, "Traveling to: "
            + getLocation(Channels.rallyLoc));
        if (attacking || committed)
        {
            committed = true;
            this.setDestination(enemyHQ);
        }
        else
        {
            this.setDestination(getLocation(Channels.rallyLoc));
        }
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        micro(nearby);
    }

}
