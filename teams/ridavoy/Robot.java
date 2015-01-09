package ridavoy;

import battlecode.common.*;

public abstract class Robot
{
    protected RobotController rc;


    public Robot(RobotController rcc)
    {
        rc = rcc;
    }


    public abstract void run();
}
