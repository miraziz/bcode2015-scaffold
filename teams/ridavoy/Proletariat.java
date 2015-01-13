package ridavoy;

import java.util.HashSet;
import java.util.LinkedList;
import battlecode.common.*;

/**
 * Mobile units class.
 * 
 * @author Miraziz
 */
public abstract class Proletariat
    extends Soveti
{
    Direction                       priorityDirection;

    private MapLocation             dest;
    private Boolean                 onWall;
    protected Direction             facing;
    private LinkedList<MapLocation> helper;           // using for
// experimenting something, may be completely useless
    private HashSet<MapLocation>    visited;          // only necessary for
// very specific cases i think.


    public Proletariat(RobotController rc)
        throws GameActionException
    {
        super(rc);
        visited = new HashSet<MapLocation>();
        helper = new LinkedList<MapLocation>();
        onWall = false;
        dest = null;
        int choice = rc.readBroadcast(Channels.minerCount) % 4;
        if (choice == 1)
        {
            priorityDirection = Direction.NORTH;
        }
        else if (choice == 2)
        {
            priorityDirection = Direction.EAST;
        }
        else if (choice == 3)
        {
            priorityDirection = Direction.SOUTH;
        }
        else
        {
            priorityDirection = Direction.WEST;
        }
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
        if ((loc != null) && (dest == null || !dest.equals(loc)))
        {
            dest = loc;
            facing = mLocation.directionTo(dest);
            visited.clear();
            return true;
        }
        return false;
    }


    /**
     * @return true if a move was made in the direction given
     * @return false if no move was made
     */
    protected boolean move(Direction dir)
        throws GameActionException
    {
        if (rc.isCoreReady() && rc.canMove(dir))
        {
            rc.move(dir);
            return true;
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
        if (dest == null)
        {
            return false;
        }
        // if we are already attached and traveling along a wall
        if (onWall)
        {
            // there is a wall ahead of us, sets onWall to false, so that the
            // next code will run
            if (!isNormalTile(facing))
            {
                onWall = false;
            }
            else
            {
                if (isNormalTile(facing.rotateRight().rotateRight()))
                {
                    rc.setIndicatorString(
                        2,
                        "switched onwall off at: " + Clock.getRoundNum());
                    onWall = false;
                    facing = mLocation.directionTo(dest);
                }
            }
        }
        // not against a wall, check for a wall ahead, and move forward
        if (!onWall)
        {
            int count = 0;
            // there is a wall ahead, need to start bugging mode
            // need to handle if count is 8 afterwards. Means the robot is
            // kind of stuck or something, tried turning every way
            // Maybe dump the whole visited HashSet in this case?
            while (!isNormalTile(facing) && count < 8)
            {
                count++;
                facing = facing.rotateLeft();
                onWall = true;
            }
            if (count == 8)
            {
                if (!helper.isEmpty())
                {
                    visited.remove(helper.removeFirst());
                }
                else
                {

                }
            }
        }
        if (move(facing))
        {
            visited.add(mLocation);
            helper.addLast(mLocation);
            return true;
        }
        else
        {
            return false;
        }
    }


    private boolean isNormalTile(Direction dir)
        throws GameActionException
    {
        MapLocation loc = mLocation.add(dir);
        if (rc.senseTerrainTile(loc) != TerrainTile.NORMAL
            || visited.contains(loc) // comment this line to not have any
            // effects from the hashtable/linked list
            || rc.senseRobotAtLocation(loc) != null)
        {
            return false;
        }
        return true;
    }


    protected void mine()
        throws GameActionException
    {

        // if not in any danger
        if (rc.isCoreReady())
        {
            double oreAmount = rc.senseOre(rc.getLocation());
            if (rc.canMine() && oreAmount > 0)
            {
                rc.mine();
                rc.setIndicatorString(0, "Just mined: " + oreAmount);
                rc.broadcast(
                    Channels.miningTotal,
                    (int)(rc.readBroadcast(Channels.miningTotal) + oreAmount));
            }
            else
            {
                Direction bestDir = priorityDirection;
                double bestScore = 0;
                Direction dir = priorityDirection;
                for (int i = 0; i < 8; i++)
                {
                    if (rc.canMove(dir))
                    {
                        double oreCount = rc.senseOre(mLocation.add(dir));
                        if (oreCount > bestScore)
                        {
                            bestDir = dir;
                            bestScore = oreCount;
                        }
                    }
                    dir = dir.rotateRight();
                }
                if (bestScore == 0)
                {
                    this.setDestination(mLocation.add(priorityDirection));
                    bug();
                }
                else
                {
                    this.priorityDirection = bestDir;
                    move(bestDir);
                }
            }
        }
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {
        double totSupply = rc.getSupplyLevel();

        RobotInfo[] nearbyAllies =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);

        double lowestSupply = totSupply;
        MapLocation allyLoc = null;
        for (RobotInfo teamMember : nearbyAllies)
        {
            if (teamMember.supplyLevel < lowestSupply
                && isAttackingUnit(teamMember.type))
            {
                lowestSupply = teamMember.supplyLevel;
                allyLoc = teamMember.location;
            }
        }

        if (allyLoc != null)
        {
            int transferAmount = (int)((totSupply - lowestSupply) / 2.0);
            rc.transferSupplies(transferAmount, allyLoc);
        }
    }
}
