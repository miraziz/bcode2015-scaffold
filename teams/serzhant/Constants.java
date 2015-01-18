package serzhant;

import battlecode.common.RobotType;

/**
 * Constants for the game.
 * 
 * @author Amit Bachchan
 */
public class Constants
{
    // TODO: If tower goes down, update some channel through HQ and have every
    // unit update their tower

    public static final int BUILD_PATH_BYTECODES    = 500;

    public static final int CHANNELS_PER_TOWER_VULN = 3;

    public static final int MAXIMUM_BUILDINGS       = 101;

    // other constants
    static int              beaverLimit             = 1;
    static int              minerLimit              = 30;
    static int              soldierLimit            = 12;
    static int              droneLimit              = 1;

    static int              tankCost                = RobotType.TANK.oreCost;
    static int              launcherCost            =
                                                        RobotType.LAUNCHER.oreCost;
    static int              soldierCost             = RobotType.SOLDIER.oreCost;

    // Production rates in ore/turn
    static double           barracksRate            = 125 / 30;
    static double           tankFactoryRate         = 125 / 30;
    static double           helipadRate             = 125 / 30;

    static int              attackRound             = 1500;
}
