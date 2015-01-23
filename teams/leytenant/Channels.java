package leytenant;

/**
 * Constants used for channel communication.
 * 
 * @author Amit Bachchan
 */
public class Channels
{
    static int        attacking             = 5000;

    // unit counts
    static int        beaverCount           = 0;
    static int        minerCount            = 1;
    static int        droneCount            = 2;
    static int        tankCount             = 3;
    static int        soldierCount          = 4;
    static int        basherCount           = 5;

    // building counts
    static int        barracksCount         = 10;
    static int        minerFactoryCount     = 11;
    static int        helipadCount          = 12;
    static int        tankFactoryCount      = 13;
    static int        aerospaceCount        = 14;

    // Enemy Info
    static int        destroyedTower        = 20;
    static int        highestEnemyHealth    = 21;
    static int        highestEnemyHealthLoc = 22;

    // unit controls
    static int        shouldSpawnSoldier    = 25;
    static int        shouldSpawnBasher     = 26;
    static int        shouldSpawnDrone      = 27;

    // supply stuff
    static int        supplyLoc             = 30;
    static int        supplyPriority        = 31;
    static int        supplyDistance        = 32;

    // commands
    static int        beaverTasksTaken      = 49;
    static int        beaverTask1           = 50;  // channels 50-? are tasks
// needed
// to
// be done in order

    static int        rallyLoc              = 36;
    static int        buildLoc              = 37;

    static int        miningTotal           = 200;

    static int        buildPathLength       = 298;
    static int        buildPathCount        = 299;
    static int        buildPath             = 300; // can span up to 30ish
// spots

    // Tower analysis
    static int        towerVulnerability    = 990;

    public static int minerPotato           = 1337;

    public static int launcherCount         = 1500;
}
