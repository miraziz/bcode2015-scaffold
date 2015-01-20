package serzhant;

import java.util.HashSet;
import java.util.LinkedList;
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

    private double                  lastRoundHealth;
    private boolean                 turnRight;
    protected MapLocation           dest;
    private Boolean                 onWall;
    protected Direction             facing;
    private LinkedList<MapLocation> helper;         // using for
// experimenting something, may be completely useless
    private HashSet<MapLocation>    visited;        // only necessary for
// very specific cases i think.


    public Proletariat(RobotController rc)
        throws GameActionException
    {
        super(rc);
        visited = new HashSet<MapLocation>();
        helper = new LinkedList<MapLocation>();
        onWall = false;
        dest = null;
        turnRight = rand.nextBoolean();
    }


    /**
     * Gets the robot's location.
     */
    @Override
    public void run()
        throws GameActionException
    {
        mLocation = rc.getLocation();
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
            visited.clear();
            return true;
        }
        return false;
    }


    /**
     * @return true if a move was made in the direction given, false if no move
     *         was made.
     */
    protected boolean move(Direction dir)
        throws GameActionException
    {
        if (rc.isCoreReady() && rc.canMove(dir))
        {
            if (!inEnemyTowerRange(dir))
            {
                rc.move(dir);
                return true;
            }
        }
        return false;
    }


    protected boolean moveSafely(Direction dir)
        throws GameActionException
    {
        if (rc.canMove(dir))
        {
            if (!inEnemyRange(mLocation.add(dir)) && !inEnemyTowerRange(dir))
            {
                rc.move(dir);
                return true;
            }
        }
        return false;
    }


    private boolean inEnemyRange(MapLocation loc)
    {
        RobotInfo[] enemyRobots =
            rc.senseNearbyRobots(mType.sensorRadiusSquared, enemyTeam);

        int len = enemyRobots.length;
        for (int i = 0; i < len; ++i)
        {
            if (enemyRobots[i].location.distanceSquaredTo(loc) <= enemyRobots[i].type.attackRadiusSquared)
            {
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
        // if we are already attached and traveling along a wall
        if (onWall)
        {
            // TODO THIS IS MADNESS
            // there is a wall ahead of us, sets onWall to false, so that the
            // next code will run
            if (!isNormalTile(facing) || inEnemyTowerRange(facing))
            {
                onWall = false;
            }
            else
            {
                if (isNormalTile(facing.rotateRight())
                    && !inEnemyTowerRange(facing.rotateRight()))
                {
                    onWall = false;
                }
            }
        }

        // not against a wall, check for a wall ahead, and move forward
        if (!onWall)
        {
            facing = rc.getLocation().directionTo(dest);

            int count = 0;
            // there is a wall ahead, need to start bugging mode
            // need to handle if count is 8 afterwards. Means the robot is
            // kind of stuck or something, tried turning every way
            // Maybe dump the whole visited HashSet in this case?
            while ((!isNormalTile(facing) || inEnemyTowerRange(facing))
                && count < 8)
            {
                count++;
                if (turnRight)
                {
                    facing = facing.rotateLeft();
                }
                else
                {
                    facing = facing.rotateRight();
                }
                onWall = true;
            }
            if (count == 8)
            {
                if (!helper.isEmpty())
                {
                    visited.remove(helper.removeFirst());
                }
            }
        }
        if (move(facing))
        {
            // visited.add(mLocation);
            // helper.addLast(mLocation);
            return true;
        }
        else
        {
            return false;
        }
    }


    private boolean inEnemyTowerRange(Direction dir)
        throws GameActionException
    {
        MapLocation moveLoc = rc.getLocation().add(dir);
        boolean attacking = rc.readBroadcast(Channels.attacking) == 1;
        int roundNum = Clock.getRoundNum();
        int distance = moveLoc.distanceSquaredTo(enemyHQ);
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
            if (r.distanceSquaredTo(moveLoc) <= RobotType.TOWER.attackRadiusSquared)
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
        MapLocation loc = mLocation.add(dir);
        if (rc.senseTerrainTile(loc) != TerrainTile.NORMAL
        // || visited.contains(loc) // comment this line to not have any
        // effects from the hashtable/linked list
            || rc.senseRobotAtLocation(loc) != null)
        {
            return false;
        }
        return true;
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {
        if (rc.getSupplyLevel() > 500)
        {
            RobotInfo[] allies =
                rc.senseNearbyRobots(
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    myTeam);
            RobotInfo targetRobot = null;
            for (RobotInfo r : allies)
            {
                if (Clock.getBytecodesLeft() < 550)
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
            if (Clock.getBytecodesLeft() < 550)
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
}
