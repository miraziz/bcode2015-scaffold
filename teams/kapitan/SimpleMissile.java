package kapitan;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SimpleMissile
    extends Soveti
{

    private MapLocation target;


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
        rc.setIndicatorString(0, "target: " + target);

    }


    @Override
    public void run()
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            return;
        }
        if (rc.getLocation().isAdjacentTo(target))
        {
            rc.explode();
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
