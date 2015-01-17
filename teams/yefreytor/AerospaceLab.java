package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class AerospaceLab
    extends Proizvodstvennoye
{

    public AerospaceLab(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        this.spawnToEnemy(RobotType.LAUNCHER);
    }

}
