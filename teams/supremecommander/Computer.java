package supremecommander;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Computer
    extends Proletariat
{

    public Computer(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
    {
        rc.disintegrate();
    }

}
