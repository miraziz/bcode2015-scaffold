package podpolkovnik;

import battlecode.common.*;

/**
 * Production building class.
 * 
 * @author Amit Bachchan
 */
public abstract class ProductionBuilding
    extends Building
{

    public ProductionBuilding(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }
}
