package marshal;

import battlecode.common.*;

/**
 * Fighter (mobile) class.
 * 
 * @author Amit Bachchan
 */
public class Fighter
    extends Proletariat
{

    protected boolean attacking;
    protected boolean committed;


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
        committed = false;
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

        // TODO BE SURE TO CALL setUpDefender() in sbuclasses of FIGHTER such as
        // drone/tank/soldier
        //

// }
    }


    protected void setUpDefender()
        throws GameActionException
    {
        this.setDestination(getLocation(Channels.rallyLoc));

        attacking = rc.readBroadcast(Channels.attacking) == 1;
        if (attacking)
        {
            enemyTowers = rc.senseEnemyTowerLocations();
            if (enemyTowers.length > 0)
            {
                MapLocation closest = enemyTowers[0];
                int minDistance = Integer.MAX_VALUE;
                for (MapLocation t : enemyTowers)
                {
                    int dist = rc.getLocation().distanceSquaredTo(t);
                    if (dist < minDistance)
                    {
                        minDistance = dist;
                        closest = t;
                    }
                }
                this.setDestination(closest);
            }
            else
            {
                this.setDestination(enemyHQ);
            }
        }
        else
        {
            this.setDestination(getLocation(Channels.rallyLoc));
        }
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        if (!attack(nearby))
        {
            micro(nearby);
        }
    }


    protected void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return;
        }
        boolean inDangerRange = false;
        boolean inEnemyAttackRange = false;
        boolean nearTowerRange = false;
        int myTeamHealth = (int)rc.getHealth();
        int enemyTeamHealth = 0;
        int avgX = 0;
        int avgY = 0;
        int allyCount = 0;
        int enemyCount = 0;
        if ((attacking && dest.distanceSquaredTo(rc.getLocation()) < 35))
        {
            nearTowerRange = true;
            enemyTeamHealth += RobotType.TOWER.maxHealth / 4;
        }
        for (RobotInfo r : nearby)
        {
            RobotType type = r.type;
            if (isAttackingUnit(type))
            {
                if (r.team == enemyTeam)
                {
                    if (rc.getLocation().distanceSquaredTo(r.location) <= type.attackRadiusSquared + 2)
                    {
                        inDangerRange = true;
                        if (rc.getLocation().distanceSquaredTo(r.location) <= type.attackRadiusSquared)
                        {
                            inEnemyAttackRange = true;
                        }
                    }
                    enemyCount++;

                    enemyTeamHealth += r.health;

                    avgX += r.location.x;
                    avgY += r.location.y;
                }
                else if (isAttackingUnit(type))
                {
                    allyCount++;
                    myTeamHealth += rc.getHealth();
                }
            }
        }
        if (nearTowerRange)
        {
            if (rc.getLocation().distanceSquaredTo(dest) <= rc.getType().attackRadiusSquared)
            {
                return;
            }
            if ((allyCount - enemyCount >= 5)
                || Clock.getRoundNum() > Constants.towerAttackRound
                || committed)
            {
                committed = true;
                Direction dir =
                    this.getFreeStrafeDirection(rc.getLocation().directionTo(
                        dest));
                if (dir != null)
                {
                    rc.move(dir);
                }
            }
            return;
        }
        if (enemyCount == 0)
        {
            bugWithCounter();
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

        committed = false;
        if (myTeamHealth >= enemyTeamHealth * 2)
        {
            bugWithCounter();
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
            bugWithCounter();
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


    protected boolean attack(RobotInfo[] nearby)
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
            if (r.team == this.enemyTeam && rc.canAttackLocation(r.location))
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
        else if (type.canSpawn())
        {
            priority = 1;
        }
        return priority;
    }


    protected MapLocation getClosestTowerOrHQ()
    {
        MapLocation closest = enemyHQ;
        int closestDist = rc.getLocation().distanceSquaredTo(enemyHQ);
        enemyTowers = rc.senseEnemyTowerLocations();
        for (int i = 0; i < enemyTowers.length; i++)
        {
            int dist = rc.getLocation().distanceSquaredTo(enemyTowers[i]);
            if (dist < closestDist)
            {
                closest = enemyTowers[i];
                closestDist = dist;
            }
        }
        return closest;
    }
}
