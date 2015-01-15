package yefreytor;

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
        throws GameActionException
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
