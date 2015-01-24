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
        enemyTeam = rc.getTeam().opponent();
// System.out.println("MADE with left: " + Clock.getBytecodesLeft());
    }


    @Override
    public void run()
        throws GameActionException
    {
        int curRound = Clock.getRoundNum();
// int startTime = Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
// System.out.println("Starting");
        mLocation = rc.getLocation();
        String doing = "Nothing";
        String firstDir = "";

        if (rc.isCoreReady())
        {
            int enemiesProcessed = 0;
            RobotInfo[] nearbyEnemies =
                rc.senseNearbyRobots(
                    Constants.MISSILE_MAX_RANGE_SQUARED,
                    enemyTeam);
            Direction bestDir = null;
            for (int i = 0; i < nearbyEnemies.length; i++)
            {
                enemiesProcessed++;
                Direction newDir =
                    mLocation.directionTo(nearbyEnemies[i].location);
                if (firstDir == null)
                {
                    firstDir = newDir.toString();
                }

                if (mLocation.isAdjacentTo(nearbyEnemies[i].location))
                {
                    bestDir = Direction.NONE;
                    break;
                }
                else if (rc.canMove(newDir))
                {
                    bestDir = newDir;
                    break;
                }
            }

// int closestEnemiesTime =
// Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
// System.out.println("Closest enemy took: "
// + (closestEnemiesTime - startTime));
// System.out.println("Enemies processed: " + enemiesProcessed);

            if (bestDir == null)
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

                // int movingTime =
                // Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
                // System.out.println("Moving took: "
                // + (movingTime - closestEnemiesTime));
            }
            else if (bestDir != Direction.NONE)
            {
                doing = "at enemy " + bestDir;
                rc.move(bestDir);
            }
        }
// int finishTime = Clock.getRoundNum() * 500 + Clock.getBytecodeNum();
// System.out.println("Total took: " + (finishTime - startTime));

        if (Clock.getRoundNum() != curRound)
        {
            System.out.println("MISSILE BEHIND: "
                + (Clock.getRoundNum() - curRound) + " WITH BYTECODES: "
                + Clock.getBytecodesLeft());
        }
        rc.setIndicatorString(1, firstDir);
        rc.setIndicatorString(1, doing);
    }


    /**
     * Ain't nobody got time for dat.
     */
    @Override
    public void transferSupplies()
        throws GameActionException
    {

    }
}
