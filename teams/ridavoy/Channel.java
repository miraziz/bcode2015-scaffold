package ridavoy;

// stores broadcast channels
enum BeaverTask
{
    BUILD_BARRACKS(0),
    BUILD_MINERFACTORY(1),
    BUILD_TANKFACTORY(2),
    BUILD_SUPPLYDEPOT(3),
    BUILD_HELIPAD(4),
    MINE(5),
    JOIN_ARMY(6);

    private int value;


    BeaverTask(int id)
    {
        value = id;
    }


    int value()
    {
        return value;
    }
}




public class Channel
{
    // unit counts
    static int beaverCount       = 0;
    static int minerCount        = 1;
    static int droneCount        = 2;
    static int tankCount         = 3;
    static int soldierCount      = 4;
    static int basherCount       = 5;

    // building counts
    static int barracksCount     = 20;
    static int minerFactoryCount = 21;
    static int helipadCount      = 22;
    static int tankFactoryCount  = 23;

    // commands
    static int beaverTask1       = 50; // channels 51-? are tasks needed to be
                                        // done in order
    static int beaverTasksTaken  = 49;

    // other
    static int farmScore1        = 30;
    static int farmScore2        = 32;
    static int farmScore3        = 34;

    // locations
    static int farmLoc1          = 31;
    static int farmLoc2          = 33;
    static int farmLoc3          = 35;
    static int rallyLoc          = 36;
    static int buildLoc          = 37;

    static int miningTotal       = 200;
}
