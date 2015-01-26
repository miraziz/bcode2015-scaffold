package kapitan;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class SimpleLauncher
    extends Proletariat
{
    private MapLocation closestTowerOrHQ;


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
            if (!runAway(enemies) && closestTowerOrHQ == null)
            {
                bug();
            }
        }
        else
        {
            // TODO ONLY DO RIGHT BEFORE MOVING rc.CoreDelay() < 1.5 ||
// rc.CoreDelay() < 2 && rc.getSupplyLevel() > 0
            closestTowerOrHQ = null;
            enemyTowers = rc.senseEnemyTowerLocations();
            int enemyNum = enemyTowers.length;
            for (int i = 0; i < enemyNum; ++i)
            {
                // TODO Aim for closest towers?
                if (mLocation.distanceSquaredTo(enemyTowers[i]) <= Constants.MISSILE_MAX_RANGE_SQUARED)
                {
                    closestTowerOrHQ = enemyTowers[i];
                    break;
                }
            }

            if (closestTowerOrHQ == null)
            {
                // TODO Aim at HQ before nearby towers?
                if (mLocation.distanceSquaredTo(enemyHQ) <= Constants.MISSILE_MAX_RANGE_SQUARED)
                {
                    closestTowerOrHQ = enemyHQ;
                }
            }
        }
        int bytecodesUsed = Clock.getBytecodeNum();
        if (bytecodesUsed > 3000)
        {
            System.out.println("Bytecodes used: " + bytecodesUsed);
        }
    }


    private void attackEnemies(RobotInfo[] enemies)
        throws GameActionException
    {
        if (rc.getMissileCount() == 0 || enemies.length == 0)
        {
            return;
        }

        MapLocation closestLoc = null;
        RobotType closestType = null;
        int minDistance = 100;
        int maxPriority = 1;
        int enemyNum = enemies.length;
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

        if (closestLoc == null)
        {
            if (closestTowerOrHQ != null)
            {
                closestLoc = closestTowerOrHQ;
            }
            else
            {
                return;
            }
        }
        Direction spawnDir = getMissileSpawnDir(closestLoc);
        if (spawnDir != null)
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
