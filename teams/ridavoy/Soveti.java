package ridavoy;

import java.util.Random;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 * Supreme Soviet.
 * 
 * @author Miraziz
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
        mLocation = rc.getLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        enemyTowers = rc.senseEnemyTowerLocations();
        allyTowers = rc.senseTowerLocations();
        rand = new Random(rc.getID());

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
                    if (ri.type == RobotType.HQ || ri.type == RobotType.TOWER)
                    {
                        target = ri;
                        break;
                    }
                }
                if (rc.canAttackLocation(target.location))
                {
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


    protected boolean isAttackingUnit(RobotType type)
    {
        return type == RobotType.DRONE || type == RobotType.BASHER
            || type == RobotType.TANK || type == RobotType.SOLDIER
            || type == RobotType.MINER || type == RobotType.BEAVER
            || type == RobotType.COMMANDER;
    }
}
