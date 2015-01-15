package yefreytor;

import battlecode.common.*;

/**
 * Miner class.
 * 
 * @author Miraziz
 */
public class Serp
    extends Proletariat
{

    private Direction myDirection;
    private boolean   turnRight;


    public Serp(RobotController rc)
        throws GameActionException
    {
        super(rc);
        turnRight = this.rand.nextBoolean();
        myDirection = this.getRandomDirection();
    }


    /**
     * Mines.
     */
    @Override
    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.minerCount,
            rc.readBroadcast(Channels.minerCount) + 1);
        if (rc.isCoreReady())
        {
            runAway();
        }
        if (rc.isCoreReady())
        {
            mine();
        }

    }


    private void runAway()
        throws GameActionException
    {
        RobotInfo[] enemies =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
        if (enemies.length == 0)
        {
            return;
        }
        int avgX = 0;
        int avgY = 0;
        for (RobotInfo enemy : enemies)
        {
            if (enemy.location.distanceSquaredTo(rc.getLocation()) <= enemy.type.attackRadiusSquared)
            {
                avgX += enemy.location.x;
                avgY += enemy.location.y;
            }
        }
        avgX /= enemies.length;
        avgY /= enemies.length;
        Direction dir =
            rc.getLocation().directionTo(new MapLocation(avgX, avgY))
                .opposite();
        Direction left = dir;
        Direction right = dir;
        int count = 0;
        while (!rc.canMove(dir) && count < 8)
        {
            if (count % 2 == 0)
            {
                left = left.rotateLeft();
                dir = left;
            }
            else
            {
                right = right.rotateRight();
                dir = right;
            }
            count++;
        }
        if (count < 5)
        {
            rc.move(dir);
        }

    }


    protected void mine()
        throws GameActionException
    {

        if (rc.senseOre(rc.getLocation()) >= 1 && rc.canMine())
        {
            rc.mine();
        }
        else
        {
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
                if (rc.canSenseLocation(rc.getLocation().add(cur)))
                {
                    double ore = rc.senseOre(rc.getLocation().add(cur));
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
            }
            else
            {
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
                }
            }

        }
    }
}
