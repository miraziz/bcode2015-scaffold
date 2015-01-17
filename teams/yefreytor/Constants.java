package yefreytor;

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

    // other constants
    static int    beaverLimit     = 2;
    static int    minerLimit      = 30;
    static int    soldierLimit    = 12;
    static int    droneLimit      = 1;

    static int    tankCost        = RobotType.TANK.oreCost;
    static int    launcherCost    = RobotType.LAUNCHER.oreCost;
    static int    soldierCost     = RobotType.SOLDIER.oreCost;

    // Production rates in ore/turn
    static double barracksRate    = 125 / 30;
    static double tankFactoryRate = 125 / 30;
    static double helipadRate     = 125 / 30;

    static int    attackRound     = 1500;
}
