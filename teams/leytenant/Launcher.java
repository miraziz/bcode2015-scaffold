package leytenant;

import battlecode.common.*;

public class Launcher
    extends Fighter
{

    public Launcher(RobotController rc)
        throws GameActionException
    {
        super(rc);
        mTypeChannel = Channels.launcherCount;
        setDestination(enemyHQ);
    }


    @Override
    public void run()
        throws GameActionException
    {
        int curRound = Clock.getRoundNum();
        super.run();

        attackEnemies();

        int took1 = Clock.getRoundNum() - curRound;

        if (rc.isCoreReady())
        {
            if (!runAway())
            {
                bug();
            }
        }

        int took2 = Clock.getRoundNum() - curRound;

        if (took2 > 0 && took2 > took1)
        {
            System.out.println("ATTACKENEMIES TOOK AN EXTRA: " + took1);
            System.out.println("BUGGING TOOK AN EXTRA: " + took2);
            System.out.println("LEFTOVER BYTECODES: "
                + Clock.getBytecodesLeft());
        }
    }


    private boolean attackEnemies()
        throws GameActionException
    {
        // TODO Launch all at once
        if (rc.getMissileCount() > 0)
        {
            RobotInfo[] nearbyEnemies =
                rc.senseNearbyRobots(
                    Constants.MISSILE_MAX_RANGE_SQUARED,
                    enemyTeam);
            for (int i = 0; i < nearbyEnemies.length; ++i)
            {
                Direction toEnemy =
                    bestLaunchDir(0, mLocation, nearbyEnemies[i].location);
                // TODO Is rc.canLaunch(toEnemy) necessary?
                if (toEnemy != null)
                {
                    System.out.println("MISSILE LAUNCH");
                    rc.launchMissile(toEnemy);
                    return true;
                }
            }
        }
        return false;
    }


    private Direction bestLaunchDir(int moves, MapLocation cur, MapLocation dest)
        throws GameActionException
    {
        Direction bestDir = null;
        if (moves < GameConstants.MISSILE_LIFESPAN)
        {
            Direction[] dirs =
                getSpanningForwardDirections(cur.directionTo(dest));
            for (int i = 0; i < dirs.length; ++i)
            {
                MapLocation next = cur.add(dirs[i]);
                if (next.equals(dest))
                {
                    return dirs[i];
                }
                else if (rc.isPathable(RobotType.MISSILE, next))
                {
                    Direction res = bestLaunchDir(moves + 1, next, dest);
                    if (res != null)
                    {
                        bestDir = dirs[i];
                        break;
                    }
                }
            }
        }
        return bestDir;
    }
}
