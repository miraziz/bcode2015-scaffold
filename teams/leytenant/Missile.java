package leytenant;

import battlecode.common.*;

public class Missile
    extends Soveti
{

    private MapLocation dest;
    private boolean     firstRun;

// private int diag = 0;
// private int count;
// private Direction left;
// private Direction right;
//
// private Direction[][] frontalDirections = {
// { Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.NORTH },
// { Direction.NORTH, Direction.EAST, Direction.NORTH_EAST },
// { Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.EAST },
// { Direction.EAST, Direction.SOUTH, Direction.SOUTH_EAST },
// { Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.SOUTH },
// { Direction.SOUTH, Direction.WEST, Direction.SOUTH_WEST },
// { Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.WEST },
// { Direction.WEST, Direction.NORTH, Direction.NORTH_WEST } };

    private byte[]      frontalDirections = { 7, 1, 0, 0, 2, 1, 1, 3, 2, 2, 4,
        3, 3, 5, 4, 4, 6, 5, 5, 7, 6, 6, 0, 7 };


// private Direction[] directions = {{Direction

    public Missile(RobotController myRC)
        throws GameActionException
    {
        System.out.println("PRE CONSTRUCTOR TAKES: " + Clock.getBytecodeNum());
        rc = myRC;
        enemyTeam = rc.getTeam().opponent();
        MapLocation mLocation = rc.getLocation();

// System.out.println("Constructor before map checks: "
// + Clock.getBytecodeNum());

        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
// System.out.println("Constructor before first offset: "
// + Clock.getBytecodeNum());
        int mapOffsetX =
            Math.max(allyHQ.x, enemyHQ.x) - GameConstants.MAP_MAX_WIDTH;
// System.out.println("Constructor before second offset: "
// + Clock.getBytecodeNum());
        int mapOffsetY =
            Math.max(allyHQ.y, enemyHQ.y) - GameConstants.MAP_MAX_HEIGHT;
        System.out.println("Constructor before loc: " + Clock.getBytecodeNum());

        int channel =
            (mLocation.x - mapOffsetX) * Constants.MAP_HEIGHT
                + (mLocation.y - mapOffsetY);
        int val = rc.readBroadcast(channel);
        dest =
            new MapLocation(val / Constants.MAP_HEIGHT + mapOffsetX, val
                % Constants.MAP_HEIGHT + mapOffsetY);

        System.out.println("Constructor took: " + Clock.getBytecodeNum());
// System.out.println("START: " + Clock.getBytecodeNum());
// Direction.EAST.ordinal();
// System.out.println("END: " + Clock.getBytecodeNum());

        firstRun = true;
    }


    @Override
    public void run()
        throws GameActionException
    {
        int curRound = Clock.getRoundNum();

        MapLocation mLocation = rc.getLocation();

        if (!firstRun)
        {
            RobotInfo[] nearbyEnemies =
                rc.senseNearbyRobots(
                    GameConstants.MISSILE_RADIUS_SQUARED,
                    enemyTeam);

            if (nearbyEnemies.length > 0)
            {
                rc.explode();
            }
            else if (rc.isCoreReady())
            {
                int ord = mLocation.directionTo(dest).ordinal() * 3;
                if (ord == 27)
                {
                    rc.explode();
                }
                else
                {
                    int startByteCode = Clock.getBytecodeNum();
                    System.out.println("BEFORE MOVING: " + startByteCode);

                    byte[] dirs = frontalDirections;
                    if (rc.canMove(directions[dirs[ord]]))
                    {
                        rc.move(directions[dirs[ord]]);
                    }
                    else if (rc.canMove(directions[dirs[++ord]]))
                    {
                        rc.move(directions[dirs[ord]]);
                    }
                    else if (rc.canMove(directions[dirs[++ord]]))
                    {
                        rc.move(directions[dirs[ord]]);
                    }
                    System.out.println("MOVING TOOK: "
                        + (Clock.getBytecodeNum() - startByteCode));
                }
            }
        }
        else
        {
            int ord = mLocation.directionTo(dest).ordinal() * 3;
            if (ord == 27)
            {
                rc.explode();
            }
            else
            {
                int startByteCode = Clock.getBytecodeNum();
                System.out.println("BEFORE MOVING: " + startByteCode);

                byte[] dirs = frontalDirections;
                if (rc.canMove(directions[dirs[ord]]))
                {
                    rc.move(directions[dirs[ord]]);
                }
                else if (rc.canMove(directions[dirs[++ord]]))
                {
                    rc.move(directions[dirs[ord]]);
                }
                else if (rc.canMove(directions[dirs[++ord]]))
                {
                    rc.move(directions[dirs[ord]]);
                }
                System.out.println("MOVING TOOK: "
                    + (Clock.getBytecodeNum() - startByteCode));
            }
        }

        if (Clock.getRoundNum() != curRound)
        {
// if (!firstRun)
// {
            System.out.println("MISSILE BEHIND: "
                + (Clock.getRoundNum() - curRound) + " WITH BYTECODES: "
                + Clock.getBytecodesLeft());
// }
        }
        else
        {
// if (Clock.getBytecodeNum() > 224)
// {
            System.out.println("Took: " + Clock.getBytecodeNum());
// }
        }
// if (firstRun)
// {
        firstRun = false;
// }
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
