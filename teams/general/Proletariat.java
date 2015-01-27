package general;

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

    private boolean       turnRight;
    private double        lastRoundHealth;
    private MapLocation   visited;
    protected MapLocation dest;
    protected Boolean     onWall;
    protected Direction   facing;
    protected int         mTypeNumber;
    protected int         mTypeChannel;
    private int           turnCount;
    private int           bugFailureCount;
    protected boolean     avoidTowers;


    public Proletariat(RobotController rc)
        throws GameActionException
    {
        super(rc);
        avoidTowers = true;
        visited = rc.getLocation();
        onWall = false;
        dest = null;
        turnRight = rand.nextBoolean();
        rc.setIndicatorString(0, "Turning right: " + turnRight);
    }


    /**
     * Gets the robot's location.
     */
    @Override
    public void run()
        throws GameActionException
    {
        turnCount = 0;
        mLocation = rc.getLocation();
        mTypeNumber = rc.readBroadcast(mTypeChannel);
        rc.broadcast(mTypeChannel, mTypeNumber + 1);
        manageSupply();
        if (rc.getType() == RobotType.BEAVER)
        {
            rc.setIndicatorString(2, "My visited loc: " + visited
                + ", error count: " + this.bugFailureCount);
        }
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
        if (this.turnCount > 9)
        {
            return false;
        }
        turnCount++;
        if (dest == null || !rc.isCoreReady())
        {
            return false;
        }
        if (onWall)
        {
            if (isClear(rotateBackward(facing)))
            {
                onWall = false;
                facing = rotateBackward(facing);
                return move(facing);
            }
            else if (isClear(facing))
            {
                return move(facing);
            }
            else
            {
                facing = rotateForward(facing);
                return bug();
            }
        }
        else
        {
            facing = rc.getLocation().directionTo(dest);
            boolean clearAhead = isClear(facing);
            if (clearAhead)
            {
                return move(facing);
            }
            else
            {
                onWall = true;
                facing = rotateForward(facing);
                return bug();
            }
        }
    }


    protected boolean bugWithCounter()
        throws GameActionException
    {
        if (bugFailureCount < 3)
        {
            if (bug())
            {
                bugFailureCount = 0;
                return true;
            }
            else
            {
                bugFailureCount++;
                return false;
            }
        }
        else
        {
            visited = rc.getLocation();
            return bug();
        }
    }


    private boolean isClear(Direction dir)
        throws GameActionException
    {
        MapLocation next = rc.getLocation().add(dir);
        RobotInfo rob = rc.senseRobotAtLocation(next);
        if (rc.senseTerrainTile(rc.getLocation().add(dir)) == TerrainTile.OFF_MAP)
        {
            facing = facing.opposite();
            turnRight = !turnRight;
        }
        boolean clear =
            rc.isPathable(rc.getType(), next)
                && (!avoidTowers || !this.inEnemyTowerRange(dir));
// if (rc.getType() != RobotType.BEAVER)
// {
        clear = clear && !visited.equals(next);

// }
        return clear;
    }


    protected boolean move(Direction dir)
        throws GameActionException
    {

        if (rc.canMove(dir))
        {
            if (!visited.equals(rc.getLocation()))
            {
                visited = rc.getLocation();
            }
            rc.move(dir);
            this.bugFailureCount = 0;
            return true;
        }
        else if (rc.senseTerrainTile(rc.getLocation().add(dir)) == TerrainTile.OFF_MAP)
        {
            System.out.println("HERE");
            visited = rc.getLocation();
            turnRight = !turnRight;
            return bug();
        }
        return false;
    }


    private Direction rotateForward(Direction dir)
    {
        if (turnRight)
        {
            return dir.rotateLeft();
        }
        else
        {
            return dir.rotateRight();
        }
    }


    private Direction rotateBackward(Direction dir)
    {
        if (turnRight)
        {
            return dir.rotateRight();
        }
        else
        {
            return dir.rotateLeft();
        }

    }


    protected boolean inEnemyTowerRange(Direction dir)
        throws GameActionException
    {
        return inEnemyTowerRange(rc.getLocation().add(dir));
    }


    protected boolean inEnemyTowerRange(MapLocation loc)
    {
        int distance = loc.distanceSquaredTo(enemyHQ);
        int enemyHQRange = RobotType.HQ.sensorRadiusSquared;
        if (enemyTowers.length >= 5)
        {
            enemyHQRange++;
        }
        if (distance <= enemyHQRange)
        {
            return true;
        }

        int towerRange = RobotType.TOWER.sensorRadiusSquared;
        for (MapLocation r : enemyTowers)
        {
            if (r.distanceSquaredTo(loc) <= towerRange)
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
        int supplyThreshhold = 500;
        if (rc.getType() == RobotType.COMMANDER)
        {
            supplyThreshhold = 5000;
        }
        if (rc.getSupplyLevel() > supplyThreshhold)
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
        while (!rc.canMove(dir) && count < turns)
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


    protected Direction[] getSpanningDirections(Direction dir)
    {
        return getSpanningDirections(dir, 8);
    }


    private void print(String nm)
    {
        if (Clock.getRoundNum() < 400)
        {
            System.out.println(nm);
        }
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
    protected boolean runAwayOrAttack()
        throws GameActionException
    {
        RobotInfo[] enemies =
            rc.senseNearbyRobots(mType.sensorRadiusSquared, enemyTeam);
        if (enemies.length == 0)
        {
            return false;
        }

        MapLocation closestMiner = null;
        int enemiesThatCanAttack = 0;
        int avgX = 0;
        int avgY = 0;
        for (RobotInfo enemy : enemies)
        {
            int dist = mLocation.distanceSquaredTo(enemy.location);
            if (dist <= enemy.type.attackRadiusSquared)
            {
                if (enemy.type.canMine())
                {
                    closestMiner = enemy.location;
                }
                else
                {
                    avgX += enemy.location.x;
                    avgY += enemy.location.y;
                    enemiesThatCanAttack++;
                }
            }
        }

        if (enemiesThatCanAttack == 0)
        {
            if (closestMiner != null)
            {
                if (rc.isWeaponReady())
                {
                    rc.attackLocation(closestMiner);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            avgX /= enemiesThatCanAttack;
            avgY /= enemiesThatCanAttack;
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


    protected void manageSupply()
        throws GameActionException
    {
        int supplyPriority = getSupplyPriority(rc.getType());
        if (rc.getSupplyLevel() < 500)
        {
            int distance = rc.getLocation().distanceSquaredTo(allyHQ);

            int curPriority = rc.readBroadcast(Channels.supplyPriority);
            if (supplyPriority > curPriority
                || (distance > rc.readBroadcast(Channels.supplyDistance) && supplyPriority == curPriority))
            {
                rc.broadcast(Channels.supplyPriority, supplyPriority);
                rc.broadcast(Channels.supplyDistance, distance);
                broadcastLocation(Channels.supplyLoc, rc.getLocation());
            }
        }
    }


    int getSupplyPriority(RobotType type)
    {
        int supplyPriority = 1;

        if (rc.getType() == RobotType.LAUNCHER)
        {
            supplyPriority = 10;
        }
        else if (rc.getType() == RobotType.COMMANDER)
        {
            supplyPriority = 4;
        }
        else if (rc.getType() == RobotType.MINER)
        {
            supplyPriority = 3;
        }
        if (rc.getType() == RobotType.TANK)
        {
            supplyPriority = 3;
        }
        else if (rc.getType() == RobotType.SOLDIER)
        {
            supplyPriority = 2;
        }

        return supplyPriority;
    }
}
