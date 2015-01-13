package ridavoy;

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

    }


    /**
     * Mines.
     */
    @Override
    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.minerCount,
            rc.readBroadcast(Channels.minerCount) + 1);
        mine();
    }
}
