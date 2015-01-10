package ridavoy;

import battlecode.common.*;

public class Shtab
    extends Atakuyushchiy
{

    public Shtab(RobotController rc)
        throws GameActionException
    {
        super(rc);

        MapLocation rallyPoint = findRallyPoint(); // broadcast this location
        // broadcastLocation(*channel*, rallyPoint);

        /* testing code */
        rc.spawn(Direction.SOUTH, RobotType.BEAVER);
        this.broadcastLocation(1, enemyHQ);
    }


    private MapLocation findRallyPoint()
    {
        int xAvg = 0;
        int yAvg = 0;
        for (MapLocation loc : this.allyTowers)
        {
            xAvg += loc.x;
            yAvg += loc.y;
        }
        xAvg += allyHQ.x;
        yAvg += allyHQ.y;
        xAvg /= allyTowers.length + 1;
        yAvg /= allyTowers.length + 1;
        MapLocation avg = new MapLocation(xAvg, yAvg);
        MapLocation closest = allyHQ;
        int closestDist = closest.distanceSquaredTo(avg);
        for (MapLocation loc : this.allyTowers)
        {
            int dist = loc.distanceSquaredTo(avg);
            if (dist <= closestDist)
            {
                closestDist = dist;
                closest = loc;
            }
        }
        return closest;
    }


    @Override
    public void run()
    {
        // TODO Auto-generated method stub

    }
}
