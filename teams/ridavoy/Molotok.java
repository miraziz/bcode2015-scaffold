package ridavoy;

import battlecode.common.*;

public class Molotok
    extends Proletariat
{

    public Molotok(RobotController rc)
    {
        super(rc);
        this.setDestination(this.enemyHQ);
    }


    @Override
    public void run()
        throws GameActionException
    {
        this.bug();
    }

}
