package leytenant;

import battlecode.common.*;

/**
 * Mobile units class.
 * 
 * @author Amit Bachchan
 */
public abstract class Proletariat
    extends Soveti
{

    protected enum Decision
    {
        RELAX,
        ATTACK,
        RUN
    }

    private double        lastRoundHealth;
    private MapLocation   visited;
    protected MapLocation dest;
    protected Boolean     onWall;
    protected Direction   facing;
    protected int         mTypeNumber;
    protected int         mTypeChannel;


    public Proletariat(RobotController rc)
        throws GameActionException
    {
        super(rc);
        visited = rc.getLocation();
        onWall = false;
        dest = null;
    }


    /**
     * Gets the robot's location.
     */
    @Override
    public void run()
        throws GameActionException
    {
        mLocation = rc.getLocation();
        mTypeNumber = rc.readBroadcast(mTypeChannel);
        rc.broadcast(mTypeChannel, mTypeNumber + 1);
    }


    /**
     * Sets this robot's destination to loc.
     * 
     * @param loc
     *            Destination to head towards.
     * @return True if the destination was changed, false otherwise.
     */
    protected boolean setDestination(MapLocation loc)
    {
        if ((loc != null)
            && (dest == null || !dest.equals(loc) || dest
                .distanceSquaredTo(loc) < 10))
        {
            dest = loc;
            // TODO How does facing work? Does the robot face wherever it moved?
            facing = mLocation.directionTo(dest);
            return true;
        }
        return false;
    }


    protected boolean moveSafely(Direction dir)
        throws GameActionException
    {
        if (rc.canMove(dir))
        {
            if (!inEnemyRange(mLocation.add(dir)))
            {
                rc.move(dir);
                return true;
            }
        }
        return false;
    }


    private boolean inEnemyRange(MapLocation loc)
        throws GameActionException
    {
        RobotInfo[] enemyRobots =
            rc.senseNearbyRobots(mType.sensorRadiusSquared, enemyTeam);

        int len = enemyRobots.length;
        for (int i = 0; i < len; ++i)
        {
            if (!enemyRobots[i].type.canMine()
                && enemyRobots[i].location.distanceSquaredTo(loc) <= enemyRobots[i].type.attackRadiusSquared)
            {
                return true;
            }
        }

        for (int i = 0; i < enemyTowers.length; ++i)
        {
            if (enemyTowers[i].distanceSquaredTo(loc) <= RobotType.TOWER.attackRadiusSquared)
            {
                if (rc.canSenseLocation(enemyTowers[i])
                    && rc.senseRobotAtLocation(enemyTowers[i]) == null)
                {
                    continue;
                }
                return true;
            }
        }
        return false;
    }


    /**
     * You MUST call setDestination before using this method, or false will be
     * returned automatically. Moves towards the dest variable and bugs around
     * walls in the way returns All rotations are currently to the left.
     * 
     * @return true if a move made was made.
     * @return false if no move call was made.
     */
    protected boolean bug()
        throws GameActionException
    {
        if (dest == null || !rc.isCoreReady())
        {
            return false;
        }
        Direction towardsRally = rc.getLocation().directionTo(dest);
        boolean clearAhead =
            isNormalTile(towardsRally)
                && !visited.equals(rc.getLocation().add(towardsRally));
        // visited.add(rc.getLocation());
        visited = rc.getLocation();
        if (clearAhead)
        {
            rc.setIndicatorString(0, "It's clear ahead");
            onWall = false;
            facing = towardsRally;
            rc.setIndicatorString(1, "" + facing);
            return move(facing);
        }
        else
        {
            onWall = true;
            if (isNormalTile(facing.rotateRight()))
            {
                facing = facing.rotateRight();
                onWall = false;
                return move(facing);
            }
            if (isNormalTile(facing))
            {
                return move(facing);
            }
            else
            {
                facing = facing.rotateLeft();
                return bug();
            }
        }

// if (dest == null || !rc.isCoreReady())
// {
// return false;
// }
// // if we are already attached and traveling along a wall
// if (onWall)
// {
// // TODO THIS IS MADNESS <---- agreed.
// // there is a wall ahead of us, sets onWall to false, so that the
// // next code will run
// if (!isNormalTile(facing) || inEnemyTowerRange(facing))
// {
// onWall = false;
// }
// else if (isNormalTile(facing.rotateRight())
// && !inEnemyTowerRange(facing.rotateRight()))
// {
// onWall = false;
// }
// }
// // not against a wall, check for a wall ahead, and move forward
// if (!onWall)
// {
// facing = rc.getLocation().directionTo(dest);
//
// int count = 0;
// while ((!isNormalTile(facing) || inEnemyTowerRange(facing))
// && count < 8)
// {
// count++;
// if (turnRight)
// {
// facing = facing.rotateLeft();
// }
// else
// {
// facing = facing.rotateRight();
// }
// onWall = true;
// }
// }
// if (move(facing))
// {
// // visited.add(mLocation);
// // helper.addLast(mLocation);
// return true;
// }
// else
// {
// return false;
// }
    }


    protected boolean move(Direction dir)
        throws GameActionException
    {
        if (rc.canMove(dir))
        {
            rc.move(dir);
            return true;
        }
        return false;
    }


    protected boolean inEnemyTowerRange(Direction dir)
        throws GameActionException
    {
        return inEnemyTowerRange(rc.getLocation().add(dir));
    }


    protected boolean inEnemyTowerRange(MapLocation loc)
    {
        int distance = loc.distanceSquaredTo(enemyHQ);
        if (enemyTowers.length >= 2)
        {
            distance -= 11;
        }
        if (distance <= RobotType.HQ.attackRadiusSquared + 3)
        {
            return true;
        }

        for (MapLocation r : enemyTowers)
        {
            if (r.distanceSquaredTo(loc) <= RobotType.TOWER.attackRadiusSquared)
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks whether the location in the given direction is a normal tile with
     * no robots on it.
     * 
     * @param dir
     *            The direction to check.
     * @return True if there location is a normal tile with no other robots on
     *         it, false otherwise.
     * @throws GameActionException
     */
    private boolean isNormalTile(Direction dir)
        throws GameActionException
    {
        return rc.senseTerrainTile(rc.getLocation().add(dir)) == TerrainTile.NORMAL;
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {
        if (rc.getSupplyLevel() > 500)
        {
            if (Clock.getBytecodesLeft() < 700)
            {
                return;
            }
            RobotInfo[] allies =
                rc.senseNearbyRobots(
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    myTeam);

            if (rc.getID() == 16995)
            {
                for (int i = 0; i < allies.length && i < 3; ++i)
                {
                    int red = i == 2 ? 255 : 0;
                    int green = i == 0 ? 255 : 0;
                    int blue = i == 1 ? 255 : 0;
                    rc.setIndicatorLine(
                        new MapLocation(mapOffsetX, mapOffsetY),
                        allies[i].location,
                        red,
                        green,
                        blue);
                }
            }
            RobotInfo targetRobot = null;
            for (RobotInfo r : allies)
            {
                if (Clock.getBytecodesLeft() < 700)
                {
                    return;
                }
                if (this.isSupplyingUnit(r.type)
                    && r.supplyLevel < rc.getSupplyLevel()
                    && (targetRobot == null || r.supplyLevel < targetRobot.supplyLevel)
                    && r.health > (r.type.maxHealth * .10)) // TODO Make
// constant for percent
                {
                    targetRobot = r;
                }
            }
            if (Clock.getBytecodesLeft() < 700)
            {
                return;
            }
            if (targetRobot != null)
            {
                if (aboutToDie())
                {
                    rc.transferSupplies(
                        (int)(rc.getSupplyLevel() * .9),
                        targetRobot.location);
                }
                else
                {
                    rc.transferSupplies(
                        (int)((rc.getSupplyLevel() - targetRobot.supplyLevel) / 2),
                        targetRobot.location);
                }
            }
        }
    }


    private boolean aboutToDie()
    {
        double healthDifference = rc.getHealth() - lastRoundHealth;
        lastRoundHealth = rc.getHealth();
        if (rc.getHealth() < rc.getType().maxHealth * .10)
        {
            return true;
        }
        if (healthDifference > rc.getHealth())
        {
            return true;
        }
        return false;

    }


    /**
     * Returns the closest free direction at most 1 turn away from dir.
     * 
     * @param dir
     *            Goal direction.
     * @return The best direction or null otherwise.
     */
    protected Direction getFreeForwardDirection(Direction dir)
    {
        return getFreeDirection(dir, 3);
    }


    /**
     * Returns the closest free direction at most 2 turns away from dir.
     * 
     * @param dir
     *            Goal direction.
     * @return The best direction or null otherwise.
     */
    protected Direction getFreeStrafeDirection(Direction dir)
    {
        return getFreeDirection(dir, 5);
    }


    /**
     * Returns the closest free direction to dir.
     * 
     * @param dir
     *            Goal direction.
     * @return The best direction or null otherwise.
     */
    protected Direction getFreeDirection(Direction dir)
    {
        return getFreeDirection(dir, 8);
    }


    /**
     * Returns the closest free direction to dir at most (turns / 2) turns away.
     * 
     * @param dir
     *            Goal direction.
     * @param turns
     *            Possible number of directions that can be returned.
     * @return The best free direction or null otherwise.
     */
    protected Direction getFreeDirection(Direction dir, int turns)
    {
        // TODO Make an array
        Direction left = dir;
        Direction right = dir;
        int count = 0;
        while (!canMove(dir) && count < turns)
        {
            if (count % 2 == 0)
            {
                left = left.rotateLeft();
                dir = left;
            }
            else
            {
                right = right.rotateRight();
                dir = right;
            }
            count++;
        }
        if (count < turns)
        {
            return dir;
        }
        else
        {
            return null;
        }
    }


    private boolean canMove(Direction dir)
    {
        return rc.canMove(dir);
    }


    protected Direction[] getSpanningDirections(Direction dir)
    {
        return getSpanningDirections(dir, 8);
    }


    protected Direction[] getSpanningDirections(Direction dir, int turns)
    {
        Direction[] dirs = new Direction[turns];
        Direction left = dir;
        Direction right = dir.rotateRight();
        int count = 0;
        while (count < turns)
        {
            if (count % 2 == 0)
            {
                dirs[count] = left;
                left = left.rotateLeft();
            }
            else
            {
                dirs[count] = right;
                right = right.rotateRight();
            }
            count++;
        }
        return dirs;
    }


    /**
     * Runs in the opposite direction of the average location of enemy units
     * within attacking distance.
     * 
     * @return True if this unit moved away, false otherwise.
     * @throws GameActionException
     */
    protected boolean runAway()
        throws GameActionException
    {
        RobotInfo[] enemies =
            rc.senseNearbyRobots(mType.sensorRadiusSquared, enemyTeam);
        if (enemies.length == 0)
        {
            return false;
        }

        int enemiesThatCanAttack = 0;
        int avgX = 0;
        int avgY = 0;
        for (RobotInfo enemy : enemies)
        {
            if (!enemy.type.canMine()
                && enemy.location.distanceSquaredTo(mLocation) <= enemy.type.attackRadiusSquared)
            {
                avgX += enemy.location.x;
                avgY += enemy.location.y;
                enemiesThatCanAttack++;
            }
        }
        avgX /= enemies.length;
        avgY /= enemies.length;

        if (enemiesThatCanAttack == 0)
        {
            return false;
        }

        Direction dirAway =
            mLocation.directionTo(new MapLocation(avgX, avgY)).opposite();
        Direction[] dirs = getSpanningDirections(dirAway);
        for (int i = 0; i < dirs.length; i++)
        {
            if (moveSafely(dirs[i]))
            {
                return true;
            }
        }
        return false;
    }


    protected Direction[] getSpanningForwardDirections(Direction directionTo)
    {
        return getSpanningDirections(directionTo, 3);
    }
}
