package ridavoy;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

/**
 * Attacking building class.
 * 
 * @author Miraziz
 */
public abstract class Atakuyushchiy
    extends Zdaniya
{

    /**
     * Broadcasts the building's score.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Atakuyushchiy(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    /**
     * Attempts to attack and then transfers supplies to nearby allies.
     */
    @Override
    public void run()
        throws GameActionException
    {
        this.attack();
    }
}
