package team025;

import battlecode.common.*;

/**
 * Beaver class.
 * 
 * @author Amit Bachchan
 */
public class Beaver
    extends Proletariat
{
    private BeaverTask  task;
    private boolean     reached;
    private MapLocation buildLoc;


    // TODO If a building is destroyed, what happens?

    /**
     * Sets a task.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Beaver(RobotController rc)
        throws GameActionException
    {
        super(rc);
        task = getNextTask();
        reached = false;
    }


    /**
     * Reads communications channels to determine what this Molotok's next task
     * and returns it.
     * 
     * @return This molotok's next task.
     * @throws GameActionException
     */
    private BeaverTask getNextTask()
        throws GameActionException
    {
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        BeaverTask myTask = BeaverTask.getTask(taskNum);

        return myTask;
    }


    /**
     * Updates molotok count and works on this molotok's given task.
     */
    @Override
    public void run()
        throws GameActionException
    {
        super.run();
        rc.broadcast(
            Channels.beaverCount,
            rc.readBroadcast(Channels.beaverCount) + 1);

        rc.setIndicatorString(1, "My task is: " + task);
        if (task == BeaverTask.JOIN_ARMY)
        {
            reached = false;
            this.setDestination(getLocation(Channels.rallyLoc));
            this.bug();
            task = this.getNextTask();
        }
        else if (task == BeaverTask.MINE)
        {
            // TODO Doesn't mine?
            reached = false;
            task = this.getNextTask();
        }
        else
        {
            if (rc.isCoreReady())
            {
                int count = rc.readBroadcast(Channels.buildPathCount);
                if (count < rc.readBroadcast(Channels.buildPathLength))
                {
                    buildLoc = getLocation(count + Channels.buildPath);
                }
                else
                {
                    // TODO Why does this happen? If the build path is complete,
// build anywhere instantly?
                    Direction dir = Direction.NORTH;
                    while (rc.senseTerrainTile(mLocation.add(dir)) != TerrainTile.NORMAL)
                    {
                        dir = dir.rotateRight();
                    }
                    buildLoc = mLocation.add(dir);
                }
                this.setDestination(buildLoc);
                rc.setIndicatorString(0, "My build loc: " + buildLoc);

                if (!reached)
                {
                    rc.setIndicatorString(1, "HERE, trying to go to: "
                        + buildLoc);
                    if (rc.getLocation().x == buildLoc.x
                        && rc.getLocation().y == buildLoc.y)
                    {
                        reached = true;
                    }
                    else if (rc.getLocation().distanceSquaredTo(buildLoc) <= 2)
                    {
                        reached = true;
                    }
                    else
                    {
                        moveTowardsFacing();
                    }
                }

                if (reached)
                {
                    if (build())
                    {
                        rc.broadcast(Channels.buildPathCount, count + 1);
                        task = getNextTask();
                        reached = false;
                        return;
                    }
                }
            }
        }
    }


    /**
     * Moves to the closest direction facing the destination if possible,
     * otherwise does nothing.
     * 
     * @throws GameActionException
     */
    private void moveTowardsFacing()
        throws GameActionException
    {
        facing = mLocation.directionTo(buildLoc);
        // TODO Only move forward or sideways to goal, don't turn back?
        Direction dir = getFreeStrafeDirection(facing);
        if (dir != null)
        {
            rc.move(dir);
        }
    }


    /**
     * Gets the next task and attempts to build the given structure.
     * 
     * @return True if the robot is now building, false otherwise.
     * @throws GameActionException
     */
    private boolean build()
        throws GameActionException
    {
        task = getNextTask();
        RobotType toBuild = getTaskBuildType(task);

        if (toBuild != null && rc.hasBuildRequirements(toBuild))
        {
            Direction dir = mLocation.directionTo(buildLoc);
            if (rc.canBuild(dir, toBuild))
            {
                incrementTask();
                rc.build(dir, toBuild);
                return true;
            }
        }
        return false;
    }


    /**
     * Returns a task's corresponding structure.
     * 
     * @param task
     *            The task needing translation.
     * @return The task's structure or null if there is no corresponding
     *         structure.
     */
    private RobotType getTaskBuildType(BeaverTask task)
    {
        RobotType toBuild = null;
        if (task == BeaverTask.BUILD_BARRACKS)
        {
            toBuild = RobotType.BARRACKS;
        }
        else if (task == BeaverTask.BUILD_HELIPAD)
        {
            toBuild = RobotType.HELIPAD;
        }
        else if (task == BeaverTask.BUILD_MINERFACTORY)
        {
            toBuild = RobotType.MINERFACTORY;
        }
        else if (task == BeaverTask.BUILD_SUPPLYDEPOT)
        {
            toBuild = RobotType.SUPPLYDEPOT;
        }
        else if (task == BeaverTask.BUILD_TANKFACTORY)
        {
            toBuild = RobotType.TANKFACTORY;
        }
        else if (task == BeaverTask.BUILD_AEROSPACE)
        {
            toBuild = RobotType.AEROSPACELAB;
        }
        return toBuild;
    }


    /**
     * Claims a task and updates the communications channels.
     * 
     * @throws GameActionException
     */
    private void incrementTask()
        throws GameActionException
    {
        // TODO What happens when a unit dies?
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        BeaverTask myTask = BeaverTask.getTask(taskNum);
        if (!(myTask == BeaverTask.MINE || myTask == BeaverTask.JOIN_ARMY))
        {
            rc.broadcast(Channels.beaverTasksTaken, tasksTaken + 1);
        }
    }
}
