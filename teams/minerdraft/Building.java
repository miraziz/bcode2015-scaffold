package minerdraft;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

class HQ
    extends AttackingBuilding
{
    public HQ(RobotController myRc)
        throws GameActionException
    {
        super(myRc);
    }


    public void runBot()
        throws GameActionException
    {
        this.BuildUnit(
            RobotType.BEAVER,
            Constants.BeaverCount,
            Constants.BeaverLimit);
        super.runBot();
    }
}




class Tower
    extends AttackingBuilding
{
    public Tower(RobotController myRc)
        throws GameActionException
    {
        super(myRc);
    }
}
