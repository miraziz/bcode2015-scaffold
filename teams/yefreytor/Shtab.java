package yefreytor;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import battlecode.common.*;

public class Shtab
    extends Atakuyushchiy
{
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

        analyzeTowers();

        broadcastLocation(Channels.rallyLoc, this.findRallyPoint());

        // fills path of where to build buildings, uses lots of bytecodes so
        // split into the run method. Can reduce or raise here according to what
        // we want
        for (int i = 0; i < 100; i++)
        {
            fillBuildingPath();
        }

        // builds minerfactory first then others
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.MINE);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_TANKFACTORY);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_BARRACKS);
        submitBeaverTask(BeaverTask.BUILD_BARRACKS);
        submitBeaverTask(BeaverTask.BUILD_MINERFACTORY);
        sendBeaverTasks();
        this.pathId = 1;
        buildCooldown = 0;

        attacking = false;
        shouldRun = false;
    }


// Finds symmetry in map and ranks towers
// ------------------------------------------------------------------

    private class TowerRank
    {
        MapLocation loc;
        int         score;


        public TowerRank(MapLocation loc, int score)
        {
            this.loc = loc;
            this.score = score;
        }
    }


    private enum Symmetry
    {
        ROTATION,
        X_REFLECTION,
        Y_REFLECTION
    }


    /**
     * gets what type of symmetry is present on the map
     */
    private Symmetry findSymmetry()
    {
        MapLocation allyFurthestTower = null;
        MapLocation enemyFurthestTower = null;
        int dist = 5000;
        for (MapLocation loc : allyTowers)
        {
            if (allyFurthestTower == null)
            {
                allyFurthestTower = loc;
                dist = allyHQ.distanceSquaredTo(allyFurthestTower);
            }
            else if (loc.distanceSquaredTo(allyHQ) > dist)
            {
                dist = loc.distanceSquaredTo(allyHQ);
                allyFurthestTower = loc;
            }
        }
        for (MapLocation loc : enemyTowers)
        {
            if (loc.distanceSquaredTo(enemyHQ) == dist)
            {
                enemyFurthestTower = loc;
                break;
            }
        }
        if (enemyFurthestTower == null)
        {
            return Symmetry.ROTATION;
        }
        int allyXOffset = allyHQ.x - allyFurthestTower.x;
        int allyYOffset = allyHQ.y - allyFurthestTower.y;
        int enemyXOffset = enemyHQ.x - enemyFurthestTower.x;
        int enemyYOffset = enemyHQ.y - enemyFurthestTower.y;

        rc.setIndicatorString(2, "Ally X Offset: " + allyXOffset
            + ", Enemy X Offset: " + enemyXOffset);
        if (allyXOffset == enemyXOffset)
        {
            return Symmetry.X_REFLECTION;
        }
        else if (allyYOffset == enemyYOffset)
        {
            return Symmetry.Y_REFLECTION;
        }
        return Symmetry.ROTATION;
    }


    /**
     * such a big method lol oops. puts the towers in order based on their
     * vulnerability, now in the enemytowers and mytowers, the towers are
     * ordered from most vulnerable to least, but only in the HQ. Needs testing
     */
    private void analyzeTowers()
    {
        class TowerComparator
            implements Comparator<TowerRank>
        {

            @Override
            public int compare(TowerRank o1, TowerRank o2)
            {
                if (o1.score < o2.score)
                {
                    return -1;
                }
                else if (o1.score > o2.score)
                {
                    return 1;
                }
                return 0;
            }

        }
        PriorityQueue<TowerRank> myTowers =
            new PriorityQueue<TowerRank>(new TowerComparator());
        for (MapLocation towerLoc : this.allyTowers)
        {
            int droneVulnerabilityScore = 0;
            int vulnerabilityScore = 100 + towerLoc.distanceSquaredTo(allyHQ);
            MapLocation[] nearby =
                MapLocation.getAllMapLocationsWithinRadiusSq(
                    towerLoc,
                    RobotType.TOWER.sensorRadiusSquared);
            for (MapLocation loc : nearby)
            {
                if (rc.canSenseLocation(loc))
                {
                    TerrainTile tile = rc.senseTerrainTile(loc);

                    if (tile == TerrainTile.OFF_MAP)
                    {
                        droneVulnerabilityScore += 12;
                        vulnerabilityScore -= 8;
                    }
                    else if (tile == TerrainTile.VOID)
                    {
                        droneVulnerabilityScore += 6;
                        vulnerabilityScore -= 4;
                    }
                }
                RobotInfo[] allies = rc.senseNearbyRobots(25, myTeam);
                for (RobotInfo ally : allies)
                {
                    if (ally.type == RobotType.HQ)
                    {
                        vulnerabilityScore -= 35;
                    }
                    else if (ally.type == RobotType.TOWER)
                    {
                        vulnerabilityScore -= 25;
                    }
                }
            }
            droneVulnerabilityScore += vulnerabilityScore;
            myTowers.offer(new TowerRank(towerLoc, vulnerabilityScore));
        }
        Symmetry symmetry = findSymmetry();
        rc.setIndicatorString(0, "Symmetry: " + symmetry);
        int i = 0;
        String str = "enemyTowers in vulnerability order: ";
        while (!myTowers.isEmpty())
        {
            TowerRank t = myTowers.poll();
            this.allyTowers[i] = t.loc;
            int xOffset = allyHQ.x - allyTowers[i].x;
            int yOffset = allyHQ.y - allyTowers[i].y;
            int x = xOffset;
            int y = yOffset;
            if (symmetry == Symmetry.X_REFLECTION)
            {
                x *= -1;
            }
            else if (symmetry == Symmetry.Y_REFLECTION)
            {
                y *= -1;
            }
            this.enemyTowers[i] = new MapLocation(enemyHQ.x + x, enemyHQ.y + y);
            str += enemyTowers[i].toString() + ", ";
            i++;
        }
        rc.setIndicatorString(1, str);

    }

    // -------------------------------------------------------------------------------

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


    // -----------------------------------------------------------------------

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
    // BeaverTask stuff
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


    // ------------------------------------------------------------------------------

    // submits a new building to be built based on mine income
    // maybe just replace with tested timed building?
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
            // rc.setIndicatorString(2, "mineRate: " + mineRate +
// ", spawnRate: "
            // + spawnRate);
            if (mineRate >= spawnRate)
            {
                // submitBeaverTask(BeaverTask.BUILD_HELIPAD);
            }
        }
    }


    // ---------------------------------------------------------------------

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
                if (Clock.getBytecodesLeft() < 511)
                {
                    return;
                }
                rc.transferSupplies(unitSupply, robot.location);
            }
        }
    }


    // -----------------------------------------------------------------------
    // run

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

}
