package yefreytor;

import battlecode.common.*;

/**
 * Drone class.
 * 
 * @author Amit Bachchan
 */
public class Drone
    extends Proletariat
{

    private boolean turnDirection;


    public Drone(RobotController rc)
        throws GameActionException
    {
        super(rc);

        turnDirection = rand.nextBoolean();
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.droneCount,
            rc.readBroadcast(Channels.droneCount) + 1);

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
            boolean inDanger = false;
            boolean shouldRun = false;

            Direction towardsEnemy = rc.getLocation().directionTo(enemyHQ);
            for (RobotInfo r : nearby)
            {
                if (r.team == enemyTeam)
                {
                    if (isAttackingUnit(r.type))
                    {
                        enemyTeamsHealth += r.health;
                        enemyX += r.location.x;
                        enemyY += r.location.y;
                        ++enemyCount;
                        if (rc.getLocation().distanceSquaredTo(r.location) <= r.type.attackRadiusSquared)
                        {
                            if (r.type == RobotType.BASHER
                                || r.type == RobotType.SOLDIER
                                || r.type == RobotType.MINER
                                || r.type == RobotType.BEAVER)
                            {
                                shouldRun = true;
                            }
                            inDanger = true;
                        }
                    }
                }
                else if (r.team == rc.getTeam() && isAttackingUnit(r.type))
                {
                    Direction dir = rc.getLocation().directionTo(r.location);
                    if (dir == towardsEnemy || dir == towardsEnemy.rotateLeft()
                        || dir == towardsEnemy.rotateLeft().rotateLeft()
                        || dir == towardsEnemy.rotateRight()
                        || dir == towardsEnemy.rotateRight().rotateRight())
                    {
                        myTeamsHealth += r.health;
                    }
                }
            }
            decision = Decision.ATTACK;

            if (enemyCount > 0)
            {
                MapLocation enemyLoc =
                    new MapLocation(enemyX / enemyCount, enemyY / enemyCount);
                towardsEnemy = rc.getLocation().directionTo(enemyLoc);
                if (shouldRun || (myTeamsHealth < enemyTeamsHealth && inDanger))
                {
                    decision = Decision.RUN;
                }
                else if (myTeamsHealth < enemyTeamsHealth * 2)
                {
                    decision = Decision.RELAX;
                }
            }
            if (Clock.getRoundNum() > 1980)
            {
                decision = Decision.ATTACK;
            }
            rc.setIndicatorString(0, "Enemy count: " + enemyCount);
            rc.setIndicatorString(1, "" + decision);
            if (decision != Decision.RUN && attack())
            {
                return;
            }
            if (decision == Decision.RUN)
            {
                towardsEnemy = towardsEnemy.opposite();
            }

            Direction right = towardsEnemy;
            Direction left = towardsEnemy;
            int count = 0;
            while (!canMove(towardsEnemy) && count < 8)
            {
                if (turnDirection)
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
                }
                else
                {
                    if (towardsEnemy == left)
                    {
                        right = right.rotateRight();
                        towardsEnemy = right;
                    }
                    else
                    {
                        left = left.rotateLeft();
                        towardsEnemy = left;
                    }
                }
                count++;
            }
            if (towardsEnemy == null)
            {
                return;
            }
            if (decision != Decision.RELAX && canMove(towardsEnemy))
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
        if (Clock.getRoundNum() < 1980)
        {
            if (next.distanceSquaredTo(enemyHQ) < RobotType.HQ.attackRadiusSquared)
            {
                return false;
            }
            for (int i = 0; i < enemyTowers.length; ++i)
            {
                if (next.distanceSquaredTo(enemyTowers[i]) < RobotType.TOWER.attackRadiusSquared)
                {
                    return false;
                }
            }
        }
        return true;
    }
}
