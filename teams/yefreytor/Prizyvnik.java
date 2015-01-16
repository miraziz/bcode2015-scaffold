package yefreytor;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Soldier class.
 * 
 * @author Miraziz
 */
public class Prizyvnik
    extends Boyets
{

    public Prizyvnik(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        super.run();
        this.setDestination(getLocation(Channels.rallyLoc));
        bug();
    }
}
