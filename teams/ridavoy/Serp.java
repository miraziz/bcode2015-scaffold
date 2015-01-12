package ridavoy;

import battlecode.common.*;

public class Serp
    extends Proletariat
{

    public Serp(RobotController rc)
        throws GameActionException
    {
        super(rc);
        reachedFarm = false;

    }


    @Override
    public void run()
        throws GameActionException
    {
        mine();
    }
}
