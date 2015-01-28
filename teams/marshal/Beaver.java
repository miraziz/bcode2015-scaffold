package marshal;

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
    private boolean     building;


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
        mTypeChannel = Channels.beaverCount;
        building = false;
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
        rc.broadcast(Channels.beaverTasksTaken, tasksTaken + 1);
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

        if (rc.isCoreReady())
        {
            if (building)
            {
                building = false;
                task = getNextTask();
            }
            RobotInfo robAtBuildLoc = null;
            if (buildLoc != null && rc.canSenseLocation(buildLoc))
            {
                robAtBuildLoc = rc.senseRobotAtLocation(buildLoc);
            }
            if (buildLoc == null
                || (robAtBuildLoc != null && robAtBuildLoc.type.isBuilding))
            {
                int count = rc.readBroadcast(Channels.buildPathCount);
                buildLoc = getLocation(count + Channels.buildPath);
                rc.broadcast(Channels.buildPathCount, count + 1);
            }
            this.setDestination(buildLoc);

            if (!reached)
            {
                if (rc.getLocation().x == buildLoc.x
                    && rc.getLocation().y == buildLoc.y)
                {
                    reached = true;
                }
                else if (rc.getLocation().isAdjacentTo(buildLoc))
                {
                    reached = true;
                }
                else
                {
                    bugWithCounter();
                }
            }
            if (reached)
            {
                if (build())
                {
                    building = true;
                    reached = false;
                    return;
                }
                else
                {
                    moveTowardsFacing();
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
        if (facing == Direction.OMNI || facing == Direction.NONE)
        {
            facing = Direction.NORTH;
        }
        // TODO Only move forward or sideways to goal, don't turn back?
        Direction dir = this.getFreeDirection(facing);
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
        RobotType toBuild = getTaskBuildType(task);

        if (toBuild != null && rc.hasBuildRequirements(toBuild))
        {
            Direction dir = mLocation.directionTo(buildLoc);
            if (rc.canBuild(dir, toBuild))
            {
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
        else if (task == BeaverTask.BUILD_TECHINSTITUTE)
        {
            toBuild = RobotType.TECHNOLOGYINSTITUTE;
        }
        else if (task == BeaverTask.BUILD_TRAININGFIELD)
        {
            toBuild = RobotType.TRAININGFIELD;
        }
        else if (task == BeaverTask.BUILD_HANDWASHSTATION)
        {
            toBuild = RobotType.HANDWASHSTATION;
        }
        return toBuild;
    }

}
