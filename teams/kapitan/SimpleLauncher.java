package kapitan;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class SimpleLauncher
    extends Proletariat
{

    public SimpleLauncher(RobotController rc)
        throws GameActionException
    {
        super(rc);
        setDestination(enemyHQ);
    }


    public void run()
        throws GameActionException
    {
        super.run();
        RobotInfo[] enemies =
            rc.senseNearbyRobots(Constants.MISSILE_MAX_RANGE_SQUARED, enemyTeam);
        attackEnemies(enemies);
        if (rc.isCoreReady())
        {
            if (!runAway(enemies))
            {
                bug();
            }
        }
        else
        {
            enemyTowers = rc.senseEnemyTowerLocations();
        }
    }


    private void attackEnemies(RobotInfo[] enemies)
        throws GameActionException
    {
        if (enemies.length == 0 || rc.getMissileCount() == 0)
        {
            return;
        }
        RobotInfo closest = null;
        int minDistance = 0;
        int maxPriority = 0;
        for (int i = 0; i < enemies.length; i++)
        {
            int priority = getMissilePriority(enemies[i].type);
            int dist = rc.getLocation().distanceSquaredTo(enemies[i].location);
            if (priority > maxPriority)
            {
                maxPriority = priority;
                minDistance = dist;
                closest = enemies[i];
            }
            else if (dist < minDistance)
            {
                minDistance = dist;
                closest = enemies[i];
            }
        }
        if (closest == null)
        {
            return;
        }
        Direction spawnDir = getMissileSpawnDir(closest.location);
        if (spawnDir == null)
        {
            return;
        }
        else
        {
            rc.launchMissile(spawnDir);
            int channel = getLocChannel(rc.getLocation().add(spawnDir));
            if (closest.type == RobotType.LAUNCHER)
            {
                broadcastLocation(channel, closest.location.add(spawnDir));
            }
            else
            {
                broadcastLocation(channel, closest.location);
            }
        }
    }


    protected Direction getMissileSpawnDir(MapLocation target)
    {
        Direction dir = rc.getLocation().directionTo(target);
        if (rc.isPathable(RobotType.MISSILE, rc.getLocation().add(dir)))
        {
            return dir;
        }
        else if (rc.isPathable(
            RobotType.MISSILE,
            rc.getLocation().add(dir.rotateRight())))
        {
            return dir.rotateRight();
        }
        else if (rc.isPathable(
            RobotType.MISSILE,
            rc.getLocation().add(dir.rotateLeft())))
        {
            return dir.rotateLeft();
        }
        return null;
    }


    protected boolean runAway(RobotInfo[] enemies)
        throws GameActionException
    {
        if (enemies.length == 0)
        {
            return false;
        }
        int avgX = 0;
        int avgY = 0;
        boolean shouldRun = false;
        int enemyCount = 0;
        for (RobotInfo r : enemies)
        {
            if (r.type.canAttack()
                && rc.getLocation().distanceSquaredTo(r.location) <= RobotType.COMMANDER.sensorRadiusSquared)
            {
                shouldRun = true;
                avgX += r.location.x;
                avgY += r.location.y;
                enemyCount++;
            }
        }
        if (shouldRun)
        {
            avgX /= enemyCount;
            avgY /= enemyCount;
            Direction runDir =
                this.getFreeStrafeDirection(rc.getLocation()
                    .directionTo(new MapLocation(avgX, avgY)).opposite());
            if (runDir != null)
            {
                rc.move(runDir);
            }
        }
        return true;
    }


    private int getMissilePriority(RobotType type)
    {
        int priority = 1;
        if (type.canAttack())
        {
            priority = 6;
            if (type.canMine())
            {
                priority = 5;
            }
        }
        if (type == RobotType.MISSILE)
        {
            priority = 0;
        }
        return priority;
    }
}
