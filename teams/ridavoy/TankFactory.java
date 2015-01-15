package ridavoy;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TankFactory
    extends Proizvodstvennoye
{

    public TankFactory(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        spawnToEnemy(RobotType.TANK);
    }

}
