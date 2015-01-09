package ridavoy;

import java.util.HashSet;
import battlecode.common.*;

public abstract class Proletariat
    extends Soveti
{
    // super class for mobile units

    protected MapLocation        dest;

    private Boolean              onWall;
    private Direction            wallDir;
    private Direction            facing;
    private HashSet<MapLocation> visited;


    public Proletariat(RobotController rc)
    {
        super(rc);
        visited = new HashSet<MapLocation>();
        onWall = false;
        dest = null;
    }


    /**
     * moves towards the dest variable returns true if a move made was made.
     * returns false if no move call was made.
     */
    public boolean bug()
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
            // there is nothing ahead, check if we should stay on the wall or
            // jump off
            if (tileAhead != TerrainTile.NORMAL
                || visited.contains(rc.getLocation().add(facing)))
            {

            }
            //
            else
            {

            }
        }
        // not against a wall, move towards enemy and bug if necessary
        else
        {

        }
        return false;
    }
    
    
}
