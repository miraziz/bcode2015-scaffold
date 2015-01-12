package ridavoy;

import battlecode.common.*;

public abstract class Soveti
{
    protected RobotController rc;
    protected MapLocation     mLocation;
    protected int             type;
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


    public void broadcastType()
    {

    }


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
     * broadcasts a location to a single channel by putting the x's and y's
     * offset from the headquarter in the broadcast
     * 
     * @throws GameActionException
     */
    protected void broadcastLocation(int channel, MapLocation loc)
        throws GameActionException
    {
        rc.setIndicatorString(1, "Called with " + loc.x + ", " + loc.y);
        int x = (allyHQ.x - loc.x);// + 200;
        int y = (allyHQ.y - loc.y);// + 200;
        x += 200;
        y += 200;
        int broadcast = x + (1000 * y);
        rc.broadcast(channel, broadcast);
        MapLocation result = getLocation(channel);
        rc.setIndicatorString(2, "Result with: " + result.x + ", " + result.y);
    }


    /**
     * @param channel
     * @return location of the broadcasted channel
     * @throws GameActionException
     */
    protected MapLocation getLocation(int channel)
        throws GameActionException
    {
        int num = rc.readBroadcast(channel);
        int x = num % 1000 - 200;
        num /= 1000;
        int y = num % 1000 - 200;
        x = allyHQ.x - x;
        y = allyHQ.y - y;
        return new MapLocation(x, y);

    }
}
