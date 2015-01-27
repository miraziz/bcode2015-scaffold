package osnovnoy;

import battlecode.common.*;

public class CombatDrone
    extends Fighter
{
    public CombatDrone(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.droneCount;
        this.setDestination(enemyHQ);
    }


    public void run()
        throws GameActionException
    {
        super.run();
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        if (!attack(nearby))
        {
            micro(nearby);
        }
    }


    protected void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return;
        }
        int enemyCount = 0;
        boolean inAttackRange = false;
        double enemyHealth = 0;
        double allyHealth = rc.getHealth();
        boolean shouldRun = false;
        boolean minersNearby = false;
        int avgX = 0;
        int avgY = 0;
        MapLocation minerLoc = null;
        for (int i = 0; i < nearby.length; ++i)
        {
            RobotInfo r = nearby[i];
            int dist = rc.getLocation().distanceSquaredTo(r.location);
            if (r.team == enemyTeam)
            {
                if (r.type.canAttack() && !r.type.canMine())
                {
                    if (dist <= r.type.attackRadiusSquared)
                    {
                        inAttackRange = true;
                    }
                    if (dist <= r.type.attackRadiusSquared + 4)
                    {
                        enemyHealth += r.health;
                    }
                    if (r.type == RobotType.MISSILE && dist <= 10)
                    {
                        shouldRun = true;
                    }
                }
                if (r.type.canMine())
                {
                    minersNearby = true;
                    if (minerLoc == null
                        || dist < rc.getLocation().distanceSquaredTo(minerLoc))
                    {
                        minerLoc = r.location;
                    }
                }
                if (!r.type.isBuilding)
                {
                    avgX += r.location.x;
                    avgY += r.location.y;
                    enemyCount++;
                }
            }
            else
            {
                if (r.type == RobotType.DRONE && dist <= 15)
                {
                    allyHealth += r.health;
                }
            }
        }
        if (enemyCount == 0)
        {
            this.bugWithCounter();
            return;
        }
        avgX /= enemyCount;
        avgY /= enemyCount;
        MapLocation enemy = new MapLocation(avgX, avgY);
        Direction toEnemy = rc.getLocation().directionTo(enemy);

        if (shouldRun)
        {
            this.setDestination(rc.getLocation().add(toEnemy.opposite()));
        }
        else if (minersNearby && allyHealth >= enemyHealth)
        {
            this.setDestination(minerLoc);
        }
        else if (allyHealth >= enemyHealth * 2)
        {
            if (inAttackRange)
            {
                if (rc.getWeaponDelay() == 3)
                {
                    this.setDestination(rc.getLocation()
                        .add(toEnemy.opposite()));
                }
                else if (rc.getWeaponDelay() >= 1)
                {
                    this.setDestination(rc.getLocation().add(toEnemy));
                }
            }
            else
            {
                this.setDestination(rc.getLocation().add(toEnemy));
            }
        }
        else if (allyHealth < enemyHealth)
        {
            this.setDestination(rc.getLocation().add(toEnemy.opposite()));
        }
        this.bugWithCounter();
    }
}
