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


    public boolean attack()
        throws GameActionException
    {
        if (!rc.isWeaponReady())
        {
            return false;
        }
        int attackRadius = 24;
        if (rc.getType() == RobotType.HQ)
        {
            allyTowers = rc.senseTowerLocations();
            if (allyTowers.length >= 2)
            {
                attackRadius = 35;
            }
        }
        RobotInfo[] nearbyEnemies =
            rc.senseNearbyRobots(attackRadius - 1, enemyTeam);
        if (nearbyEnemies.length == 0)
        {
            rc.setIndicatorString(0, "Dont see anyone");
            return false;
        }

        rc.setIndicatorString(0, "See People");
        double lowestHealth = nearbyEnemies[0].health;
        MapLocation loc = nearbyEnemies[0].location;
        for (RobotInfo i : nearbyEnemies)
        {
            if (i.health < lowestHealth)
            {
                lowestHealth = i.health;
                loc = i.location;
            }
        }
        if (loc != null && rc.canAttackLocation(loc))
        {
            rc.attackLocation(loc);
            return true;
        }
        return false;
    }
}
