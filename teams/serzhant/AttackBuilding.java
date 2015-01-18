package serzhant;

import battlecode.common.*;

/**
 * Attacking building class.
 * 
 * @author Amit Bachchan
 */
public abstract class AttackBuilding
    extends Building
{

    /**
     * Broadcasts the building's score.
     * 
     * @param rc
     * @throws GameActionException
     */
    public AttackBuilding(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    /**
     * Attempts to attack and then transfers supplies to nearby allies.
     */
    @Override
    public void run()
        throws GameActionException
    {
        this.findDefenseSpot();
        this.attack();
    }


    public void findDefenseSpot()
        throws GameActionException
    {
        RobotInfo[] nearby =
            rc.senseNearbyRobots(
                rc.getType().sensorRadiusSquared,
                this.enemyTeam);
        if (nearby.length == 0)
        {
            return;
        }
        int enemyHealth = 0;
        int avgX = 0;
        int avgY = 0;
        for (RobotInfo r : nearby)
        {
            if (r.type == RobotType.HQ || r.type == RobotType.TOWER)
            {
                continue;
            }
            enemyHealth += r.health;
            avgX += r.location.x;
            avgY += r.location.y;
        }
        if (rc.readBroadcast(Channels.highestEnemyHealth) < enemyHealth)
        {
            rc.broadcast(Channels.highestEnemyHealth, enemyHealth);
            avgX /= nearby.length;
            avgY /= nearby.length;
            broadcastLocation(Channels.rallyLoc, new MapLocation(avgX, avgY));
        }
    }
}
