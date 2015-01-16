package yefreytor;

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

    private MapLocation             dest;
    private Boolean                 onWall;
    protected Direction             facing;
    private LinkedList<MapLocation> helper; // using for
// experimenting something, may be completely useless
    private HashSet<MapLocation>    visited; // only necessary for
// very specific cases i think.


    public Proletariat(RobotController rc)
        throws GameActionException
    {
        super(rc);
        visited = new HashSet<MapLocation>();
        helper = new LinkedList<MapLocation>();
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
                if (isNormalTile(facing.rotateRight().rotateRight())
                    || isOffMap(facing.rotateRight().rotateRight()))
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
            facing = rc.getLocation().directionTo(dest);
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


    private boolean isOffMap(Direction dir)
    {
        MapLocation loc = mLocation.add(dir);
        if (rc.senseTerrainTile(loc) == TerrainTile.OFF_MAP)
        {
            return true;
        }
        return false;
    }


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
        double totSupply = rc.getSupplyLevel();

        RobotInfo[] nearbyAllies =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);

        double beaverSupply = Math.min(200, totSupply);
        MapLocation beaverLoc = null;

        // TODO If someone is dying, give away supply

        // TODO If someone is dying, give away supply

        double attackSupply = totSupply;
        MapLocation allyLoc = null;
        for (RobotInfo teamMember : nearbyAllies)
        {
            double teamSupply = teamMember.supplyLevel;
            if (teamMember.type == RobotType.BEAVER)
            {
                if (teamSupply < 200 && teamSupply < beaverSupply)
                {
                    beaverSupply = teamSupply;
                    beaverLoc = teamMember.location;
                }
            }
            else if (teamMember.supplyLevel < attackSupply
                && isAttackingUnit(teamMember.type))
            {
                attackSupply = teamMember.supplyLevel;
                allyLoc = teamMember.location;
            }
        }

        if (allyLoc != null)
        {
            int transferAmount = (int)((totSupply - attackSupply) / 2.0);
            if (rc.getType() == RobotType.BEAVER && totSupply > 200)
            {
                transferAmount = (int)(totSupply - 200);
            }

            rc.transferSupplies(transferAmount, allyLoc);
        }

        if (beaverLoc != null)
        {
            if (totSupply > beaverSupply)
            {
                int transferAmount = (int)((totSupply - beaverSupply) / 2.0);
                rc.transferSupplies(transferAmount, beaverLoc);
            }
        }
    }
}
