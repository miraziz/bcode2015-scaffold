package kapitan;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class SimpleMissile
    extends Soveti
{

    private MapLocation target;
    private int         steps;


    public SimpleMissile(RobotController rc)
        throws GameActionException
    {
        this.rc = rc;
        steps = 0;
        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        mapOffsetX =
            Math.max(allyHQ.x, enemyHQ.x) - GameConstants.MAP_MAX_WIDTH;
        mapOffsetY =
            Math.max(allyHQ.y, enemyHQ.y) - GameConstants.MAP_MAX_HEIGHT;
        int channel = getLocChannel(rc.getLocation());
        rc.setIndicatorString(2, "My channel: " + channel);

        target = getLocation(channel);

    }


    @Override
    public void run()
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return;
        }
        if (steps > 0)
        {
            RobotInfo[] enemies =
                rc.senseNearbyRobots(5, rc.getTeam().opponent());
            if (enemies.length != 0)
            {
                target = enemies[0].location;
                int minDistance =
                    rc.getLocation().distanceSquaredTo(enemies[0].location);
                for (int i = 1; i < enemies.length; ++i)
                {
                    int dist =
                        rc.getLocation().distanceSquaredTo(enemies[i].location);
                    if (rc.getLocation().distanceSquaredTo(enemies[i].location) < minDistance)
                    {
                        minDistance = dist;
                        target = enemies[i].location;
                    }
                }
            }
        }
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
            steps++;
        }
        if (rc.getLocation().isAdjacentTo(target))
        {
            rc.explode();
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
