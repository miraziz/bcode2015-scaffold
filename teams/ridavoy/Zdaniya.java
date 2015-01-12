package ridavoy;

import battlecode.common.*;

public abstract class Zdaniya
    extends Soveti
{
    public Zdaniya(RobotController rc)
    {
        super(rc);
        mLocation = rc.getLocation();
    }


    /**
     * Transfers supply away from HQ. Supplies nearby units and maintains a
     * minor stock pile.
     */
    @Override
    public void transferSupplies()
        throws GameActionException
    {
        // TODO: Implement
    }
}
