package team025;

import battlecode.common.*;

/**
 * Miner class.
 * 
 * @author Miraziz
 */
public class Serp
    extends Proletariat
{

    public Serp(RobotController rc)
        throws GameActionException
    {
        super(rc);
        reachedFarm = false;
    }


    /**
     * Mines.
     */
    @Override
    public void run()
        throws GameActionException
    {
        mine();
    }
}
