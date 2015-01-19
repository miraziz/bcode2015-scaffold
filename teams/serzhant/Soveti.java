package serzhant;

import java.util.Random;
import battlecode.common.*;

/**
 * Supreme Soviet.
 * 
 * @author Amit Bachchan
 */
public abstract class Soveti
{
    protected RobotController          rc;

    protected MapLocation              mLocation;
    protected int                      mapOffsetX;
    protected int                      mapOffsetY;
    protected int                      broadcastO;

    protected RobotType                mType;
    protected Team                     myTeam;
    protected Team                     enemyTeam;
    protected MapLocation              allyHQ;
    protected MapLocation              enemyHQ;
    protected MapLocation[]            enemyTowers;
    protected MapLocation[]            allyTowers;
    protected Random                   rand;

    protected static final Direction[] directions = Direction.values();


    /**
     * Saves known locations of towers and HQs, initializes map broadcast
     * parameters and determines this robot's current location.
     * 
     * @param myRC
     */
    public Soveti(RobotController myRC)
    {
        rc = myRC;
        rand = new Random(rc.getID());

        mLocation = rc.getLocation();
        mType = rc.getType();

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        enemyTowers = rc.senseEnemyTowerLocations();
        allyTowers = rc.senseTowerLocations();

        mapOffsetX = allyHQ.x - GameConstants.MAP_MAX_WIDTH;
        mapOffsetY = allyHQ.y - GameConstants.MAP_MAX_HEIGHT;
        broadcastO = GameConstants.MAP_MAX_HEIGHT * 2;
    }


    /**
     * Robot's main running loop.
     * 
     * @throws GameActionException
     */
    public abstract void run()
        throws GameActionException;


    /**
     * Transfers supplies from this unit to nearby allied units.
     * 
     * @throws GameActionException
     */
    public abstract void transferSupplies()
        throws GameActionException;


    /**
     * Attacks the lowest HP enemy unit in this robot's attack range.
     * 
     * @return True if the attack is successful, false otherwise.
     * @throws GameActionException
     */
    public boolean attack()
        throws GameActionException
    {
        if (rc.isWeaponReady())
        {
            RobotInfo[] nearbyEnemies =
                rc.senseNearbyRobots(
                    rc.getType().attackRadiusSquared,
                    enemyTeam);

            RobotInfo target = null;
            if (nearbyEnemies.length > 0 && rc.isWeaponReady())
            {
                for (RobotInfo ri : nearbyEnemies)
                {
                    if (target == null || ri.health < target.health)
                    {
                        target = ri;
                    }

                    // TODO Probably not a good idea to only target the
// strongest unit in the area
                    if (ri.type == RobotType.HQ || ri.type == RobotType.TOWER)
                    {
                        target = ri;
                        break;
                    }
                }
                if (rc.canAttackLocation(target.location))
                {
                    if (target.type == RobotType.TOWER
                        && target.health <= rc.getType().attackPower)
                    {
                        broadcastLocation(
                            Channels.destroyedTower,
                            target.location);
                    }
                    rc.attackLocation(target.location);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Broadcasts a location to a single channel by putting the x's and y's
     * offset from the HQ in the broadcast.
     * 
     * @throws GameActionException
     */
    protected void broadcastLocation(int channel, MapLocation loc)
        throws GameActionException
    {
        int x = (loc.x - mapOffsetX);
        int y = (loc.y - mapOffsetY);

        int broadcast = broadcastO * x + y;
        rc.broadcast(channel, broadcast);
    }


    /**
     * Get a location from the specified channel.
     * 
     * @param channel
     * @return location of the broadcasted channel
     * @throws GameActionException
     */
    protected MapLocation getLocation(int channel)
        throws GameActionException
    {
        MapLocation res = null;

        int num = rc.readBroadcast(channel);
        if (num > 0)
        {
            int x = num / broadcastO + mapOffsetX;
            int y = num % broadcastO + mapOffsetY;
            res = new MapLocation(x, y);
        }

        return res;
    }


    /**
     * Returns true if the given type is a mobile robot capable of attacking.
     * 
     * @param type
     *            Specified robot type.
     * @return True if an attacking unit, false otherwise.
     */
    protected boolean isAttackingUnit(RobotType type)
    {
        return type == RobotType.DRONE || type == RobotType.BASHER
            || type == RobotType.TANK || type == RobotType.SOLDIER
            || type == RobotType.LAUNCHER || type == RobotType.COMMANDER;
    }


    protected boolean isSupplyingUnit(RobotType type)
    {
        return type == RobotType.SOLDIER || type == RobotType.TANK
            || type == RobotType.MINER || type == RobotType.LAUNCHER;
    }


    protected boolean isProductionBuilding(RobotType type)
    {
        return type == RobotType.BARRACKS || type == RobotType.MINERFACTORY
            || type == RobotType.TANKFACTORY || type == RobotType.HELIPAD
            || type == RobotType.AEROSPACELAB || type == RobotType.HQ;
    }


    /**
     * Returns a random direction.
     * 
     * @return a random direction.
     */
    protected Direction getRandomDirection()
    {
        return Direction.values()[rand.nextInt(8)];
    }

}
