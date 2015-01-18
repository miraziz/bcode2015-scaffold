package yefreytor;

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

    // other constants
    static int              beaverLimit             = 2;
    static int              minerLimit              = 30;

    // Production rates in ore/turn
    static double           barracksRate            = 125 / 30;
    static double           tankFactoryRate         = 125 / 30;
    static double           helipadRate             = 125 / 30;
}
