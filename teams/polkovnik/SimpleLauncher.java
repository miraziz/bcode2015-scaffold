package polkovnik;

import battlecode.common.*;

// TODO Launchers avoid running away into tower range
public class SimpleLauncher
    extends Proletariat
{
    private MapLocation closestTowerOrHQ;
    int[]               missileIds;
    int[]               missileTurnCount;
    int                 missileToAdd;
    String              attacking = "";


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
        String doing = "Not set";
        missileToAdd = -1;
        RobotInfo[] enemies =
            rc.senseNearbyRobots(Constants.MISSILE_MAX_RANGE_SQUARED, enemyTeam);
        enemyTowers = rc.senseEnemyTowerLocations();
        attackEnemies(enemies);
        fixClosestTowerOrHQ();
        if (rc.isCoreReady())
        {
            doing = "Core is ready";
            // TODO Move away if empty?
            if (!runAway(enemies))
            {
                doing = "Did not run away";
                if (closestTowerOrHQ == null)
                {
                    doing = "Bugging";
                    bugWithCounter();
                }
            }
            else
            {
                doing = "Ran away";
            }
        }
        else
        {
            // TODO ONLY DO RIGHT BEFORE MOVING rc.CoreDelay() < 1.5 ||
// rc.CoreDelay() < 2 && rc.getSupplyLevel() > 0
            // fixClosestTowerOrHQ();
        }
        rc.setIndicatorString(0, doing);
        rc.setIndicatorString(1, "Is near tower or HQ: "
            + (closestTowerOrHQ == null ? "No" : closestTowerOrHQ));
        int bytecodesUsed = Clock.getBytecodeNum();
        if (bytecodesUsed > 3500)
        {
            System.out.println("Bytecodes used: " + bytecodesUsed);
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
        int closestID = -1;
        int minDistance = 100;
        int maxPriority = 1;
        int enemyNum = enemies.length;
        int counter = 0;
        for (int i = 0; i < enemyNum && Clock.getBytecodeNum() < 1000; ++i)
        {
            counter++;
            int priority = getMissilePriority(enemies[i].type);
            int dist = mLocation.distanceSquaredTo(enemies[i].location);
            if (priority > maxPriority)
            {
                maxPriority = priority;
                minDistance = dist;
                closestLoc = enemies[i].location;
                closestType = enemies[i].type;
                closestID = enemies[i].ID;
            }
            else if (priority == maxPriority && dist < minDistance)
            {
                minDistance = dist;
                closestLoc = enemies[i].location;
                closestType = enemies[i].type;
                closestID = enemies[i].ID;
            }
        }
        if (Clock.getBytecodeNum() > 1000)
        {
            System.out.println(counter);
        }

        if (closestLoc == null)
        {
            if (closestTowerOrHQ != null)
            {
                closestLoc = closestTowerOrHQ;
            }
            else
            {
                if (rc.getMissileCount() == 5 && rc.isWeaponReady())
                {
                    MapLocation target = mLocation.add(facing, 7);
                    Direction spawnDir = getMissileSpawnDir(target);
                    if (spawnDir != null)
                    {
                        rc.launchMissile(spawnDir);
                        MapLocation spawnSpot = mLocation.add(spawnDir);
                        int channel = getLocChannel(spawnSpot);
                        broadcastLocation(channel, target);
                        rc.broadcast(channel + 1, 32001);
                    }
                }
                return;
            }
        }

        Direction spawnDir = getMissileSpawnDir(closestLoc);
        if (spawnDir != null)
        {
            rc.launchMissile(spawnDir);
            MapLocation spawnSpot = mLocation.add(spawnDir);
            int channel = getLocChannel(spawnSpot);
            if (closestType == RobotType.LAUNCHER)
            {
                broadcastLocation(channel, closestLoc.add(spawnDir));
            }
            else
            {
                broadcastLocation(channel, closestLoc);
            }
            rc.broadcast(channel + 1, closestID);
        }
        // this.manageMissiles();
    }


    protected Direction getMissileSpawnDir(MapLocation target)
    {
        Direction dir = mLocation.directionTo(target);
        if (rc.isPathable(RobotType.MISSILE, mLocation.add(dir)))
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
            if (type == RobotType.DRONE)
            {
                priority = 2;
            }
        }
        return priority;
    }


    private void fixClosestTowerOrHQ()
    {
        closestTowerOrHQ = null;
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
}
