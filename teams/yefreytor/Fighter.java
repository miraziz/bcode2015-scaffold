package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Fighter (mobile) class.
 * 
 * @author Amit Bachchan
 */
public class Fighter
    extends Proletariat
{

    /**
     * Sets rally point.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Fighter(RobotController rc)
        throws GameActionException
    {
        super(rc);

        setDestination(getLocation(Channels.rallyLoc));
    }


    /**
     * Attacks if possible, or bugs otherwise.
     */
    @Override
    public void run()
        throws GameActionException
    {
        super.run();

        // TODO Stop them from moving when they're in a clump to avoid wasting
// supply
        this.setDestination(getLocation(Channels.rallyLoc));
        rc.setIndicatorString(1, "Boyets run method");
        rc.setIndicatorString(0, "Traveling to: "
            + getLocation(Channels.rallyLoc));
        if (!attack())
        {
            bug();
        }
    }
}
