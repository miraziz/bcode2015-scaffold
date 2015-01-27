package polkovnik;

/**
 * Constants used for channel communication.
 * 
 * @author Amit Bachchan
 */
public class Channels
{

    static int              attacking             = 55000;

    // unit counts
    static int              beaverCount           = 50000;
    static int              minerCount            = 50001;
    static int              droneCount            = 50002;
    static int              tankCount             = 50003;
    static int              soldierCount          = 50004;
    static int              basherCount           = 50005;

    // building counts
    static int              barracksCount         = 50010;
    static int              minerFactoryCount     = 50011;
    static int              helipadCount          = 50012;
    static int              tankFactoryCount      = 50013;
    static int              aerospaceCount        = 50014;

    // Enemy Info
    static int              destroyedTower        = 50020;
    static int              highestEnemyHealth    = 50021;
    static int              highestEnemyHealthLoc = 50022;

    // unit controls
    static int              shouldSpawnSoldier    = 50025;
    static int              shouldSpawnBasher     = 50026;
    static int              shouldSpawnDrone      = 50027;

    // supply stuff
    static int              supplyLoc             = 50030;
    static int              supplyPriority        = 50031;
    static int              supplyDistance        = 50032;

    // commands
    static int              beaverTasksTaken      = 50049;
    static int              beaverTask1           = 50050; // channels 50-?
// are
// tasks
// needed
// to
// be done in order

    static int              rallyLoc              = 50036;
    static int              buildLoc              = 50037;

    static int              miningTotal           = 50200;

    static int              buildPathLength       = 50298;
    static int              buildPathCount        = 50299;
    static int              buildPath             = 50300; // can span up to
// 30ish
// spots

    // Tower analysis
    static int              towerVulnerability    = 50990;

    public static int       minerPotato           = 51337;

    public static int       launcherCount         = 51500;

    public static final int NUMBER_OF_MISSILES    = 52000;
}
