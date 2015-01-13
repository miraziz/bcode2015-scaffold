package ridavoy;

/**
 * Constants for the game.
 * 
 * @author Miraziz
 */
public class Constants
{
    // TODO: If tower goes down, update some channel through HQ and have every
// unit update their tower

    // Rally points
    public static int bestFarmScore   = 3;
    public static int bestFarmX       = 4;
    public static int bestFarmY       = 5;

    // other constants

    static int        beaverLimit     = 2;
    static int        minerLimit      = 20;

    // Production rates in ore/turn
    static double     barracksRate    = 125 / 30;
    static double     tankFactoryRate = 125 / 30;
    static double     helipadRate     = 125 / 30;
}
