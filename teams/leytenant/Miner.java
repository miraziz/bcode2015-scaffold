package leytenant;

import battlecode.common.*;

/**
 * Miner class.
 * 
 * @author Amit Bachchan
 */
public class Miner
    extends Proletariat
{
    private Direction[]   minerDirs;

    private int[][]       mapPointers;
    private int[][]       mapLevels;
    private MapLocation[] path;
    private boolean       pathFollowing;
    private int           curPathPos;
    private int           pathFailedCount;
    private MapLocation   minerDest;
    private double        bestOreNum;


    public Miner(RobotController rc)
        throws GameActionException
    {
        super(rc);

        minerDirs = new Direction[8];
        if (allyHQ.x == enemyHQ.x)
        {
            minerDirs[0] = Direction.SOUTH;
            minerDirs[1] = Direction.EAST;
            minerDirs[2] = Direction.WEST;
            minerDirs[3] = Direction.NORTH;
            minerDirs[4] = Direction.SOUTH_WEST;
            minerDirs[5] = Direction.SOUTH_EAST;
            minerDirs[6] = Direction.NORTH_WEST;
            minerDirs[7] = Direction.NORTH_EAST;

            if (allyHQ.y > enemyHQ.y)
            {
                for (int i = 0; i < minerDirs.length; i++)
                {
                    minerDirs[i] = minerDirs[i].opposite();
                }
            }
        }
        else if (allyHQ.y == enemyHQ.y)
        {
            minerDirs[0] = Direction.EAST;
            minerDirs[1] = Direction.NORTH;
            minerDirs[2] = Direction.SOUTH;
            minerDirs[3] = Direction.WEST;
            minerDirs[4] = Direction.SOUTH_EAST;
            minerDirs[5] = Direction.NORTH_EAST;
            minerDirs[6] = Direction.SOUTH_WEST;
            minerDirs[7] = Direction.NORTH_WEST;

            if (allyHQ.x > enemyHQ.x)
            {
                for (int i = 0; i < minerDirs.length; i++)
                {
                    minerDirs[i] = minerDirs[i].opposite();
                }
            }

        }
        else if (allyHQ.x < enemyHQ.x && allyHQ.y > enemyHQ.y)
        {
            // TODO Take into account difference between x and y (prefer bigger
// dimension)
            // first quadrant
            minerDirs[0] = Direction.WEST;
            minerDirs[1] = Direction.SOUTH;
            minerDirs[2] = Direction.NORTH;
            minerDirs[3] = Direction.EAST;
            minerDirs[4] = Direction.SOUTH_WEST;
            minerDirs[5] = Direction.NORTH_WEST;
            minerDirs[6] = Direction.SOUTH_EAST;
            minerDirs[7] = Direction.NORTH_EAST;
        }
        else if (allyHQ.x > enemyHQ.x && allyHQ.y > enemyHQ.y)
        {
            // second quadrant
            minerDirs[0] = Direction.EAST;
            minerDirs[1] = Direction.SOUTH;
            minerDirs[2] = Direction.NORTH;
            minerDirs[3] = Direction.WEST;
            minerDirs[4] = Direction.SOUTH_EAST;
            minerDirs[5] = Direction.NORTH_EAST;
            minerDirs[6] = Direction.SOUTH_WEST;
            minerDirs[7] = Direction.NORTH_WEST;
        }
        else if (allyHQ.x > enemyHQ.x && allyHQ.y < enemyHQ.y)
        {
            // third quadrant
            minerDirs[0] = Direction.EAST;
            minerDirs[1] = Direction.NORTH;
            minerDirs[2] = Direction.SOUTH;
            minerDirs[3] = Direction.WEST;
            minerDirs[4] = Direction.NORTH_EAST;
            minerDirs[5] = Direction.SOUTH_EAST;
            minerDirs[6] = Direction.NORTH_WEST;
            minerDirs[7] = Direction.SOUTH_WEST;
        }
        else if (allyHQ.x < enemyHQ.x && allyHQ.y < enemyHQ.y)
        {
            // fourth quadrant
            minerDirs[0] = Direction.WEST;
            minerDirs[1] = Direction.NORTH;
            minerDirs[2] = Direction.SOUTH;
            minerDirs[3] = Direction.EAST;
            minerDirs[4] = Direction.NORTH_WEST;
            minerDirs[5] = Direction.SOUTH_WEST;
            minerDirs[6] = Direction.NORTH_EAST;
            minerDirs[7] = Direction.SOUTH_EAST;
        }
        mTypeChannel = Channels.minerCount;
    }


    /**
     * Runs away if enemies are nearby, then tries to mine.
     */
    @Override
    public void run()
        throws GameActionException
    {
        super.run();

        int mGroup = mTypeNumber % Constants.TOT_MINER_FRACS;
        if (mGroup < Constants.FAST_MINER_FRACS)
        {
            Constants.MIN_ORE = Constants.FAST_MIN_ORE;
        }
        else if (mGroup < Constants.NORM_MINER_FRACS)
        {
            Constants.MIN_ORE = Constants.NORM_MIN_ORE;
        }
        else
        {
            Constants.MIN_ORE = Constants.POTATO_MIN_ORE;
        }
        findDefenseSpot();

        String doing = "Not set";

        if (rc.isCoreReady())
        {
            // TODO Don't go back in the direction of the enemy. Have some
// memory that prevents you from going in that direction for some K turns before
// trying again
            if (runAway())
            {
                doing = "Running away";
                pathFollowing = false;
            }
            else
            {
                boolean coreDelayed = false;
                if (pathFollowing)
                {
                    // TODO Move out of way?
                    if (rc.senseOre(mLocation) >= Constants.PATH_ORE)
                    {
                        doing = "Mining on path";
                        rc.mine();
                        coreDelayed = true;
                    }
                    else
                    {
                        doing = "Moving along path";
                        coreDelayed = moveAlongPath();
                    }
                }

                if (!coreDelayed && !pathFollowing)
                {
                    if (unblockAlly())
                    {
                        doing = "Unblocking ally";
                    }
                    else if (moveOrMine())
                    {
                        doing = "Moving or mining";
                    }
                    else
                    {
                        // Mine on the way to best ore or stay in your area
                        MapLocation bestLoc = findClosestOre();
                        if (bestLoc != null)
                        {
                            mLocation = rc.getLocation();
                            setPath(bestLoc);
                        }
                    }
                }
            }
        }

// if (!rc.isCoreReady() && !pathFollowing && rc.getSupplyLevel() > 14)
// {
// System.out.println("CORE DELAY START: " + rc.getCoreDelay());
// MapLocation bestLoc = checkBetterOre();
// if (bestLoc != null)
// {
// setPath(bestLoc);
// run();
// }
// System.out.println("CORE DELAY END: " + rc.getCoreDelay());
// }

        rc.setIndicatorString(0, doing);
        rc.setIndicatorString(1, "IS FOLLOWING: " + pathFollowing);
        rc.setIndicatorString(2, "DEST: " + minerDest);
        rc.setIndicatorString(3, "ORE: " + bestOreNum);

        // TODO If nothing is found, go in a random direction or blow up

    }


    private boolean unblockAlly()
        throws GameActionException
    {
        MapLocation loc;
        RobotInfo robot;
        Direction blockedDir = null;
        for (int i = minerDirs.length - 1; i >= 0; --i)
        {
            loc = mLocation.add(minerDirs[i]);
            if (rc.senseOre(loc) < Constants.MIN_ORE)
            {
                robot = rc.senseRobotAtLocation(loc);
                if (robot != null && robot.type == mType
                    && robot.team == myTeam)
                {
                    blockedDir = minerDirs[i];
                    break;
                }
            }
        }

        if (blockedDir != null)
        {
            Direction[] dirs = getSpanningDirections(blockedDir.opposite());
            for (int i = 0; i < dirs.length; i++)
            {
                if (rc.senseOre(mLocation.add(dirs[i])) >= Constants.MIN_ORE
                    && moveSafely(dirs[i]))
                {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean moveAlongPath()
        throws GameActionException
    {
        Direction dirToMove = mLocation.directionTo(path[curPathPos]);
        rc.setIndicatorLine(mLocation, path[path.length - 1], 0, 255, 0);
        if (moveSafely(dirToMove))
        {
            curPathPos++;
            if (curPathPos == path.length)
            {
                pathFollowing = false;
            }
            return true;
        }
        else
        {
            if (pathFailedCount > Constants.PATH_MAX_FAILED_TRIES)
            {
                pathFollowing = false;
            }
            else
            {
                pathFailedCount++;
            }
            return false;
        }
    }


    /**
     * Attempts to mine the current location. If location is mined out, attempts
     * to move to richest adjacent cell. If all adjacent cells are mined out,
     * moves in a random direction. Otherwise, does nothing.
     * 
     * @return True if this serp moved or mined, false otherwise.
     * @throws GameActionException
     */
    protected boolean moveOrMine()
        throws GameActionException
    {
        if (rc.senseOre(mLocation) >= Constants.MIN_ORE)
        {
            rc.mine();
            return true;
        }
        else
        {
            for (int i = 0; i < minerDirs.length; i++)
            {
                if (rc.senseOre(mLocation.add(minerDirs[i])) >= Constants.MIN_ORE
                    && moveSafely(minerDirs[i]))
                {
                    return true;
                }
            }
        }
        return false;
    }


    private MapLocation checkBetterOre()
    {
        System.out
            .println("Starting checkBetterOre: " + Clock.getBytecodeNum());
        mapPointers = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        mapLevels = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        MapLocation[] trollQ =
            new MapLocation[GameConstants.MAP_MAX_WIDTH
                * GameConstants.MAP_MAX_HEIGHT];
        int startQ = 0, endQ = 0;

        bestOreNum = rc.senseOre(mLocation);
        for (int i = 0; i < minerDirs.length; ++i)
        {
            bestOreNum =
                Math.max(bestOreNum, rc.senseOre(mLocation.add(minerDirs[i])));
        }
        MapLocation bestLoc = null;

        trollQ[endQ++] = mLocation;
        mapPointers[mLocation.x - mapOffsetX][mLocation.y - mapOffsetY] = -1;

        int cX, cY, oX, oY, cL;
        while (startQ != endQ)
        {
            MapLocation cur = trollQ[startQ++];
            cX = cur.x - mapOffsetX;
            cY = cur.y - mapOffsetY;
            cL = mapLevels[cX][cY];

            double ore = rc.senseOre(cur);
            if (ore >= 0)
            {
                if (cL >= Constants.BETTER_ORE_MIN_RANGE && ore > bestOreNum)
                {
                    bestOreNum = ore;
                    bestLoc = cur;
                }

                if (cL <= Constants.BETTER_ORE_MAX_RANGE
                    && rc.senseTerrainTile(cur) == TerrainTile.NORMAL)
                {
                    for (int i = 0; i < 8; i += 2)
                    {
                        MapLocation next = cur.add(directions[i]);
                        oX = next.x - mapOffsetX;
                        oY = next.y - mapOffsetY;
                        if (mapPointers[oX][oY] == 0)
                        {
                            mapPointers[oX][oY] =
                                cX * Constants.MAP_HEIGHT + cY;
                            mapLevels[oX][oY] = cL + 1;
                            trollQ[endQ++] = next;
                        }

                        if (i == 6)
                        {
                            i = -1;
                        }
                    }
                }
            }
        }

        System.out.println("Finishing checkBetterOre: "
            + Clock.getBytecodeNum());
        return bestLoc;
    }


    private MapLocation findClosestOre()
        throws GameActionException
    {
        mapPointers = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        mapLevels = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        MapLocation[] trollQ =
            new MapLocation[GameConstants.MAP_MAX_WIDTH
                * GameConstants.MAP_MAX_HEIGHT];
        int startQ = 0, endQ = 0;

        MapLocation oreLoc = null;

        trollQ[endQ++] = mLocation;
        mapPointers[mLocation.x - mapOffsetX][mLocation.y - mapOffsetY] = -1;

        int cX, cY, oX, oY;
        while (startQ != endQ)
        {
            MapLocation cur = trollQ[startQ++];
            cX = cur.x - mapOffsetX;
            cY = cur.y - mapOffsetY;

            if (Clock.getBytecodesLeft() < 100)
            {
                rc.yield();
            }
            double ore = rc.senseOre(cur);

            if (ore == -1)
            {
                oreLoc = cur;
                break;
            }
            else if (rc.isPathable(RobotType.MINER, cur))
            {
// else if (rc.canSenseLocation(cur))
// {
// // TODO Go into previously seen ores (so don't do
// // rc.canSenseLocation)
// RobotInfo robot = rc.senseRobotAtLocation(cur);
// if (robot == null || !robot.type.isBuilding
// && !robot.type.canBuild())
// {
// // TODO Avoid stationary units in another way (Not just
// // avoid beavers)
                if (ore >= Constants.MIN_ORE)
                {
                    oreLoc = cur;
                    break;
                }

                if (rc.senseTerrainTile(cur) == TerrainTile.NORMAL)
                {
                    for (int i = 0; i < 8; i += 2)
                    {
                        MapLocation next = cur.add(directions[i]);
                        oX = next.x - mapOffsetX;
                        oY = next.y - mapOffsetY;
                        if (mapPointers[oX][oY] == 0)
                        {
                            mapPointers[oX][oY] =
                                cX * Constants.MAP_HEIGHT + cY;
                            mapLevels[oX][oY] = mapLevels[cX][cY] + 1;
                            trollQ[endQ++] = next;
                        }

                        if (i == 6)
                        {
                            i = -1;
                        }
                    }
                }
            }

        }
// System.out.println("Finishing findClosestOre: "
// + Clock.getBytecodeNum());
        return oreLoc;
    }


    private void setPath(MapLocation cur)
    {
        minerDest = cur;
        int cX = cur.x - mapOffsetX;
        int cY = cur.y - mapOffsetY;
        int level = mapLevels[cX][cY];
        path = new MapLocation[level + 1];

        int point = mapPointers[cX][cY];
        while (point != -1)
        {
            path[level] = new MapLocation(cX + mapOffsetX, cY + mapOffsetY);
            cX = point / Constants.MAP_HEIGHT;
            cY = point % Constants.MAP_HEIGHT;
            point = mapPointers[cX][cY];
            level--;
        }
        pathFollowing = true;
        curPathPos = 1;
        pathFailedCount = 0;
    }


    /**
     * Determines the enemy presence around this miner and sets the global rally
     * to this position if its the biggest presence found so far.
     * 
     * @throws GameActionException
     */
    public void findDefenseSpot()
        throws GameActionException
    {
        // TODO Merge with runAway. Uses the same things.
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
            broadcastLocation(Channels.highestEnemyHealthLoc, new MapLocation(
                avgX,
                avgY));
        }
    }
}
