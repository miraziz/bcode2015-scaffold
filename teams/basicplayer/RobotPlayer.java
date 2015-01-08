package basicplayer;

import battlecode.common.*;

public class RobotPlayer
{
    private static RobotController rc;


    public void run(RobotController rcc)
    {
        rc = rcc;

        while (true)
        {
            rc.yield();
        }
    }
}
