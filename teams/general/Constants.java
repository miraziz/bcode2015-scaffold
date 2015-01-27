package general;

import battlecode.common.GameConstants;
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

    public static final int    BUILD_PATH_BYTECODES      = 500;

    public static final int    CHANNELS_PER_TOWER_VULN   = 3;

    public static final int    MAXIMUM_BUILDINGS         = 8;

    // TODO Figure out the math
    public static final int    MAP_WIDTH                 =
                                                             (int)(1.5 * GameConstants.MAP_MAX_WIDTH) + 10;
    public static final int    MAP_HEIGHT                =
                                                             (int)(1.5 * GameConstants.MAP_MAX_HEIGHT) + 10;

    public static final double POTATO_MIN_ORE            = 1;
    public static final double NORM_MIN_ORE              = 2;
    public static final double FAST_MIN_ORE              =
                                                             GameConstants.MINER_MINE_MAX
                                                                 * GameConstants.MINER_MINE_RATE;

    public static double       MIN_ORE                   = NORM_MIN_ORE;
    public static final int    BETTER_ORE_MIN_RANGE      = 3;
    public static final int    BETTER_ORE_MAX_RANGE      = 4;

    public static final int    PATH_MAX_FAILED_TRIES     = 2;

    public static final double PATH_ORE                  =
                                                             GameConstants.MINER_MINE_MAX
                                                                 * GameConstants.MINER_MINE_RATE;

    public static final int    TOT_MINER_FRACS           = 10;
    public static final double FAST_MINER_FRACS          = 1;
    public static final double NORM_MINER_FRACS          = 9 + FAST_MINER_FRACS;

    public static final int    MISSILE_MAX_RANGE_SQUARED = 49;

    // DPR = Damage per round
    public static final double COMMANDER_DPR             = 10;

    public static final int    MISSILE_MAX_RANGE         = 7;

    public static final int    ENEMY_NEAR_LAUNCHER       = 2;

    public static final int    ALLY_NEAR_LAUNCHER        = 3;

    public static final int    MAP_OUT_OF_ORE            = 1555;

    static int                 beaverLimit               = 1;
    static int                 soldierLimit              = 6;
    static int                 minerLimit                = 40;
    static int                 droneLimit                = 1;

    static int                 tankCost                  =
                                                             RobotType.TANK.oreCost;
    static int                 launcherCost              =
                                                             RobotType.LAUNCHER.oreCost;
    static int                 soldierCost               =
                                                             RobotType.SOLDIER.oreCost;

    // Production rates in ore/turn
    static double              barracksRate              = 125 / 30;
    static double              tankFactoryRate           = 125 / 30;
    static double              helipadRate               = 125 / 30;

    static int                 attackRound               = 1800;
    static int                 requiredTanksForAttack    = 18;

    static int                 towerAttackRound          = 1900;
}
