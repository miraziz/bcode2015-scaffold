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
                    if (rc.canSpawn(Direction.NORTH, RobotType.BEAVER))
                    {
                        rc.spawn(Direction.NORTH, RobotType.BEAVER);
                    }
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
