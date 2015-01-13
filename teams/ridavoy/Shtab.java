package ridavoy;

import java.util.LinkedList;
import battlecode.common.*;

public class Shtab
    extends Atakuyushchiy
{
    MapLocation                    rallyLoc;
    private LinkedList<BeaverTask> tasks;
    boolean                        attacking;
    private int                    buildCooldown;
    private boolean                shouldRun;


    public Shtab(RobotController rc)
        throws GameActionException
    {
        super(rc);
        rc.broadcast(Channels.buildPathCount, 1);
        this.broadcastLocation(Channels.buildPath, allyHQ);
        this.pathId = 1;
        buildCooldown = 0;

        attacking = false;
        shouldRun = false;
        rallyLoc = findRallyPoint();

        broadcastLocation(Channels.rallyLoc, rallyLoc);
        broadcastLocation(Channels.buildLoc, allyHQ);
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.MINE);
        submitBeaverTask(BeaverTask.BUILD_HELIPAD);
        submitBeaverTask(BeaverTask.BUILD_HELIPAD);
        sendBeaverTasks();
    }


    private void submitBeaverTask(BeaverTask task)
    {
        tasks.addFirst(task);
        shouldRun = true;
    }


    private void sendBeaverTasks()
        throws GameActionException
    {
        if (!shouldRun)
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
        needMoreBuildings();
        String str = "tasks: ";
        for (int i = 0; i < 10; i++)
        {
            str += rc.readBroadcast(Channels.beaverTask1 + i) + ", ";
        }
        rc.setIndicatorString(2, str);

        int beaverCount = rc.readBroadcast(Channels.beaverCount);
        if (rc.isCoreReady())
        {
            if (beaverCount < Constants.beaverLimit)
            {
                this.spawnToEnemy(RobotType.BEAVER);
            }
        }
        // do broadcast things with the counts so people know what to do

        rc.broadcast(Channels.beaverCount, 0);
        rc.broadcast(Channels.helipadCount, 0);
        rc.broadcast(Channels.minerFactoryCount, 0);
        rc.broadcast(Channels.barracksCount, 0);

        sendBeaverTasks();
        shouldRun = false;

        if (!attacking && Clock.getRoundNum() > 1500)
        {
            broadcastLocation(Channels.rallyLoc, enemyHQ);
            attacking = true;
        }
    }


    // returns a robottype that should be built next
    // or returns null if no building needs to be built.
    private void needMoreBuildings()
        throws GameActionException
    {
        int roundNum = Clock.getRoundNum();
        buildCooldown++;
        rc.setIndicatorString(0, "round num: " + roundNum
            + ", build cooldown: " + buildCooldown);
        if (roundNum > 100 && roundNum % 10 == 0 && buildCooldown > 10)
        {
            buildCooldown = 0;
            int mined = rc.readBroadcast(Channels.miningTotal);
            rc.broadcast(Channels.miningTotal, 0);
            double mineRate = mined / 10;
            int barracksCount = rc.readBroadcast(Channels.barracksCount);
            int helipadCount = rc.readBroadcast(Channels.helipadCount);
            int tankFactoryCount = rc.readBroadcast(Channels.tankFactoryCount);
            rc.setIndicatorString(1, "helipad count: " + helipadCount);
            double spawnRate =
                (Constants.barracksRate * barracksCount)
                    + (Constants.tankFactoryRate * tankFactoryCount)
                    + (Constants.helipadRate * helipadCount);
            rc.setIndicatorString(2, "mineRate: " + mineRate + ", spawnRate: "
                + spawnRate);
            if (mineRate >= spawnRate)
            {
                // submitBeaverTask(BeaverTask.BUILD_HELIPAD);
            }
        }
    }


    /**
     * Evenly distributes all of Shtab's supply.
     */
    @Override
    public void transferSupplies()
        throws GameActionException
    {
        double totSupply = rc.getSupplyLevel();

        RobotInfo[] nearbyAllies =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);
        if (nearbyAllies.length > 0)
        {
            int unitSupply = (int)(totSupply / nearbyAllies.length);
            for (RobotInfo robot : nearbyAllies)
            {
                rc.transferSupplies(unitSupply, robot.location);
            }
        }
    }

}
