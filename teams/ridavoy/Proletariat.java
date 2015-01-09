package ridavoy;

import java.util.HashSet;
import battlecode.common.*;

public abstract class Proletariat
    extends Soveti
{
    // super class for mobile units

    private MapLocation          dest;
    private Boolean              onWall;
    private Direction            facing;
    private HashSet<MapLocation> visited;


    public Proletariat(RobotController rc)
    {
        super(rc);
        visited = new HashSet<MapLocation>();
        onWall = false;
        dest = null;
    }


    protected void setDestination(MapLocation loc)
    {
        dest = loc;
        facing = rc.getLocation().directionTo(dest);
        visited.clear();
    }


    /**
     * returns true if a move was made in the direction given, returns false if
     * no move was made
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
     * moves towards the dest variable and bugs around walls in the way returns
     * All rotations are currently to the left.
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
        TerrainTile tileAhead =
            rc.senseTerrainTile(rc.getLocation().add(facing));
        // if we are already attached and traveling along a wall
        if (onWall)
        {
            // there is a wall ahead of us, sets onWall to false, so that the
            // next code will run
            if (tileAhead != TerrainTile.NORMAL
                || visited.contains(rc.getLocation().add(facing)))
            {
                onWall = false;
            }
            else
            {
                MapLocation right =
                    rc.getLocation().add(facing.rotateRight().rotateRight());
                if (rc.senseTerrainTile(right) == TerrainTile.NORMAL
                    && !visited.contains(right))
                {
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
            while (tileAhead != TerrainTile.NORMAL
                || visited.contains(rc.getLocation().add(facing)) && count < 8)
            {
                count++;
                facing = facing.rotateLeft();
                onWall = true;
                tileAhead = rc.senseTerrainTile(rc.getLocation().add(facing));
            }
        }
        if (move(facing))
        {
            visited.add(rc.getLocation());
            return true;
        }
        else
        {
            return false;
        }
    }
}
