package general;

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
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
        if (!attack(nearby))
        {
            micro(nearby);
        }
    }


    protected void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        RobotInfo toChase = null;
        for (int i = 0; i < nearby.length; i++)
        {
            RobotInfo cur = nearby[i];
            int dist = rc.getLocation().distanceSquaredTo(cur.location);
            if (dist <= cur.type.attackRadiusSquared + 4
                || (dist <= 35 && cur.type == RobotType.LAUNCHER))
            {
                if (toChase == null
                    || rc.getLocation().distanceSquaredTo(toChase.location) > dist)
                {
                    toChase = cur;
                }
            }
            else if (toChase == null && cur.type.canBuild())
            {
                toChase = cur;
            }
            else if (toChase == null && cur.type.isBuilding
                && cur.type.canAttack())
            {
                toChase = cur;
            }
        }
        if (toChase != null)
        {
            this.setDestination(toChase.location);
            if (!committed)
            {
                if (rc.readBroadcast(Channels.highestEnemyHealth) < 100)
                {
                    rc.broadcast(Channels.highestEnemyHealth, 100);
                    broadcastLocation(
                        Channels.highestEnemyHealthLoc,
                        toChase.location);
                }
            }
        }
        this.bugWithCounter();
    }

}
