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
            try
            {
                if (rc.getType() == RobotType.HQ)
                {
                    rc.spawn(Direction.NORTH, RobotType.BEAVER);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}
