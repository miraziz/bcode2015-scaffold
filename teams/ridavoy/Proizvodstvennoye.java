package ridavoy;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public abstract class Proizvodstvennoye
    extends Zdaniya
{

    public Proizvodstvennoye(RobotController rc)
    {
        super(rc);
    }


    public boolean spawnUnit(RobotType type)
        throws GameActionException
    {
        Direction dir = mLocation.directionTo(enemyHQ);
        int count = 0;
        if (!rc.isCoreReady())
        {
            return false;
        }
        while (!rc.canSpawn(dir, type) && count < 8)
        {
            dir = dir.rotateRight();
            count++;
        }
        if (count < 8)
        {
            rc.spawn(dir, type);
            return true;
        }
        return false;
    }

}
