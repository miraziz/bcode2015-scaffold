package basherbot;

import java.util.LinkedList;
import java.util.PriorityQueue;
import battlecode.common.*;

/**
 * HQ class.
 * 
 * @author Amit Bachchan
 */
public class HQ
    extends AttackBuilding
{
    private LinkedList<BeaverTask>  tasks;
    boolean                         attacking;
    private boolean                 shouldRun;
    MapLocation                     defaultRallyLoc;
    private int                     maxBuildingCount;
    private boolean[][]             visited;
    private LinkedList<MapLocation> queue;

    private int                     buildingCount;


    public HQ(RobotController rc)
        throws GameActionException
    {
        super(rc);

        // Set rally
        // broadcastLocation(Channels.rallyLoc, findRallyPoint());

        broadcastLocation(Channels.rallyLoc, enemyHQ);

        // TODO Set miner limits based on map size

        // TODO Uh.........
        // builds minerfactory first then others
        tasks = new LinkedList<BeaverTask>();
        submitBeaverTask(BeaverTask.BUILD_HANDWASHSTATION);
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
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_SUPPLYDEPOT);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_AEROSPACE);
        submitBeaverTask(BeaverTask.BUILD_TRAININGFIELD);
        submitBeaverTask(BeaverTask.BUILD_TECHINSTITUTE);
        submitBeaverTask(BeaverTask.BUILD_HELIPAD);
        submitBeaverTask(BeaverTask.BUILD_MINERFACTORY);

        sendBeaverTasks();
        this.pathId = 1;

        // TODO What does shouldRun do for the HQ?
        rc.broadcast(Channels.attacking, 0);
        attacking = false;
        shouldRun = false;

        System.out.println("Took " + Clock.getBytecodeNum()
            + " bytecodes so far");

        maxBuildingCount = Constants.MAXIMUM_BUILDINGS;

        visited = new boolean[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        queue = new LinkedList<MapLocation>();
        queue.offer(allyHQ);
        System.out.println("(" + allyHQ.x + ", " + allyHQ.y + ")"
            + " x offset: " + mapOffsetX + ", y offset: " + mapOffsetY);
        visited[allyHQ.x - mapOffsetX][allyHQ.y - mapOffsetY] = true;

        fillBuildingPath();

        // Wait for towers to calculate vulnerability
        // analyzeTowers();
    }


    // -----------------------------------------------------------------------
    // run

    @Override
    public void run()
        throws GameActionException
    {
        super.run();

        allyTowers = rc.senseTowerLocations();
        if (rc.readBroadcast(Channels.buildPathCount) >= 0.8 * maxBuildingCount)
        {
            maxBuildingCount += Constants.MAXIMUM_BUILDINGS;
            fillBuildingPath();
        }
        else if (buildingCount < maxBuildingCount)
        {
            fillBuildingPath();
        }

        int roundNum = Clock.getRoundNum();
        if (roundNum > 500)
        {
            Constants.beaverLimit = 6;
        }
        else if (roundNum > 400)
        {
            Constants.beaverLimit = 4;
        }
        else if (roundNum > 25)
        {
            Constants.beaverLimit = 2;
        }
        else
        {
            Constants.beaverLimit = 1;
        }

        rc.broadcast(Channels.minerPotato, 4 - roundNum / 501);

        int beaverCount = rc.readBroadcast(Channels.beaverCount);
        if (rc.isCoreReady() && roundNum >= 20)
        {
            if (beaverCount < Constants.beaverLimit)
            {
                int buildCount = rc.readBroadcast(Channels.buildPathCount);
                MapLocation loc = getLocation(Channels.buildPath + buildCount);
                this.spawn(
                    mLocation.directionTo(loc).rotateRight(),
                    RobotType.BEAVER);
            }
        }
        // do broadcast things with the counts so people know what to do

        manageSpawnsAndBuildings();

        sendBeaverTasks();
        shouldRun = false;

        manageRallyLoc(roundNum);
    }


