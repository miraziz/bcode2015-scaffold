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
        if (rc.getMissileCount() == 0)
        {
            return;
        }

        MapLocation closestLoc = null;
        RobotType closestType = null;
        int minDistance = 100;
        int maxPriority = 1;
        int enemyNum = enemies.length;
        if (enemyNum > 0)
        {
            for (int i = 0; i < enemyNum; ++i)
            {
                int priority = getMissilePriority(enemies[i].type);
                int dist = mLocation.distanceSquaredTo(enemies[i].location);
                if (priority > maxPriority)
                {
                    maxPriority = priority;
                    minDistance = dist;
                    closestLoc = enemies[i].location;
                    closestType = enemies[i].type;
                }
                else if (priority == maxPriority && dist < minDistance)
                {
                    minDistance = dist;
                    closestLoc = enemies[i].location;
                    closestType = enemies[i].type;
                }
            }
        }
        else
        {
            enemyNum = enemyTowers.length;
            for (int i = 0; i < enemyNum; ++i)
            {
                // TODO Aim for closest towers?
                if (mLocation.distanceSquaredTo(enemyTowers[i]) <= Constants.MISSILE_MAX_RANGE_SQUARED)
                {
                    closestLoc = enemyTowers[i];
                    closestType = RobotType.TOWER;
                    break;
                }
            }

            if (closestLoc == null)
            {
                // TODO Aim at HQ before nearby towers?
                if (mLocation.distanceSquaredTo(enemyHQ) <= Constants.MISSILE_MAX_RANGE_SQUARED)
                {
                    closestLoc = enemyHQ;
                }
                else
                {
                    return;
                }
            }
        }

        Direction spawnDir = getMissileSpawnDir(closestLoc);
        if (spawnDir == null)
        {
            return;
        }
        else
        {
            rc.launchMissile(spawnDir);
            int channel = getLocChannel(mLocation.add(spawnDir));
            if (closestType == RobotType.LAUNCHER)
            {
                broadcastLocation(channel, closestLoc.add(spawnDir));
            }
            else
            {
                broadcastLocation(channel, closestLoc);
            }
        }
    }


    protected Direction getMissileSpawnDir(MapLocation target)
    {
        Direction dir = mLocation.directionTo(target);
        if (rc.isPathable(RobotType.MISSILE, target))
        {
            return dir;
        }

        dir = dir.rotateRight();
        if (rc.isPathable(RobotType.MISSILE, mLocation.add(dir)))
        {
            return dir;
        }

        dir = dir.rotateLeft().rotateLeft();
        if (rc.isPathable(RobotType.MISSILE, mLocation.add(dir)))
        {
            return dir;
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

        boolean shouldRun = false;
        int avgX = 0;
        int avgY = 0;
        int enemyCount = 0;
        for (RobotInfo r : enemies)
        {
            if (!r.type.canMine() && r.type.canMove())
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
                this.getFreeStrafeDirection(mLocation.directionTo(
                    new MapLocation(avgX, avgY)).opposite());
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

        if (type == RobotType.MISSILE)
        {
            priority = 0;
        }
        else if (type.canAttack())
        {
            priority = 6;
            if (type.canMine())
            {
                priority = 5;
            }
        }
        return priority;
    }
}
