package yefreytor;

import java.util.Random;
import battlecode.common.*;

/**
 * Supreme Soviet.
 * 
 * @author Amit Bachchan
 */
public abstract class Soveti
{
    protected RobotController rc;
    protected MapLocation     mLocation;
    protected int             mapOffsetX;
    protected int             mapOffsetY;
    protected int             broadcastO;

    protected Team            myTeam;
    protected Team            enemyTeam;
    protected MapLocation     allyHQ;
    protected MapLocation     enemyHQ;
    protected MapLocation[]   enemyTowers;
    protected MapLocation[]   allyTowers;
    protected Random          rand;


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
            || type == RobotType.MINER || type == RobotType.BEAVER
            || type == RobotType.COMMANDER;
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


    /**
     * Returns the closest free direction at most 1 turn away from dir.
     * 
     * @param dir
     *            Goal direction.
     * @return The best direction or null otherwise.
     */
    protected Direction getFreeForwardDirection(Direction dir)
    {
        return getFreeDirection(dir, 3);
    }


    /**
     * Returns the closest free direction at most 2 turns away from dir.
     * 
     * @param dir
     *            Goal direction.
     * @return The best direction or null otherwise.
     */
    protected Direction getFreeStrafeDirection(Direction dir)
    {
        return getFreeDirection(dir, 5);
    }


    /**
     * Returns the closest free direction to dir.
     * 
     * @param dir
     *            Goal direction.
     * @return The best direction or null otherwise.
     */
    protected Direction getFreeDirection(Direction dir)
    {
        return getFreeDirection(dir, 8);
    }


    /**
     * Returns the closest free direction to dir at most (turns / 2) turns away.
     * 
     * @param dir
     *            Goal direction.
     * @param turns
     *            Possible number of directions that can be returned.
     * @return The best free direction or null otherwise.
     */
    protected Direction getFreeDirection(Direction dir, int turns)
    {
        Direction left = dir;
        Direction right = dir;
        int count = 0;
        while (!rc.canMove(dir) && count < turns)
        {
            if (count % 2 == 0)
            {
                left = left.rotateLeft();
                dir = left;
            }
            else
            {
                right = right.rotateRight();
                dir = right;
            }
            count++;
        }
        if (count < turns)
        {
            return dir;
        }
        else
        {
            return null;
        }
    }
}
