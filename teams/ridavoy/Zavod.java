package ridavoy;

import battlecode.common.*;

/**
 * Tank factory class.
 * 
 * @author Miraziz
 */
public class Zavod
    extends Proizvodstvennoye
{

    public Zavod(RobotController rc)
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {
        // TODO: Smarter factory
        spawnToEnemy(RobotType.TANK);
    }

}
