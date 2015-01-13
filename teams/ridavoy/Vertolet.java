package ridavoy;

import battlecode.common.*;

public class Vertolet
    extends Proletariat
{

    private enum Decision
    {
        RELAX,
        ATTACK,
        RUN
    }


    public Vertolet(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        if (rc.isCoreReady())
        {
            RobotInfo[] nearby =
                rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
            Decision decision = null;
            int enemyX = 0;
            int enemyY = 0;
            int enemyCount = 0;
            int myTeamsHealth = 0;
            int enemyTeamsHealth = 0;
            for (RobotInfo r : nearby)
            {
                if (r.team == enemyTeam)
                {
                    RobotType type = r.type;
                    if (isAttackingUnit(r.type))
                    {
                        enemyTeamsHealth += r.health;
                        enemyX += r.location.x;
                        enemyY += r.location.y;
                        enemyCount++;
                    }
                }
                else
                {
                    if (isAttackingUnit(r.type))
                    {
                        myTeamsHealth += r.health;
                    }
                }
            }
            Direction towardsEnemy = rc.getLocation().directionTo(enemyHQ);
            if (enemyCount == 0)
            {
                decision = Decision.ATTACK;
            }
            else
            {
                MapLocation enemyLoc =
                    new MapLocation(enemyX / enemyCount, enemyY / enemyCount);
                towardsEnemy = rc.getLocation().directionTo(enemyLoc);
                if (myTeamsHealth < enemyTeamsHealth + 400)
                {
                    decision = Decision.RUN;
                }
                else if (myTeamsHealth < enemyTeamsHealth)
                {
                    decision = Decision.ATTACK;
                }
            }
            if (attack())
            {
                return;
            }
            Direction right = towardsEnemy;
            Direction left = towardsEnemy;
            int count = 0;
            while (!canMove(towardsEnemy) && count < 8)
            {
                if (towardsEnemy == right)
                {
                    left = left.rotateLeft();
                    towardsEnemy = left;
                }
                else
                {
                    right = right.rotateRight();
                    towardsEnemy = right;
                }
                count++;
            }
            if (towardsEnemy == null)
            {
                return;
            }
            if (decision != Decision.RELAX)
            {
                this.move(towardsEnemy);
            }
        }
    }


    /**
     * checks if we can move to a location, can't move if there is a tower in
     * the range
     * 
     * @param dir
     * @return if we can move in the direction given
     * @throws GameActionException
     */
    public boolean canMove(Direction dir)
        throws GameActionException
    {
        if (!rc.canMove(dir))
        {
            return false;
        }
        MapLocation next = rc.getLocation().add(dir);
        if (next.distanceSquaredTo(enemyHQ) < RobotType.HQ.attackRadiusSquared)
        {
            return false;
        }
        for (int i = 0; i < enemyTowers.length; i++)
        {
            if (next.distanceSquaredTo(enemyTowers[i]) < RobotType.TOWER.attackRadiusSquared)
            {
                return false;
            }
        }
        return true;
    }


    private boolean isAttackingUnit(RobotType type)
    {
        return type == RobotType.DRONE || type == RobotType.BASHER
            || type == RobotType.TANK || type == RobotType.SOLDIER
            || type == RobotType.MINER || type == RobotType.BEAVER
            || type == RobotType.COMMANDER;
    }
}
