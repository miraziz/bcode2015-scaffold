package basherbot;

import battlecode.common.*;

/**
 * Soldier class.
 * 
 * @author Amit Bachchan
 */
public class Soldier
    extends Fighter
{

    public Soldier(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.soldierCount;
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
            this.avoidTowers = false;
            committed = true;
            this.setDestination(enemyHQ);
        }
        else
        {
            this.setDestination(getLocation(Channels.rallyLoc));
        }
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        if (!attack(nearby))
        {
            micro(nearby);
        }
    }

}
