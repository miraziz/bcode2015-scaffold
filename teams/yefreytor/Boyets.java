package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Fighter (mobile) class.
 * 
 * @author Miraziz
 */
public class Boyets
    extends Proletariat
{

    /**
     * Sets rally point.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Boyets(RobotController rc)
        throws GameActionException
    {
        super(rc);

        setDestination(getLocation(Channels.rallyLoc));
    }


    @Override
    public void run()
        throws GameActionException
    {
        super.run();
        if (!attack())
        {
            bug();
        }
    }
}
