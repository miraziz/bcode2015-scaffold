package ridavoy;

import java.util.LinkedList;
import battlecode.common.*;

public class Shtab
    extends Atakuyushchiy
{
    MapLocation                    rallyLoc;
    private LinkedList<BeaverTask> tasks;
    private boolean                needsToRun;
    private boolean                attacking;
    private int                    buildCooldown;


    public Shtab(RobotController rc)
        throws GameActionException
    {
        super(rc);

        buildCooldown = 0;

        attacking = false;
        rallyLoc = findRallyPoint();

        broadcastLocation(Channels.rallyLoc, rallyLoc);
        broadcastLocation(Channels.buildLoc, allyHQ);
        broadcastLocation(123, enemyHQ);
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.BUILD_MINERFACTORY);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_HELIPAD);
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
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        for (int i = 0; i < tasksTaken; i++)
        {
            tasks.removeFirst();
        }
        int i = Channels.beaverTask1;
        for (BeaverTask t : tasks)
        {
            rc.broadcast(i, t.value());
            i++;
        }
        rc.broadcast(Channels.beaverTasksTaken, 0);
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
        int beaverCount = rc.readBroadcast(Channels.beaverCount);

        if (rc.isCoreReady())
        {
            if (beaverCount < Constants.beaverLimit)
            {
                this.spawnToEnemy(RobotType.BEAVER);
            }
        }
        String str = "Tasks: ";
        for (int i = 0; i < 6; i++)
        {
            str += rc.readBroadcast(Channels.beaverTask1 + i) + ", ";
        }
        rc.setIndicatorString(
            0,
            "Beaver count: " + rc.readBroadcast(Channels.beaverCount));
        rc.setIndicatorString(
            1,
            "Tasks Taken: " + rc.readBroadcast(Channels.beaverTasksTaken));
        rc.setIndicatorString(2, str);

        // do broadcast things with the counts so people know what to do

        rc.broadcast(Channels.beaverCount, 0);

        sendBeaverTasks();

        if (!attacking && Clock.getRoundNum() > 1500)
        {
            broadcastLocation(Channels.rallyLoc, enemyHQ);
            attacking = true;
        }
    }


    // returns a robottype that should be built next
    // or returns null if no building needs to be built.
    private RobotType needMoreBuildings()
        throws GameActionException
    {
        int roundNum = Clock.getRoundNum();
        buildCooldown++;
        if (roundNum > 100 && roundNum % 5 == 0 && buildCooldown < 50)
        {
            int mined = rc.readBroadcast(Channels.miningTotal);
            rc.broadcast(Channels.miningTotal, 0);
            double mineRate = mined / 5;
            int barracksCount = rc.readBroadcast(Channels.barracksCount);
            int helipadCount = rc.readBroadcast(Channels.helipadCount);
            int tankFactoryCount = rc.readBroadcast(Channels.tankFactoryCount);
            double spawnRate =
                (Constants.barracksRate * barracksCount)
                    + (Constants.tankFactoryRate * tankFactoryCount)
                    + (Constants.helipadRate * helipadCount);
            if (mineRate >= spawnRate)
            {
                tasks.add(BeaverTask.BUILD_HELIPAD);
            }
        }
        return null;
    }
}
