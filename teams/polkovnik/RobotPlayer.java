package polkovnik;

import battlecode.common.*;

/**
 * Main robot class file. Assigns the robot type and runs the robot.
 * 
 * @author Amit Bachchan
 */
public class RobotPlayer
{
    public static void run(RobotController rc)
        throws GameActionException
    {
        Soveti robot = null;
        if (rc.getType() == RobotType.HQ)
        {
            robot = new HQ(rc);
        }
        else if (rc.getType() == RobotType.TOWER)
        {
            robot = new Tower(rc);
        }
        else if (rc.getType() == RobotType.AEROSPACELAB)
        {
            robot = new AerospaceLab(rc);
        }
        else if (rc.getType() == RobotType.LAUNCHER)
        {
            robot = new SimpleLauncher(rc);
        }
        else if (rc.getType() == RobotType.MISSILE)
        {
            robot = new SimpleMissile(rc);
        }
        else if (rc.getType() == RobotType.SUPPLYDEPOT)
        {
            robot = new SupplyDepot(rc);
        }
        else if (rc.getType() == RobotType.MINERFACTORY)
        {
            robot = new MinerFactory(rc);
        }
        else if (rc.getType() == RobotType.BARRACKS)
        {
            robot = new Barracks(rc);
        }
        else if (rc.getType() == RobotType.TANKFACTORY)
        {
            robot = new TankFactory(rc);
        }
        else if (rc.getType() == RobotType.TANK)
        {
            robot = new Tank(rc);
        }
        else if (rc.getType() == RobotType.BEAVER)
        {
            robot = new Beaver(rc);
        }
        else if (rc.getType() == RobotType.MINER)
        {
            robot = new Miner(rc);
        }
        else if (rc.getType() == RobotType.SOLDIER)
        {
            robot = new Soldier(rc);
        }
        else if (rc.getType() == RobotType.HELIPAD)
        {
            robot = new Helipad(rc);
        }
        else if (rc.getType() == RobotType.DRONE)
        {
            robot = new SupplyDrone(rc);
        }
        else if (rc.getType() == RobotType.BASHER)
        {
            robot = new Basher(rc);
        }
        else if (rc.getType() == RobotType.TECHNOLOGYINSTITUTE)
        {
            robot = new TechnologyInstitute(rc);
        }
        else if (rc.getType() == RobotType.TRAININGFIELD)
        {
            robot = new TrainingField(rc);
        }
        else if (rc.getType() == RobotType.COMMANDER)
        {
            robot = new Commander(rc);
        }
        else if (rc.getType() == RobotType.HANDWASHSTATION)
        {
            robot = new HandwashStation(rc);
        }
        else if (rc.getType() == RobotType.COMPUTER)
        {
            robot = new Computer(rc);
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
// e.printStackTrace();
            }
// int endRound = Clock.getRoundNum();
// if (endRound != currentRound && rc.getType() != RobotType.MINER)
// {
// System.out.println("Used up all bytecodes: "
// + (endRound - currentRound));
// }
            rc.yield();
        }
    }
}
