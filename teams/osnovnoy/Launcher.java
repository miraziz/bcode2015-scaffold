package osnovnoy;

import battlecode.common.*;

// TODO Reduce bytecode usage

public class Launcher
    extends Fighter
{
    private int[][]     nearbyMap;
    private int         nearbyMapX;
    private int         nearbyMapY;
    private int         curRound;
    private MapLocation reachableEnemyLoc = null;


    public Launcher(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.launcherCount;
        setDestination(enemyHQ);
    }


    @Override
    public void run()
        throws GameActionException
    {
        curRound = Clock.getRoundNum();
        super.run();

        attackEnemies();

        int took1 = Clock.getRoundNum() - curRound;

        if (rc.isCoreReady())
        {
            if (!runAwayOrAttack())
            {
                bugWithCounter();
            }
        }

        int took2 = Clock.getRoundNum() - curRound;

        if (took1 > 0)
        {
            System.out.println("ATTACKENEMIES TOOK AN EXTRA: " + took1);
            System.out.println("BUGGING TOOK AN EXTRA: " + (took2 - took1));
            System.out.println("LEFTOVER BYTECODES: "
                + Clock.getBytecodesLeft());
            System.out.println("LAUNCHER UNSUPPLIED: "
                + (rc.getSupplyLevel() == 0));
        }
    }


    private void attackEnemies()
        throws GameActionException
    {
        // TODO Launch all at once
        int missileCount = rc.getMissileCount();
        if (missileCount > 0)
        {
            RobotInfo[] nearbyEnemies =
                rc.senseNearbyRobots(
                    Constants.MISSILE_MAX_RANGE_SQUARED,
                    enemyTeam);
            if (nearbyEnemies.length > 0)
            {
                RobotInfo[] nearbyAllies =
                    rc.senseNearbyRobots(
                        Constants.MISSILE_MAX_RANGE_SQUARED,
                        myTeam);
                nearbyMap =
                    new int[3 * Constants.MISSILE_MAX_RANGE][3 * Constants.MISSILE_MAX_RANGE];
                nearbyMapX = mLocation.x - Constants.MISSILE_MAX_RANGE;
                nearbyMapY = mLocation.y - Constants.MISSILE_MAX_RANGE;

                for (int i = 0; i < nearbyAllies.length; ++i)
                {
                    nearbyMap[nearbyAllies[i].location.x - nearbyMapX][nearbyAllies[i].location.y
                        - nearbyMapY] = Constants.ALLY_NEAR_LAUNCHER;
                }
                for (int i = 0; i < nearbyEnemies.length; ++i)
                {
                    nearbyMap[nearbyEnemies[i].location.x - nearbyMapX][nearbyEnemies[i].location.y
                        - nearbyMapY] = Constants.ENEMY_NEAR_LAUNCHER;
                }

                for (int i = 0; i < nearbyEnemies.length && missileCount > 0; ++i)
                {
                    if (nearbyEnemies[i].team != myTeam)
                    {
                        Direction toEnemy =
                            bestLaunchDir(0, i + Constants.ALLY_NEAR_LAUNCHER
                                + 1, mLocation, nearbyEnemies[i].location);
                        ;
                        // TODO Is rc.canLaunch(toEnemy) necessary?
                        if (toEnemy != null)
                        {
                            // System.out.println("MISSILE LAUNCH");
                            if (Clock.getRoundNum() != curRound)
                            {
                                System.out.println("Rounds behind: "
                                    + (Clock.getRoundNum() - curRound));
                            }
                            rc.launchMissile(toEnemy);

                            broadcastLocation(
                                getLocChannel(mLocation.add(toEnemy)),
                                reachableEnemyLoc);
                            reachableEnemyLoc = null;

                            MapLocation newLoc = mLocation.add(toEnemy);
                            nearbyMap[newLoc.x - nearbyMapX][newLoc.y
                                - nearbyMapY] = Constants.ALLY_NEAR_LAUNCHER;
// missileCount--;
                            break;
                        }
                    }
                }
            }
            else
            {
                Direction toTower = null;
                enemyTowers = rc.senseEnemyTowerLocations();
                int N = enemyTowers.length;
                for (int i = 0; i < N; ++i)
                {
                    if (mLocation.distanceSquaredTo(enemyTowers[i]) <= Constants.MISSILE_MAX_RANGE_SQUARED)
                    {
                        toTower = mLocation.directionTo(enemyTowers[i]);
                        if (rc.canLaunch(toTower))
                        {
                            rc.launchMissile(toTower);

                            MapLocation newLoc = mLocation.add(toTower);
                            broadcastLocation(
                                getLocChannel(newLoc),
                                enemyTowers[i]);
                            break;
                        }
                    }
                }
            }
        }
    }


    protected boolean runAwayOrAttack()
        throws GameActionException
    {
        RobotInfo[] enemies =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
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
            if (r.type.canAttack())
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


    private Direction bestLaunchDir(
        int moves,
        int mark,
        MapLocation cur,
        MapLocation dest)
        throws GameActionException
    {
        Direction bestDir = null;
        if (moves < Constants.MISSILE_MAX_RANGE - 1)
        {
            nearbyMap[cur.x - nearbyMapX][cur.y - nearbyMapY] = mark;
            Direction left = cur.directionTo(dest);
            Direction right = left.rotateRight();
            Direction nextDir = null;
            int count = 0;
            while (count < 3)
            {
                if (count % 2 == 0)
                {
                    nextDir = left;
                    left = left.rotateLeft();
                }
                else
                {
                    nextDir = right;
                    right = right.rotateRight();
                }

                MapLocation next = cur.add(nextDir);
                int robotOnTile =
                    nearbyMap[next.x - nearbyMapX][next.y - nearbyMapY];
                if (robotOnTile == Constants.ENEMY_NEAR_LAUNCHER)
                {
                    if (moves > 0)
                    {
                        bestDir = nextDir;
                        reachableEnemyLoc = next;
                        break;
                    }
                }
                else if (robotOnTile != Constants.ALLY_NEAR_LAUNCHER
                    && robotOnTile != mark)
                {
                    Direction res = bestLaunchDir(moves + 1, mark, next, dest);
                    if (res != null)
                    {
                        // TODO Mark all tiles this missile would go through as
// blocked for any future attempted missile launches
                        // nearbyMap[cur.x - nearbyMapX][cur.y - nearbyMapY] =
// Constants.ALLY_NEAR_LAUNCHER;
                        bestDir = nextDir;
                        break;
                    }
                }
                count++;
            }
        }
        return bestDir;
    }
}
