package leytenant;

import battlecode.common.*;

public class Missile
    extends Soveti
{
    public Missile(RobotController myRC)
        throws GameActionException
    {
        super();
        rc = myRC;
        enemyHQ = rc.senseEnemyHQLocation();
    }


    @Override
    public void run()
        throws GameActionException
    {
        int startTime = Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
        System.out.println("Starting");
        mLocation = rc.getLocation();
        String doing = "Nothing";

        RobotInfo[] nearbyEnemies =
            rc.senseNearbyRobots(Constants.MISSILE_MAX_RANGE_SQUARED, enemyTeam);
        Direction bestDir = null;
        int minDist = 50;
        for (int i = 0; i < nearbyEnemies.length; i++)
        {
            MapLocation newLoc = nearbyEnemies[i].location;
            int newDist = newLoc.distanceSquaredTo(mLocation);
            Direction newDir = mLocation.directionTo(mLocation);
            if (newDist < minDist && rc.canMove(newDir))
            {
                minDist = newDist;
                bestDir = mLocation.directionTo(newLoc);
            }
        }

        int closestEnemiesTime =
            Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
        System.out.println("Closest enemy took: "
            + (closestEnemiesTime - startTime));

        if (bestDir != null)
        {
            doing = "at enemy";
            rc.move(bestDir);
        }
        else
        {
            Direction left = mLocation.directionTo(enemyHQ);
            Direction right = left.rotateRight();
            Direction next = null;
            int count = 0;
            while (count < 3)
            {
                if (count % 2 == 0)
                {
                    next = left;
                    left = left.rotateLeft();
                }
                else
                {
                    next = right;
                    right = right.rotateRight();
                }
                if (rc.canMove(next))
                {
                    rc.move(next);
                    break;
                }
                count++;
            }

            int movingTime = Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
            System.out.println("Moving took: "
                + (movingTime - closestEnemiesTime));
        }

        int finishTime = Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
        System.out.println("Total took: " + (finishTime - startTime));

        rc.setIndicatorString(1, doing);
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {

    }
}
