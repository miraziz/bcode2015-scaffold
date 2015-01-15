package ridavoy;

import battlecode.common.*;

/**
 * Barracks class.
 * 
 * @author Miraziz
 */
public class Kazarma
    extends Proizvodstvennoye
{

    public Kazarma(RobotController rc)
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {
        // TODO smarter barracks
        spawnToEnemy(RobotType.SOLDIER);
    }

}
