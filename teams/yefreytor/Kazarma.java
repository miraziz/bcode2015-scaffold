package yefreytor;

import battlecode.common.*;

/**
 * Barracks class.
 * 
 * @author Amit Bachchan
 */
public class Kazarma
    extends Proizvodstvennoye
{

    public Kazarma(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    /**
     * Builds a soldier whenever possible.
     */
    @Override
    public void run()
        throws GameActionException
    {
        // TODO smarter barracks
        if (rc.readBroadcast(Channels.shouldSpawnSoldier) == 1)
        {
            spawnToEnemy(RobotType.SOLDIER);
        }
        else if (rc.readBroadcast(Channels.shouldSpawnBasher) == 1)
        {
            spawnToEnemy(RobotType.BASHER);
        }
    }

}
