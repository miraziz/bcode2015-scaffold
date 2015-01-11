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
        int choice = (int)(Math.random() * 3);
        if (choice == 1)
        {
            farmArea = getLocation(Channel.farmLoc2);
        }
        else
        {
            farmArea = getLocation(Channel.farmLoc1);
        }
    }


    @Override
    public void run()
        throws GameActionException
    {
        mine();
    }
}
