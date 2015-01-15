package yefreytor;

/**
 * Constants used for channel communication.
 * 
 * @author Miraziz
 */
public class Channels
{
    // unit counts
    static int beaverCount       = 0;
    static int minerCount        = 1;
    static int droneCount        = 2;
    static int tankCount         = 3;
    static int soldierCount      = 4;
    static int basherCount       = 5;

    // building counts
    static int barracksCount     = 10;
    static int minerFactoryCount = 11;
    static int helipadCount      = 12;
    static int tankFactoryCount  = 13;

    // commands
    static int beaverTasksTaken  = 49;
    static int beaverTask1       = 50; // channels 50-? are tasks needed to be
                                        // done in order

    static int rallyLoc          = 36;
    static int buildLoc          = 37;

    static int miningTotal       = 200;

    static int buildPathLength   = 298;
    static int buildPathCount    = 299;
    static int buildPath         = 300; // can span up to 30ish spots

}