package ridavoy;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Helipad
    extends Proizvodstvennoye
{

    public Helipad(RobotController rc)
    {
        super(rc);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void run()
        throws GameActionException
    {
        this.spawnUnit(RobotType.DRONE);

    }

}
