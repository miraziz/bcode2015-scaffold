package team025;

import battlecode.common.*;

public class SimpleMissile
    extends Soveti
{

    private MapLocation target;
    private int         steps;
    private int         enemyId;


    public SimpleMissile(RobotController rc)
        throws GameActionException
    {
        this.rc = rc;
        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        mapOffsetX =
            Math.max(allyHQ.x, enemyHQ.x) - GameConstants.MAP_MAX_WIDTH;
        mapOffsetY =
            Math.max(allyHQ.y, enemyHQ.y) - GameConstants.MAP_MAX_HEIGHT;
        int channel = getLocChannel(rc.getLocation());
        rc.setIndicatorString(2, "My channel: " + channel);

        target = getLocation(channel);
        enemyId = rc.readBroadcast(channel + 1);
    }


    @Override
    public void run()
        throws GameActionException
    {
        int curRound = Clock.getRoundNum();

        if (steps > 0)
        {
            if (rc.getLocation().isAdjacentTo(target))
            {
                rc.explode();
            }

            if (rc.canSenseRobot(enemyId))
            {
                target = rc.senseRobot(enemyId).location;
            }
        }

        if (rc.isCoreReady())
        {
// TODO FIX BUG ON MESH, run and you'll see it
            Direction dir = rc.getLocation().directionTo(target);
            Direction left = dir.rotateLeft();
            Direction right = dir.rotateRight();
            int count = 0;
            while (!rc.canMove(dir) && count < 5)
            {
                if (count % 2 == 0)
                {
                    dir = left;
                    left = left.rotateLeft();
                }
                else
                {
                    dir = right;
                    right = right.rotateRight();
                }
                count++;
            }
            if (count < 5)
            {
                rc.move(dir);
            }
        }
        steps++;

        int endRound = Clock.getRoundNum();
        if (curRound != endRound)
        {
            System.out.println("Missile over bytecodes: "
                + (endRound - curRound));
        }
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {
        // intentionally blank
        // missile no need supply
    }

}
