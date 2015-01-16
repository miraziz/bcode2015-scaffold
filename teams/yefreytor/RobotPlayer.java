package yefreytor;

import battlecode.common.*;

public class RobotPlayer
{
    public static void run(RobotController rc)
        throws GameActionException
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
        else if (rc.getType() == RobotType.MINERFACTORY)
        {
            robot = new Ferma(rc);
        }
        else if (rc.getType() == RobotType.BARRACKS)
        {
            robot = new Kazarma(rc);
        }
        else if (rc.getType() == RobotType.TANKFACTORY)
        {
            robot = new Zavod(rc);
        }
        else if (rc.getType() == RobotType.TANK)
        {
            robot = new Tank(rc);
        }
        else if (rc.getType() == RobotType.BEAVER)
        {
            robot = new Molotok(rc);
        }
        else if (rc.getType() == RobotType.MINER)
        {
            robot = new Serp(rc);
        }
        else if (rc.getType() == RobotType.SOLDIER)
        {
            robot = new Prizyvnik(rc);
        }
        else if (rc.getType() == RobotType.HELIPAD)
        {
            robot = new Ploshchadka(rc);
        }
        else if (rc.getType() == RobotType.DRONE)
        {
            robot = new Vertolet(rc);
        }

        while (true)
        {
            try
            {
                robot.run();
                 robot.transferSupplies();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            rc.yield();
        }
    }
}