// Finds symmetry in map and ranks towers
// ------------------------------------------------------------------

    private void manageRallyLoc(int roundNum)
        throws GameActionException
    {
        if (!attacking && Clock.getRoundNum() > Constants.attackRound)
        {
            attacking = true;
            rc.broadcast(Channels.attacking, 1);
        }
        if (!attacking)
        {
            MapLocation newRally = getLocation(Channels.highestEnemyHealthLoc);
            if (newRally != null)
            {
                broadcastLocation(Channels.rallyLoc, newRally);
            }
            else
            {
                broadcastLocation(Channels.rallyLoc, enemyHQ);
            }
        }
        rc.broadcast(Channels.highestEnemyHealth, 0);
        rc.broadcast(Channels.highestEnemyHealthLoc, 0);
    }


    // -------------------------------------------------------------------------------

    /**
     * @throws GameActionException
     */
    private void fillBuildingPath()
        throws GameActionException
    {
        while (!queue.isEmpty() && buildingCount < maxBuildingCount
            && Clock.getBytecodesLeft() > 5000)
        {
            MapLocation cur = queue.poll();

            if (cur != allyHQ && rc.senseTerrainTile(cur) == TerrainTile.NORMAL)
            {
                broadcastLocation(Channels.buildPath + buildingCount, cur);
                buildingCount++;
            }

            for (int i = 1; i < 8; i += 2)
            {
                MapLocation next = cur.add(directions[i]);
                if (!visited[next.x - mapOffsetX][next.y - mapOffsetY]
                    && rc.canSenseLocation(next)
                    && rc.senseTerrainTile(next) != TerrainTile.OFF_MAP)
                {
                    visited[next.x - mapOffsetX][next.y - mapOffsetY] = true;
                    queue.offer(next);
                }
            }
        }
        rc.broadcast(Channels.buildPathLength, buildingCount);
    }


    // -----------------------------------------------------------------------

    /**
     * Finds the average position of allied towers and HQ and returns the
     * closest structure to that average position.
     * 
     * @return The location of the tower closest to the center of mass of the
     *         initial allied buildings on the map.
     */
    private MapLocation findRallyPoint()
    {
        int xAvg = 0;
        int yAvg = 0;
        for (MapLocation loc : allyTowers)
        {
            xAvg += loc.x;
            yAvg += loc.y;
        }
        xAvg += allyHQ.x;
        yAvg += allyHQ.y;
        xAvg /= (allyTowers.length + 1);
        yAvg /= (allyTowers.length + 1);
        MapLocation avg = new MapLocation(xAvg, yAvg);

        MapLocation closest = allyHQ;
        int closestDist = closest.distanceSquaredTo(avg);
        for (MapLocation loc : allyTowers)
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
    private void manageSpawnsAndBuildings()
        throws GameActionException
    {
        // building stuff
        int roundNum = Clock.getRoundNum();
        double myOre = rc.getTeamOre();

        int barracksCount = rc.readBroadcast(Channels.barracksCount);
        int helipadCount = rc.readBroadcast(Channels.helipadCount);
        int tankFactoryCount = rc.readBroadcast(Channels.tankFactoryCount);
        int aerospaceCount = rc.readBroadcast(Channels.aerospaceCount);

        int soldierCount = rc.readBroadcast(Channels.soldierCount);
        int basherCount = rc.readBroadcast(Channels.basherCount);
        int tankCount = rc.readBroadcast(Channels.tankCount);
        int droneCount = rc.readBroadcast(Channels.droneCount);

        rc.broadcast(Channels.shouldSpawnBasher, 0);
        rc.broadcast(Channels.shouldSpawnSoldier, 0);
        rc.broadcast(Channels.shouldSpawnDrone, 0);
        if (droneCount < Constants.droneLimit)
        {
            rc.broadcast(Channels.shouldSpawnDrone, 1);
        }
        if (soldierCount < Constants.soldierLimit)
        {
            rc.broadcast(Channels.shouldSpawnSoldier, 1);
        }
        if (!attacking && tankCount >= Constants.requiredTanksForAttack)
        {
            attacking = true;
            System.out.println("Tanks should attack now");
            rc.broadcast(Channels.attacking, 1);
        }

        rc.broadcast(Channels.beaverCount, 0);
        rc.broadcast(Channels.helipadCount, 0);
        rc.broadcast(Channels.minerFactoryCount, 0);
        rc.broadcast(Channels.barracksCount, 0);
        rc.broadcast(Channels.tankFactoryCount, 0);
        rc.broadcast(Channels.soldierCount, 0);
        rc.broadcast(Channels.basherCount, 0);
        rc.broadcast(Channels.tankCount, 0);
        rc.broadcast(Channels.droneCount, 0);
    }


    /**
     * such a big method lol oops. puts the towers in order based on their
     * vulnerability, now in the enemytowers and mytowers, the towers are
     * ordered from most vulnerable to least, but only in the HQ. Needs testing
     * 
     * @throws GameActionException
     */
    private void analyzeTowers()
        throws GameActionException
    {
        System.out.println("Starting tower analysis");

        PriorityQueue<TowerRank> myTowers = new PriorityQueue<TowerRank>();
        for (int i = 0; i < allyTowers.length; i++)
        {
            int vulnerabilityScore =
                rc.readBroadcast(Channels.towerVulnerability
                    + Constants.CHANNELS_PER_TOWER_VULN * i + 1);
            myTowers.offer(new TowerRank(allyTowers[i], vulnerabilityScore));
        }

        System.out.println("Towers analyzed");
        Symmetry symmetry = findSymmetry();

        int i = 0;
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
            i++;
        }

    }


    private enum Symmetry
    {
        ROTATION,
        X_REFLECTION,
        Y_REFLECTION
    }


    /**
     * Determines the symmetry of the map.
     * 
     * @return The symmetry of the map.
     */
    private Symmetry findSymmetry()
    {
        if (allyHQ.x == enemyHQ.x)
        {
            int yAxis = (allyHQ.y + enemyHQ.y) / 2;
            boolean works = true;
            for (MapLocation enemy : enemyTowers)
            {
                works = false;
                for (MapLocation ally : allyTowers)
                {
                    if (enemy.x == ally.x
                        && ((enemy.y - yAxis) == -(ally.y - yAxis)))
                    {
                        works = true;
                        break;
                    }
                }
                if (!works)
                {
                    break;
                }
            }

            if (works)
            {
                return Symmetry.X_REFLECTION;
            }
            else
            {
                return Symmetry.ROTATION;
            }
        }
        else if (allyHQ.y == enemyHQ.y)
        {
            int xAxis = (allyHQ.x + enemyHQ.x) / 2;
            boolean works = true;
            for (MapLocation enemy : enemyTowers)
            {
                works = false;
                for (MapLocation ally : allyTowers)
                {
                    if (enemy.y == ally.y
                        && ((enemy.x - xAxis) == -(ally.x - xAxis)))
                    {
                        works = true;
                        break;
                    }
                }
                if (!works)
                {
                    break;
                }
            }

            if (works)
            {
                return Symmetry.Y_REFLECTION;
            }
            else
            {
                return Symmetry.ROTATION;
            }
        }
        else
        {
            return Symmetry.ROTATION;
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
        RobotInfo[] allies =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);

        for (RobotInfo r : allies)
        {
            if (Clock.getBytecodesLeft() < 550)
            {
                return;
            }
            if (r.type == RobotType.COMMANDER || r.type.canSpawn())
            {
                rc.transferSupplies(
                    (int)(rc.getSupplyLevel() * .90),
                    r.location);
                return;
            }
        }
    }
}
