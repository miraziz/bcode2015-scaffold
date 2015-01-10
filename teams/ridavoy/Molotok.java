package ridavoy;

import battlecode.common.*;

public class Molotok
    extends Proletariat
{

    public Molotok(RobotController rc)
        throws GameActionException
    {
        super(rc);
        this.setDestination(enemyHQ);
    }


    @Override
    public void run()
        throws GameActionException
    {
        this.bug();
    }

}
