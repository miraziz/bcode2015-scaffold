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
        fillBuildingPath();
        String str = "Path: ";
        for (int i = 0; i < 16; i++)
        {
            str += getLocation(Channels.buildPath + i);
        }
        rc.setIndicatorString(2, str);
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.MINE);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_MINERFACTORY);
        sendBeaverTasks();
        this.pathId = 1;
        buildCooldown = 0;

        attacking = false;
        shouldRun = false;
    }


    private void fillBuildingPath()
        throws GameActionException
    {
        rc.broadcast(Channels.buildPathCount, 0);

        int count = 0;
        MapLocation[] allSquares =
            MapLocation.getAllMapLocationsWithinRadiusSq(
                rc.getLocation(),
                rc.getType().sensorRadiusSquared);
        HashSet<MapLocation> visited = new HashSet<MapLocation>();
        ArrayDeque<MapLocation> queue = new ArrayDeque<MapLocation>();
        queue.offer(allyHQ);
        while (!queue.isEmpty())
        {
            MapLocation cur = queue.poll();
            if (visited.contains(cur) || !rc.canSenseLocation(cur))
            {
                continue;
            }
            visited.add(cur);
            if (cur != allyHQ && rc.senseTerrainTile(cur) == TerrainTile.NORMAL)
            {
                broadcastLocation(Channels.buildPath + count, cur);
                count++;
            }
            queue.offer(cur.add(Direction.NORTH_WEST));
            queue.offer(cur.add(Direction.NORTH_EAST));
            queue.offer(cur.add(Direction.SOUTH_EAST));
            queue.offer(cur.add(Direction.SOUTH_WEST));
        }
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
        int roundNum = Clock.getRoundNum();
        super.run();
        needMoreBuildings();

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
