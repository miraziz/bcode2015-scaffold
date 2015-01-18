package serzhant;

import battlecode.common.*;

/**
 * Miner class.
 * 
 * @author Amit Bachchan
 */
public class Miner
    extends Proletariat
{

    private Direction myDirection;
    private boolean   turnRight;


    public Miner(RobotController rc)
        throws GameActionException
    {
        super(rc);
        turnRight = this.rand.nextBoolean();
        myDirection = this.getRandomDirection();
    }


    /**
     * Runs away if enemies are nearby, then tries to mine.
     */
    @Override
    public void run()
        throws GameActionException
    {
        super.run();

        rc.broadcast(
            Channels.minerCount,
            rc.readBroadcast(Channels.minerCount) + 1);
        findDefenseSpot();
        if (rc.isCoreReady())
        {
            // TODO Don't go back in the direction of the enemy. Have some
// memory that prevents you from going in that direction for some K turns before
// trying again
            runAway();

            // TODO Should it try to mine if it's running?
            if (rc.isCoreReady())
            {
                mine();
            }
        }
    }


    /**
     * Runs in the opposite direction of the average location of enemy units
     * within attacking distance.
     * 
     * @return True if this serp moved away, false otherwise.
     * @throws GameActionException
     */
    private boolean runAway()
        throws GameActionException
    {
        RobotInfo[] enemies =
            rc.senseNearbyRobots(mType.sensorRadiusSquared, enemyTeam);
        if (enemies.length == 0)
        {
            return false;
        }

        int avgX = 0;
        int avgY = 0;
        for (RobotInfo enemy : enemies)
        {
            if (enemy.location.distanceSquaredTo(mLocation) <= enemy.type.attackRadiusSquared)
            {
                avgX += enemy.location.x;
                avgY += enemy.location.y;
            }
        }
        avgX /= enemies.length;
        avgY /= enemies.length;

        Direction dir =
            mLocation.directionTo(new MapLocation(avgX, avgY)).opposite();

        dir = getFreeStrafeDirection(dir);
        if (dir != null)
        {
            rc.move(dir);
            return true;
        }
        return false;
    }


    /**
     * Attempts to mine the current location. If location is mined out, attempts
     * to move to richest adjacent cell. If all adjacent cells are mined out,
     * moves in a random direction. Otherwise, does nothing.
     * 
     * @return True if this serp moved or mined, false otherwise.
     * @throws GameActionException
     */
    protected boolean mine()
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return false;
        }

        // TODO Use constant to determine if worth mining?
        if (rc.senseOre(mLocation) >= 1)
        {
            rc.mine();
            return true;
        }
        else
        {
            // TODO use array
            Direction left = myDirection.opposite().rotateRight();
            Direction right = myDirection.opposite();

            Direction best = null;
            double topScore = 0;

            Direction cur;
            for (int i = 0; i < 8; i++)
            {
                if (i % 2 == 0)
                {
                    left = left.rotateLeft();
                    cur = left;
                }
                else
                {
                    right = right.rotateRight();
                    cur = right;
                }

                if (rc.canSenseLocation(mLocation.add(cur)))
                {
                    double ore = rc.senseOre(mLocation.add(cur));
                    if (ore > 1 && ore > topScore && rc.canMove(cur))
                    {
                        topScore = ore;
                        best = cur;
                    }
                }
            }
            if (best != null)
            {
                rc.move(best);
                return true;
            }
            else
            {
                // TODO use array
                // TODO Avoid diagonals unless necessary
                int count = 0;
                while (!rc.canMove(myDirection) && count < 8)
                {
                    if (turnRight)
                    {
                        myDirection = myDirection.rotateRight();
                    }
                    else
                    {
                        myDirection = myDirection.rotateLeft();
                    }
                    count++;
                }
                if (count < 8)
                {
                    rc.move(myDirection);
                    return true;
                }
            }
        }
        return false;
    }


    public void findDefenseSpot()
        throws GameActionException
    {
        RobotInfo[] nearby =
            rc.senseNearbyRobots(
                rc.getType().sensorRadiusSquared,
                this.enemyTeam);
        if (nearby.length == 0)
        {
            return;
        }
        int enemyHealth = 0;
        int avgX = 0;
        int avgY = 0;
        for (RobotInfo r : nearby)
        {
            if (r.type == RobotType.HQ || r.type == RobotType.TOWER)
            {
                continue;
            }
            enemyHealth += r.health;
            avgX += r.location.x;
            avgY += r.location.y;
        }
        if (rc.readBroadcast(Channels.highestEnemyHealth) < enemyHealth)
        {
            rc.broadcast(Channels.highestEnemyHealth, enemyHealth);
            avgX /= nearby.length;
            avgY /= nearby.length;
            broadcastLocation(Channels.rallyLoc, new MapLocation(avgX, avgY));
        }
    }
}
