package ridavoy;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Helipad class.
 * 
 * @author Miraziz
 */
public class Ploshchadka
    extends Proizvodstvennoye
{

    public Ploshchadka(RobotController rc)
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {
        spawnToEnemy(RobotType.DRONE);
    }

}
