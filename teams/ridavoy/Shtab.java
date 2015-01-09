package ridavoy;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Shtab
    extends Atakuyushchiy
{

    public Shtab(RobotController rc)
        throws GameActionException
    {
        super(rc);
        rc.spawn(Direction.SOUTH, RobotType.BEAVER);
    }


    @Override
    public void run()
    {
        // TODO Auto-generated method stub

    }
}
