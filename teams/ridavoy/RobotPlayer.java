package ridavoy;

import battlecode.common.*;

public class RobotPlayer
{
    public void run(RobotController rc)
    {
        Robot robot = null;
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
        else if (rc.getType() == RobotType.BARRACKS)
        {
            robot = new Kazarma(rc);
        }
        else if (rc.getType() == RobotType.BEAVER)
        {
            robot = new Proletariat(rc);
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
