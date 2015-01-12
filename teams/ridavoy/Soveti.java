package ridavoy;

import battlecode.common.*;

public abstract class Soveti
{    
    protected RobotController rc;
    protected MapLocation     mLocation;
    protected int               type;
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
}
