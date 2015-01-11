package ridavoy;

import java.util.LinkedList;
import battlecode.common.*;

public class Shtab
    extends Atakuyushchiy
{
    MapLocation                    rallyLoc;
    private LinkedList<BeaverTask> tasks;
    private boolean                needsToRun;


    public Shtab(RobotController rc)
        throws GameActionException
    {
        super(rc);

        rallyLoc = findRallyPoint();

        broadcastLocation(Channel.rallyLoc, rallyLoc);
        broadcastLocation(Channel.buildLoc, allyHQ);
        broadcastLocation(123, enemyHQ);
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.BUILD_MINERFACTORY);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_BARRACKS);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_HELIPAD);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.MINE);
        sendBeaverTasks();
    }


    private void submitBeaverTask(BeaverTask task)
    {
        tasks.add(task);
        needsToRun = true;
    }


    private void sendBeaverTasks()
        throws GameActionException
    {
        if (!needsToRun)
        {
            return;
        }
        int tasksTaken = rc.readBroadcast(Channel.beaverTasksTaken);
        for (int i = 0; i < tasksTaken; i++)
        {
            tasks.removeFirst();
        }
        int i = Channel.beaverTask1;
        for (BeaverTask t : tasks)
        {
            rc.broadcast(i, t.value());
            i++;
        }
        rc.broadcast(Channel.beaverTasksTaken, 0);
        needsToRun = false;
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
        throws GameActionException
    {
        super.run();
        int beaverCount = rc.readBroadcast(Channel.beaverCount);

        if (rc.isCoreReady())
        {
            if (beaverCount < Constants.beaverLimit)
            {
                this.spawn(RobotType.BEAVER);
            }
        }
        String str = "Tasks: ";
        for (int i = 0; i < 6; i++)
        {
            str += rc.readBroadcast(Channel.beaverTask1 + i) + ", ";
        }
        rc.setIndicatorString(
            0,
            "Beaver count: " + rc.readBroadcast(Channel.beaverCount));
        rc.setIndicatorString(
            1,
            "Tasks Taken: " + rc.readBroadcast(Channel.beaverTasksTaken));
        rc.setIndicatorString(2, str);

        // do broadcast things with the counts so people know what to do

        rc.broadcast(Channel.beaverCount, 0);

        sendBeaverTasks();

    }
}
