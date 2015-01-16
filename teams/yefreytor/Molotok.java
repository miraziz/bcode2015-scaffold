package yefreytor;

import battlecode.common.*;

/**
 * Beaver class.
 * 
 * @author Amit Bachchan
 */
public class Molotok
    extends Proletariat
{
    private MolotokTask task;
    private boolean     reached;
    private MapLocation buildLoc;


    // TODO If a building is destroyed, what happens?

    /**
     * Sets a task.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Molotok(RobotController rc)
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
    private MolotokTask getNextTask()
        throws GameActionException
    {
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        MolotokTask myTask = MolotokTask.getTask(taskNum);

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
        if (task == MolotokTask.JOIN_ARMY)
        {
            reached = false;
            this.setDestination(getLocation(Channels.rallyLoc));
            this.bug();
            task = this.getNextTask();
        }
        else if (task == MolotokTask.MINE)
        {
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
                    Direction dir = Direction.NORTH;
                    while (rc.senseTerrainTile(rc.getLocation().add(dir)) != TerrainTile.NORMAL)
                    {
                        dir = dir.rotateRight();
                    }
                    buildLoc = rc.getLocation().add(dir);
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


    private void moveTowardsFacing()
        throws GameActionException
    {
        facing = rc.getLocation().directionTo(buildLoc);
        Direction dir = facing;
        Direction left = dir.rotateRight();
        Direction right = dir;
        int count = 0;
        while (!rc.canMove(dir) && count < 8)
        {
            if (count % 2 == 0)
            {
                left = left.rotateLeft();
                dir = left;
            }
            else
            {
                right = right.rotateRight();
                dir = right;
            }
            count++;
        }
        if (count < 8)
        {
            rc.move(dir);
        }

    }


    private boolean build()
        throws GameActionException
    {
        task = getNextTask();
        RobotType toBuild = getTaskBuildType(task);
        if (toBuild == null)
        {
            return false;
        }
        if (rc.hasBuildRequirements(toBuild))
        {
            Direction dir = rc.getLocation().directionTo(buildLoc);
            if (!rc.canBuild(dir, toBuild))
            {
                return false;
            }
            else
            {
                incrementTask();
                rc.build(dir, toBuild);
                return true;
            }
        }
        return false;
    }


    private RobotType getTaskBuildType(MolotokTask task)
    {
        RobotType toBuild = null;
        if (task == MolotokTask.BUILD_BARRACKS)
        {
            toBuild = RobotType.BARRACKS;
        }
        else if (task == MolotokTask.BUILD_HELIPAD)
        {
            toBuild = RobotType.HELIPAD;
        }
        else if (task == MolotokTask.BUILD_MINERFACTORY)
        {
            toBuild = RobotType.MINERFACTORY;
        }
        else if (task == MolotokTask.BUILD_SUPPLYDEPOT)
        {
            toBuild = RobotType.SUPPLYDEPOT;
        }
        else if (task == MolotokTask.BUILD_TANKFACTORY)
        {
            toBuild = RobotType.TANKFACTORY;
        }
        return toBuild;
    }


    private void incrementTask()
        throws GameActionException
    {
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        MolotokTask myTask = MolotokTask.getTask(taskNum);
        if (!(myTask == MolotokTask.MINE || myTask == MolotokTask.JOIN_ARMY))
        {
            rc.broadcast(Channels.beaverTasksTaken, tasksTaken + 1);
        }
    }
}
