package ridavoy;

public class Constants
{
    // TODO: If tower goes down, update some channel through HQ and have every
// unit update their tower

    // Rally points
    public static int bestFarmScore = 3;
    public static int bestFarmX     = 4;
    public static int bestFarmY     = 5;

    // other constants

    static int        beaverLimit   = 2;
    static int        minerLimit    = 20;

    // Production rates in ore/turn
    static double     barracksRate;
    static double     tankFactoryRate;
    static double     helipadRate   = 125 / 30;


    static BeaverTask getTask(int value)
    {
        if (value == 0)
        {
            return BeaverTask.BUILD_BARRACKS;
        }
        else if (value == 1)
        {
            return BeaverTask.BUILD_MINERFACTORY;
        }
        else if (value == 2)
        {
            return BeaverTask.BUILD_TANKFACTORY;
        }
        else if (value == 3)
        {
            return BeaverTask.BUILD_SUPPLYDEPOT;
        }
        else if (value == 4)
        {
            return BeaverTask.BUILD_HELIPAD;
        }
        else if (value == 5)
        {
            return BeaverTask.MINE;
        }
        return BeaverTask.JOIN_ARMY;
    }
}
