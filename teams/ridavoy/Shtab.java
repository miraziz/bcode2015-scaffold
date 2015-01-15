package ridavoy;

import java.util.ArrayDeque;
import java.util.HashSet;
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

        // fillbuildingpath setup
        rc.broadcast(Channels.buildPathCount, 0);
        visited = new HashSet<MapLocation>();
        queue = new ArrayDeque<MapLocation>();
        queue.offer(allyHQ);

        // Fills the path for where to build for beavers, raise or lower the
        // loop to change the amount of turns wasted.
        // It will be eventually completed in the run method regardless, so it
        // does not matter
        // We have 20 rounds free anyways if building a minerfactory first, so
        // it's fine to make this run a while.
        for (int i = 0; i < 600; i++)
        {
            fillBuildingPath();
        }

        // builds minerfactory first
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.MINE);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_TANKFACTORY);
        submitBeaverTask(BeaverTask.BUILD_BARRACKS);
        submitBeaverTask(BeaverTask.BUILD_HELIPAD);
        submitBeaverTask(BeaverTask.BUILD_MINERFACTORY);
        sendBeaverTasks();
        this.pathId = 1;
        buildCooldown = 0;

        attacking = false;
        shouldRun = false;
    }

// RUNS ONE TIME
// ------------------------------------------------------------------

    private HashSet<MapLocation> visited;
    ArrayDeque<MapLocation>      queue;
    private int                  buildingCount;


    private void fillBuildingPath()
        throws GameActionException
    {
        if (queue.isEmpty())
        {
            return;
        }
        else
        {
            MapLocation cur = queue.poll();
            if (visited.contains(cur) || !rc.canSenseLocation(cur))
            {
                return;
            }
            visited.add(cur);
            if (cur != allyHQ && rc.senseTerrainTile(cur) == TerrainTile.NORMAL)
            {
                broadcastLocation(Channels.buildPath + buildingCount, cur);
                buildingCount++;
            }
            queue.offer(cur.add(Direction.NORTH_WEST));
            queue.offer(cur.add(Direction.NORTH_EAST));
            queue.offer(cur.add(Direction.SOUTH_EAST));
            queue.offer(cur.add(Direction.SOUTH_WEST));
        }
        rc.broadcast(Channels.buildPathLength, buildingCount);
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


    // ---------------------------------------------------------------------
    // Run from the run() function
    // ---------------------------------------------------------------------

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


    @Override
    public void run()
        throws GameActionException
    {
        int roundNum = Clock.getRoundNum();
        super.run();

        int beaverCount = rc.readBroadcast(Channels.beaverCount);
        if (rc.isCoreReady() && roundNum >= 20)
        {
            if (beaverCount < Constants.beaverLimit)
            {
                int buildCount = rc.readBroadcast(Channels.buildPathCount);
                MapLocation loc = getLocation(Channels.buildPath + buildCount);
                this.spawn(
                    rc.getLocation().directionTo(loc).rotateRight(),
                    RobotType.BEAVER);
            }
        }
        // do broadcast things with the counts so people know what to do

        fillBuildingPath();

        needMoreBuildings();
        rc.broadcast(Channels.beaverCount, 0);
        rc.broadcast(Channels.helipadCount, 0);
        rc.broadcast(Channels.minerFactoryCount, 0);
        rc.broadcast(Channels.barracksCount, 0);
        rc.broadcast(Channels.tankFactoryCount, 0);

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
        if (roundNum > 100 && roundNum % 10 == 0 && buildCooldown > 10)
        {
            buildCooldown = 0;
            int mined = rc.readBroadcast(Channels.miningTotal);
            rc.broadcast(Channels.miningTotal, 0);
            double mineRate = mined / 10;
            int barracksCount = rc.readBroadcast(Channels.barracksCount);
            int helipadCount = rc.readBroadcast(Channels.helipadCount);
            int tankFactoryCount = rc.readBroadcast(Channels.tankFactoryCount);
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
