package serzhant;

import battlecode.common.*;

/**
 * Fighter (mobile) class.
 * 
 * @author Amit Bachchan
 */
public class Fighter
    extends Proletariat
{

    /**
     * Sets rally point.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Fighter(RobotController rc)
        throws GameActionException
    {
        super(rc);

        setDestination(getLocation(Channels.rallyLoc));
    }


    /**
     * Attacks if possible, or bugs otherwise.
     */
    @Override
    public void run()
        throws GameActionException
    {
        super.run();

        // TODO Stop them from moving when they're in a clump to avoid wasting
// supply
        this.setDestination(getLocation(Channels.rallyLoc));
        rc.setIndicatorString(0, "Traveling to: "
            + getLocation(Channels.rallyLoc));
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
        if (!attack(nearby))
        {
            micro(nearby);
        }
    }


    private void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return;
        }
        boolean inDangerRange = false;
        boolean inEnemyAttackRange = false;
        int myTeamHealth = 0;
        int enemyTeamHealth = 0;
        int avgX = 0;
        int avgY = 0;
        int enemyCount = 0;
        for (RobotInfo r : nearby)
        {
            if (isAttackingUnit(r.type))
            {
                if (r.team == enemyTeam)
                {
                    if (rc.getLocation().distanceSquaredTo(r.location) <= r.type.attackRadiusSquared + 2)
                    {
                        inDangerRange = true;
                        if (rc.getLocation().distanceSquaredTo(r.location) <= r.type.attackRadiusSquared)
                        {
                            inEnemyAttackRange = true;
                        }
                    }
                    enemyCount++;
                    enemyTeamHealth += r.health;
                    avgX += r.location.x;
                    avgY += r.location.y;
                }
                else
                {
                    myTeamHealth += rc.getHealth();
                }
            }
        }
        if (enemyCount == 0)
        {
            bug();
            return;
        }
        avgX /= enemyCount;
        avgY /= enemyCount;
        MapLocation enemy = new MapLocation(avgX, avgY);
        if (enemyTeamHealth > rc.readBroadcast(Channels.highestEnemyHealth))
        {
            rc.broadcast(Channels.highestEnemyHealth, enemyTeamHealth);
            broadcastLocation(Channels.highestEnemyHealthLoc, enemy);
        }
        if (myTeamHealth >= enemyTeamHealth * 2)
        {
            bug();
        }
        else if (myTeamHealth < enemyTeamHealth * 2)
        {
            if (inEnemyAttackRange)
            {
                runAway(enemy.directionTo(rc.getLocation()));
            }
        }
        else if (!inDangerRange)
        {
            bug();
        }
    }


    private void runAway(Direction towards)
        throws GameActionException
    {
        Direction dir = this.getFreeForwardDirection(towards);
        if (dir != null && rc.canMove(dir))
        {
            rc.move(dir);
        }
    }


    private boolean attack(RobotInfo[] nearby)
        throws GameActionException
    {
        if (!rc.isWeaponReady())
        {
            return false;
        }
        int highestPriority = 0;
        RobotInfo toAttack = null;
        for (RobotInfo r : nearby)
        {
            if (rc.canAttackLocation(r.location))
            {
                int priority = getPriority(r.type);
                if (priority > highestPriority)
                {
                    toAttack = r;
                    highestPriority = priority;
                }
                else if (priority != 0 && priority == highestPriority
                    && r.health < toAttack.health)
                {
                    toAttack = r;
                }
            }
        }
        if (highestPriority == 0)
        {
            return false;
        }
        rc.attackLocation(toAttack.location);
        return true;
    }


    private int getPriority(RobotType type)
    {
        int priority = 0;
        if (type == RobotType.TOWER || type == RobotType.HQ)
        {
            priority = 4;
        }
        else if (isAttackingUnit(type))
        {
            priority = 3;
        }
        else if (type == RobotType.MINER || type == RobotType.BEAVER)
        {
            priority = 2;
        }
        else if (isProductionBuilding(type))
        {
            priority = 1;
        }
        return priority;
    }
}
