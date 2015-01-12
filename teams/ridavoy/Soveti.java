package ridavoy;

import battlecode.common.*;

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


    public Soveti(RobotController myRC)
    {
        rc = myRC;
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
                rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc
                    .getTeam().opponent());
            RobotInfo target = null;

            if (nearbyEnemies.length > 0 && rc.isWeaponReady())
            {
                for (RobotInfo ri : nearbyEnemies)
                {
                    if (target == null || ri.health < target.health)
                    {
                        target = ri;
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
        rc.setIndicatorString(1, "Called with " + loc.x + ", " + loc.y);
        int x = (loc.x - mapOffsetX);
        int y = (loc.y - mapOffsetY);

        int broadcast = broadcastO * x + y;
        rc.broadcast(channel, broadcast);

        MapLocation result = getLocation(channel);
        rc.setIndicatorString(2, "Result with: " + result.x + ", " + result.y);
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
            int x = num / 300 + mapOffsetX;
            int y = num % 300 + mapOffsetY;
            res = new MapLocation(x, y);
        }

        return res;
    }
}
