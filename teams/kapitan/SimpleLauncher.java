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
    int[] missileIds;
    int[] missileTurnCount;
    int   missileToAdd;


    public SimpleLauncher(RobotController rc)
        throws GameActionException
    {
        super(rc);
        setDestination(enemyHQ);
        missileIds = new int[5];
        missileTurnCount = new int[5];
    }


    public void run()
        throws GameActionException
    {
        super.run();
        missileToAdd = -1;
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
        int maxPriority = 1;
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
            else if (priority == maxPriority && dist < minDistance)
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
            MapLocation spawnSpot = rc.getLocation().add(spawnDir);
            int channel = getLocChannel(spawnSpot);
            if (closest.type == RobotType.LAUNCHER)
            {
                broadcastLocation(channel, closest.location.add(spawnDir));
                rc.broadcast(getIdChannel(spawnSpot), closest.ID);
            }
            else
            {
                broadcastLocation(channel, closest.location);
                rc.broadcast(getIdChannel(spawnSpot), closest.ID);
            }
        }
        // this.manageMissiles();
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


    private void manageMissiles()
        throws GameActionException
    {
        for (int i = 0; i < missileIds.length; i++)
        {
            if (missileToAdd > 0 && missileIds[i] == 0)
            {
                missileIds[i] = missileToAdd;
                missileTurnCount[i] = 0;
                missileToAdd = -1;
            }
            if (rc.canSenseRobot(missileIds[i]))
            {
                RobotInfo rob = rc.senseRobot(missileIds[i]);
                RobotInfo[] nearbyToMissile =
                    rc.senseNearbyRobots(
                        rob.location,
                        (6 * 6) - (missileTurnCount[i] * missileTurnCount[i]),
                        this.enemyTeam);
                MapLocation closest =
                    findClosestEnemy(rob.location, nearbyToMissile);
                if (closest != null)
                {
                    broadcastLocation(this.getLocChannel(rob.location), closest);
                }
                else
                {
                    rc.broadcast(this.getLocChannel(rob.location), 0);
                }
                missileTurnCount[i]++;
            }
            else
            {
                missileIds[i] = 0;
                missileTurnCount[i] = 0;
            }
        }
    }


    private MapLocation findClosestEnemy(MapLocation source, RobotInfo[] robots)
    {
        if (robots.length == 0)
        {
            return null;
        }
        RobotInfo closest = null;
        int minDistance = 100;
        for (int i = 0; i < robots.length; i++)
        {
            int dist = source.distanceSquaredTo(robots[i].location);
            if (dist < minDistance && robots[i].type.canAttack())
            {
                closest = robots[i];
            }
            else if ((closest == null || !closest.type.canAttack())
                && dist < minDistance && robots[i].type.isBuilding)
            {
                closest = robots[i];
            }
        }
        return closest.location;
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
            if (type == RobotType.DRONE)
            {
                priority = 2;
            }
        }
        if (type == RobotType.MISSILE)
        {
            priority = 0;
        }
        return priority;
    }
}
