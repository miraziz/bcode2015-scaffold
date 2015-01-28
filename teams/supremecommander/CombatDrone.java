package supremecommander;

import battlecode.common.*;

public class CombatDrone
    extends Fighter
{
    public CombatDrone(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.droneCount;
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
        int avgX = 0;
        int avgY = 0;
        int enemyHealth = 0;
        double allyHealth = rc.getHealth();
        Direction enemyMissileDir = null;
        boolean inDanger = false;
        RobotInfo ri = null;
        boolean attacking = false;
        RobotInfo closest = null;
        Integer minDistance = null;
        for (int i = 0; i < nearby.length; i++)
        {
            ri = nearby[i];
            if (ri.team == enemyTeam)
            {
                if (ri.type == RobotType.MISSILE
                    && rc.getLocation().distanceSquaredTo(ri.location) < 10)
                {
                    enemyMissileDir = ri.location.directionTo(rc.getLocation());
                }
                else if (ri.type.canAttack() && !ri.type.isBuilding)
                {
                    if (!ri.type.canMine())
                    {
                        enemyHealth += ri.health;
                    }
                    avgX += ri.location.x;
                    avgY += ri.location.y;
                    enemyCount++;
                    int dist = rc.getLocation().distanceSquaredTo(ri.location);
                    if (dist < ri.type.attackRadiusSquared)
                    {
                        inDanger = true;
                    }
                    if (minDistance == null || dist < minDistance)
                    {
                        minDistance = dist;
                        closest = ri;
                    }
                }
            }
        }
        for (int i = 0; i < nearby.length; i++)
        {
            ri = nearby[i];
            if (ri.team == myTeam)
            {
                if (ri.type.canAttack() && !ri.type.isBuilding
                    && ri.type.canMine())
                {
                    allyHealth += ri.health;
                }
            }
        }
        if (enemyCount == 0)
        {
            this.setDestination(getLocation(Channels.rallyLoc));
        }
        else
        {
            avgX /= enemyCount;
            avgY /= enemyCount;
            this.setDestination(new MapLocation(avgX, avgY));
            if (enemyMissileDir != null)
            {
                Direction clearPath = getFreeStrafeDirection(enemyMissileDir);
                this.setDestination(rc.getLocation().add(clearPath));
            }
            if (allyHealth > enemyHealth)
            {
                MapLocation enemy = new MapLocation(avgX, avgY);
                this.setDestination(enemy);
                attacking = true;
            }
            else if (inDanger)
            {
                Direction away =
                    this.getFreeStrafeDirection(new MapLocation(avgX, avgY)
                        .directionTo(rc.getLocation()));
                this.setDestination(rc.getLocation().add(away));
            }
        }
        Direction towards =
            this.getFreeStrafeDirection(rc.getLocation().directionTo(dest));
        if (!attacking
            && closest != null
            && rc.getLocation().add(towards)
                .distanceSquaredTo(closest.location) <= closest.type.attackRadiusSquared)
        {
            return;
        }
        bugWithCounter();
    }
}
