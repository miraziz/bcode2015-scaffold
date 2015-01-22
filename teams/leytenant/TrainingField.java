package leytenant;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TrainingField
    extends Building
{

    public TrainingField(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {

        if (!rc.hasCommander())
        {
            this.spawnToEnemy(RobotType.COMMANDER);
        }
    }

}
