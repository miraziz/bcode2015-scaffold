package ridavoy;

import battlecode.common.*;

public abstract class Soveti
{
    protected RobotController rc;
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


    public abstract void run()
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
        RobotInfo[] nearbyEnemies =
            rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam()
                .opponent());
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
        int x = allyHQ.x - loc.x + 120;
        int y = allyHQ.y - loc.y + 120;
        int broadcast = x + (1000 * y);
        rc.broadcast(channel, broadcast);
        rc.setIndicatorString(0, "This is what I Broadcasted: " + broadcast);
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
        int x = num % 1000;
        num /= 1000;
        int y = num % 1000;
        x -= 120 + allyHQ.x;
        y -= 120 + allyHQ.y;
        return new MapLocation(x * -1, y * -1);

    }
}
