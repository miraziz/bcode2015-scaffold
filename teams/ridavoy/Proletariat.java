package ridavoy;

import java.util.HashSet;
import java.util.LinkedList;
import battlecode.common.*;

public abstract class Proletariat
    extends Soveti
{
    // super class for mobile units

    protected boolean               reachedFarm;
    protected MapLocation           farmArea;
    private MapLocation             dest;
    private Boolean                 onWall;
    protected Direction             facing;
    private LinkedList<MapLocation> helper;     // using for experimenting
                                                 // something, may be completely
                                                 // useless
    private HashSet<MapLocation>    visited;    // only necessary for very
// specific
                                                 // cases i think.


    public Proletariat(RobotController rc)
        throws GameActionException
    {
        super(rc);
        visited = new HashSet<MapLocation>();
        helper = new LinkedList<MapLocation>();
        onWall = false;
        dest = null;
        reachedFarm = true;
    }


    protected void setDestination(MapLocation loc)
    {
        if (dest != null && dest.equals(loc))
        {
            return;
        }
        dest = loc;
        facing = rc.getLocation().directionTo(dest);
        visited.clear();
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
                    facing = rc.getLocation().directionTo(dest);
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
            visited.add(rc.getLocation());
            helper.addLast(rc.getLocation());
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
        MapLocation loc = rc.getLocation().add(dir);
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
        rc.broadcast(
            Channel.minerCount,
            rc.readBroadcast(Channel.minerCount) + 1);
        // if not in any danger
        if (rc.isCoreReady())
        {
            if (reachedFarm)
            {
                if (rc.canMine() && rc.senseOre(rc.getLocation()) > 0)
                {
                    double oreCount = rc.getTeamOre();
                    rc.mine();
                    oreCount = rc.getTeamOre() - oreCount;
                    rc.broadcast(
                        Channel.miningTotal,
                        (int)(rc.readBroadcast(Channel.miningTotal) + oreCount));
                }
                else
                {
                    Direction bestDir = Direction.NORTH;
                    double bestScore = 0;
                    Direction dir = Direction.NORTH;
                    for (int i = 0; i < 8; i++)
                    {
                        if (rc.canMove(dir))
                        {
                            double oreCount =
                                rc.senseOre(rc.getLocation().add(dir));
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
                        this.setDestination(allyHQ);
                        bug();
                    }
                    else
                    {
                        move(bestDir);
                    }
                }
            }
            else
            {
                if (rc.canMine() && rc.senseOre(rc.getLocation()) > 1)
                {
                    double oreCount = rc.getTeamOre();
                    rc.mine();
                    oreCount = rc.getTeamOre() - oreCount;
                    rc.broadcast(
                        Channel.miningTotal,
                        (int)(rc.readBroadcast(Channel.miningTotal) - oreCount));
                }
                else
                {
                    this.setDestination(farmArea);
                    bug();
                    if (farmArea.distanceSquaredTo(rc.getLocation()) <= 25)
                    {
                        reachedFarm = true;
                    }
                }
            }
        }
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {
        // TODO Auto-generated method stub

    }
}
