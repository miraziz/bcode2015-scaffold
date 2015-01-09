package ridavoy;

import battlecode.common.*;

public class Bashna
    extends Atakuyushchiy
{

    public Bashna(RobotController rc)
    {
        super(rc);
    }


    @Override
    public void run()
        throws GameActionException
    {

        this.attack();

    }

}
