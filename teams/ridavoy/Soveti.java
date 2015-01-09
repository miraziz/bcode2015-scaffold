package ridavoy;

import battlecode.common.*;

public abstract class Soveti
{
    protected RobotController rc;


    public Soveti(RobotController rcc)
    {
        rc = rcc;
    }


    public abstract void run();
}
