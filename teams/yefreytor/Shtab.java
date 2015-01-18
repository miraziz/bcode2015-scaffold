package yefreytor;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import battlecode.common.*;

/**
 * HQ class.
 * 
 * @author Amit Bachchan
 */
public class Shtab
    extends Atakuyushchiy
{
    private LinkedList<MolotokTask> tasks;
    boolean                         attacking;
    private int                     buildCooldown;
    private boolean                 shouldRun;
    HashSet<MapLocation>            destroyedTowers;

    private boolean[][]             visited;
    ArrayDeque<MapLocation>         queue;
    private int                     buildingCount;


    public Shtab(RobotController rc)
        throws GameActionException
    {
        super(rc);

        // Spawn before calculating anything. Buys 10 turns.
        spawnToEnemy(RobotType.BEAVER);

        // Set rally
        broadcastLocation(Channels.rallyLoc, findRallyPoint());

        // TODO Set miner limits based on map size

        // TODO Uh.........
        // builds minerfactory first then others
        tasks = new LinkedList<MolotokTask>();
        submitMolotokTask(MolotokTask.MINE);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_TANKFACTORY);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_TANKFACTORY);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_SUPPLYDEPOT);
        submitMolotokTask(MolotokTask.BUILD_BARRACKS);
        submitMolotokTask(MolotokTask.BUILD_BARRACKS);
        submitMolotokTask(MolotokTask.BUILD_MINERFACTORY);
        sendMolotokTasks();
        this.pathId = 1;
        buildCooldown = 0;

        // TODO What does shouldRun do for the HQ?
        attacking = false;
        shouldRun = false;

        System.out.println("Took " + Clock.getBytecodeNum()
            + " bytecodes so far");

        // fillbuildingpath setup
        rc.broadcast(Channels.buildPathCount, 0);
        visited =
            new boolean[2 * GameConstants.MAP_MAX_WIDTH][2 * GameConstants.MAP_MAX_HEIGHT];
        fillBuildingPath();

        // Wait for towers to calculate vulnerability
        rc.yield();
        rc.yield();
        destroyedTowers = new HashSet<MapLocation>();
        analyzeTowers();
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
                    mLocation.directionTo(loc).rotateRight(),
                    RobotType.BEAVER);
            }
        }
        // do broadcast things with the counts so people know what to do

        needMoreBuildings();
        rc.broadcast(Channels.beaverCount, 0);
        rc.broadcast(Channels.helipadCount, 0);
        rc.broadcast(Channels.minerFactoryCount, 0);
        rc.broadcast(Channels.barracksCount, 0);
        rc.broadcast(Channels.tankFactoryCount, 0);

        sendMolotokTasks();
        shouldRun = false;

        if (!attacking && Clock.getRoundNum() > 1500)
        {

            broadcastLocation(Channels.rallyLoc, this.enemyHQ);
            if (enemyTowers.length > 0)
            {
                broadcastLocation(Channels.rallyLoc, this.enemyTowers[0]);
            }
            attacking = true;
        }
        if (attacking)
        {
            broadcastLocation(Channels.rallyLoc, this.enemyHQ);
            // TODO Make units broadcast the most recently destroyed tower as
            // they deal the final blow to it, to move on to the next tower.
            MapLocation destroyed = getLocation(Channels.destroyedTower);
            if (destroyed != null)
            {
                this.destroyedTowers.add(destroyed);
                rc.broadcast(Channels.destroyedTower, 0);
            }
            for (int i = 0; i < this.enemyTowers.length; i++)
            {
                if (!this.destroyedTowers.contains(enemyTowers[i]))
                {
                    broadcastLocation(Channels.rallyLoc, enemyTowers[i]);
                }
            }

        }
    }


// Finds symmetry in map and ranks towers
// ------------------------------------------------------------------

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

        System.out.println("Symmetry detected");
    }


    // -------------------------------------------------------------------------------

    /**
     * @throws GameActionException
     */
    private void fillBuildingPath()
        throws GameActionException
    {
        queue = new ArrayDeque<MapLocation>();
        queue.offer(allyHQ);

        // TODO Leaves at least 500 bytecodes after completion. May need to be
// changed if there aren't enough bytecodes for supply transfer.

        while (!queue.isEmpty()
            && Clock.getBytecodesLeft() > 2 * Constants.BUILD_PATH_BYTECODES)
        {
            MapLocation cur = queue.poll();
            if (!visited[cur.x - mapOffsetX][cur.y - mapOffsetY]
                && rc.senseTerrainTile(cur) == TerrainTile.NORMAL)
            {
                visited[cur.x - mapOffsetX][cur.y - mapOffsetY] = true;

                if (cur != allyHQ)
                {
                    broadcastLocation(Channels.buildPath + buildingCount, cur);
                    buildingCount++;
                }

                for (int i = 1; i < 8; i += 2)
                {
                    MapLocation next = cur.add(directions[i]);
                    if (!visited[next.x - mapOffsetX][next.y - mapOffsetY])
                    {
                        queue.offer(next);
                    }
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

    private void submitMolotokTask(MolotokTask task)
    {
        tasks.addFirst(task);
        shouldRun = true;
    }


    private void sendMolotokTasks()
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
        for (MolotokTask t : tasks)
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
        if (totSupply == 0)
        {
            return;
        }
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
}
