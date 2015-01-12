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
     * Gets the robot's current location at the start of every turn.
     */
    @Override
    public void run()
        throws GameActionException
    {
        mLocation = rc.getLocation();
    }


    /**
     * Moves towards the dest variable returns true if a move made was made.
     * returns false if no move call was made.
     * 
     * @return
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


    /**
     * Shares supply evenly between this unit and its least supplied ally within
     * range.
     */
    @Override
    public void transferSupplies()
        throws GameActionException
    {
        RobotInfo[] nearbyAllies =
            rc.senseNearbyRobots(
                rc.getLocation(),
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                rc.getTeam());

        double lowestSupply = rc.getSupplyLevel();
        double transferAmount = 0;
        MapLocation suppliesToThisLocation = null;
        for (RobotInfo ri : nearbyAllies)
        {
            if (ri.supplyLevel < lowestSupply)
            {
                lowestSupply = ri.supplyLevel;
                transferAmount = (rc.getSupplyLevel() - ri.supplyLevel) / 2;
                suppliesToThisLocation = ri.location;
            }
        }

        if (suppliesToThisLocation != null)
        {
            rc.transferSupplies((int)transferAmount, suppliesToThisLocation);
        }
    }

}
