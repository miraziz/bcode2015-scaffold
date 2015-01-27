package general;

import battlecode.common.*;

public class Missile
    extends Soveti
{

    private MapLocation dest;
    private int         enemyId;
    private int         turn;
    private byte[]      frontalDirections = { 7, 1, 0, 0, 2, 1, 1, 3, 2, 2, 4,
        3, 3, 5, 4, 4, 6, 5, 5, 7, 6, 6, 0, 7 };


    public Missile(RobotController myRC)
        throws GameActionException
    {
        rc = myRC;
        enemyTeam = rc.getTeam().opponent();
        MapLocation mLocation = rc.getLocation();

        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        int mapOffsetX =
            Math.max(allyHQ.x, enemyHQ.x) - GameConstants.MAP_MAX_WIDTH;
        int mapOffsetY =
            Math.max(allyHQ.y, enemyHQ.y) - GameConstants.MAP_MAX_HEIGHT;

        int channel =
            (mLocation.x - mapOffsetX) * Constants.MAP_HEIGHT
                + (mLocation.y - mapOffsetY);
// int val = rc.readBroadcast(channel);
// dest =
// new MapLocation(val / Constants.MAP_HEIGHT + mapOffsetX, val
// % Constants.MAP_HEIGHT + mapOffsetY);
        dest = getLocation(getLocChannel(rc.getLocation()));
        enemyId = rc.readBroadcast(getIdChannel(rc.getLocation()));
    }


    @Override
    public void run()
        throws GameActionException
    {
        int curRound = Clock.getRoundNum();
        if (rc.canSenseRobot(enemyId))
        {

            // dest = rc.senseRobot(enemyId).location;
        }
        rc.setIndicatorLine(rc.getLocation(), dest, 0, 200, 255);
        MapLocation mLocation = rc.getLocation();
        MapLocation m = getLocation(getLocChannel(rc.getLocation()));
        if (m != null)
        {
            dest = m;
        }

        if (turn < 3)
        {
            int ord = mLocation.directionTo(dest).ordinal() * 3;
            if (ord == 27)
            {
                rc.explode();
            }
            else if (rc.isCoreReady())
            {
                byte[] dirs = frontalDirections;
                Direction[] directions = this.directions;
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
            }
            turn++;
        }
        else
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
                    byte[] dirs = frontalDirections;
                    Direction[] directions = this.directions;
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
                }
            }
        }

        if (Clock.getRoundNum() != curRound)
        {
// System.out.println("MISSILE BEHIND: "
// + (Clock.getRoundNum() - curRound) + " WITH BYTECODES: "
// + Clock.getBytecodesLeft());
        }
        else
        {
// System.out.println("Took: " + Clock.getBytecodeNum());
        }
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
