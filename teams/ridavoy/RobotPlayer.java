package ridavoy;

import battlecode.common.*;

public class RobotPlayer
{
    public void run(RobotController rc)
    {
        Soveti robot = null;
        if (rc.getType() == RobotType.HQ)
        {
            robot = new Shtab(rc);
        }
        else if (rc.getType() == RobotType.TOWER)
        {
            robot = new Bashna(rc);
        }
        else if (rc.getType() == RobotType.SUPPLYDEPOT)
        {
            robot = new Zastava(rc);
        }
        else if(rc.getType() == RobotType.MINERFACTORY)
        {
            robot = new Ferma(rc);
        }
        else if (rc.getType() == RobotType.BARRACKS)
        {
            robot = new Kazarma(rc);
        }
        else if (rc.getType() == RobotType.BEAVER)
        {
            robot = new Molotok(rc);
        }
        else if(rc.getType() == RobotType.MINER)
        {
            robot = new Serp(rc);
        }
        else if (rc.getType() == RobotType.SOLDIER)
        {
            robot = new Prizyvnik(rc);
        }

        while (true)
        {
            try
            {
                robot.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}
