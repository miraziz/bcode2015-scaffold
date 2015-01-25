package leytenant;

import battlecode.common.*;

public class Missile
    extends Soveti
{

    private int         diag = 0;
    private int         count;
    private MapLocation dest;
    private Direction   left;
    private Direction   right;


// private Direction[] directions = {{Direction

    public Missile(RobotController myRC)
        throws GameActionException
    {
        rc = myRC;
        enemyTeam = rc.getTeam().opponent();
        mLocation = rc.getLocation();

        allyHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        mapOffsetX =
            Math.max(allyHQ.x, enemyHQ.x) - GameConstants.MAP_MAX_WIDTH;
        mapOffsetY =
            Math.max(allyHQ.y, enemyHQ.y) - GameConstants.MAP_MAX_HEIGHT;

        dest = getLocation(getLocChannel(mLocation));
        rc.setIndicatorString(0, "DEST: " + dest);
// System.out.println("Constructor took: " + Clock.getBytecodeNum());
// System.out.println("START: " + Clock.getBytecodeNum());
// Direction.EAST.ordinal();
// System.out.println("END: " + Clock.getBytecodeNum());

    }


    @Override
    public void run()
        throws GameActionException
    {
        int curRound = Clock.getRoundNum();

        if (diag < 3)
        {
            RobotInfo[] nearbyEnemies =
                rc.senseNearbyRobots(
                    GameConstants.MISSILE_RADIUS_SQUARED,
                    enemyTeam);

            if (nearbyEnemies.length > 0)
            {
                rc.explode();
            }
            else
            {
                left = mLocation.directionTo(dest);
                right = left.rotateRight();
                count = 0;
                while (count < 3)
                {
                    if (count % 2 == 0)
                    {
                        if (rc.canMove(left))
                        {
                            rc.move(left);
                            mLocation = mLocation.add(left);
                            if (left.isDiagonal())
                            {
                                diag++;
                            }
                            break;
                        }
                        left.rotateLeft();
                    }
                    else
                    {
                        if (rc.canMove(right))
                        {
                            rc.move(right);
                            mLocation = mLocation.add(right);
                            if (right.isDiagonal())
                            {
                                diag++;
                            }
                            break;
                        }
                        right.rotateRight();
                    }
                    count++;
                }
                rc.setIndicatorString(2, "COUNT: " + count);
            }

        }
        else
        {
            diag = 0;
        }

        if (Clock.getRoundNum() != curRound)
        {
            System.out.println("MISSILE BEHIND: "
                + (Clock.getRoundNum() - curRound) + " WITH BYTECODES: "
                + Clock.getBytecodesLeft());
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
