package supremecommander;

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
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        mapOffsetX =
            Math.max(allyHQ.x, enemyHQ.x) - GameConstants.MAP_MAX_WIDTH;
        mapOffsetY =
            Math.max(allyHQ.y, enemyHQ.y) - GameConstants.MAP_MAX_HEIGHT;
        int channel = getLocChannel(rc.getLocation());

        int num = rc.readBroadcast(channel);
        target =
            new MapLocation(num / Constants.MAP_HEIGHT + mapOffsetX, num
                % Constants.MAP_HEIGHT + mapOffsetY);
        enemyId = rc.readBroadcast(channel + 1);
    }


    @Override
    public void run()
        throws GameActionException
    {
        if (steps > 0)
        {
            if (enemyId == 32001)
            {
                RobotInfo[] enemyRobots =
                    rc.senseNearbyRobots(
                        (5 - steps + 1) * (5 - steps + 1),
                        enemyTeam);
                if (enemyRobots.length > 0)
                {
                    enemyId = enemyRobots[0].ID;
                    target = enemyRobots[0].location;
                }
                else if (steps == 4)
                {
                    rc.disintegrate();
                }
            }
            else if (rc.canSenseRobot(enemyId))
            {
                target = rc.senseRobot(enemyId).location;
            }

            if (rc.getLocation().isAdjacentTo(target))
            {
                rc.explode();
            }
        }

        if (rc.isCoreReady())
        {
// TODO FIX BUG ON MESH, run and you'll see it
            Direction dir = rc.getLocation().directionTo(target);
            Direction left = dir.rotateLeft();
            Direction right = dir.rotateRight();
            int count = 0;
            while (!rc.canMove(dir) && count < 3)
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
            if (count < 3)
            {
                rc.move(dir);
            }
            else if (steps == 4)
            {
                RobotInfo[] nearbyRobotsAtLoc =
                    rc.senseNearbyRobots(rc.getLocation().add(dir), 2, null);
                int num = nearbyRobotsAtLoc.length;
                int allyNum = 0;
                int enemyNum = 0;
                for (int i = num - 1; i >= 0; --i)
                {
                    if (nearbyRobotsAtLoc[i].type != RobotType.MISSILE)
                    {
                        if (nearbyRobotsAtLoc[i].team == myTeam)
                        {
                            allyNum++;
                        }
                        else
                        {
                            enemyNum++;
                        }
                    }
                }

                if (allyNum > enemyNum)
                {
                    rc.disintegrate();
                }
            }
        }
        else if (steps == 4)
        {
            RobotInfo[] nearbyRobotsAtLoc =
                rc.senseNearbyRobots(rc.getLocation(), 2, null);
            int num = nearbyRobotsAtLoc.length;
            int allyNum = 0;
            int enemyNum = 0;
            for (int i = num - 1; i >= 0; --i)
            {
                if (nearbyRobotsAtLoc[i].type != RobotType.MISSILE)
                {
                    if (nearbyRobotsAtLoc[i].team == myTeam)
                    {
                        allyNum++;
                    }
                    else
                    {
                        enemyNum++;
                    }
                }
            }

            if (allyNum > enemyNum)
            {
                rc.disintegrate();
            }
        }
        steps++;
    }


    @Override
    public void transferSupplies()
        throws GameActionException
    {
        // intentionally blank
        // missile no need supply
    }

}
