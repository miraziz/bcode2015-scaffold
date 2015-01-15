package ridavoy;

import battlecode.common.*;

/**
 * Beaver class.
 * 
 * @author Miraziz
 */
public class Molotok
    extends Proletariat
{
    private BeaverTask  task;
    private boolean     reached;
    private MapLocation buildLoc;


    // TODO If a building is destroyed, what happens?

    public Molotok(RobotController rc)
        throws GameActionException
    {
        super(rc);
        task = getNextTask();
        reached = false;
    }


    private BeaverTask getNextTask()
        throws GameActionException
    {
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        BeaverTask myTask = BeaverTask.getTask(taskNum);

        return myTask;
    }


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
            reached = false;
            task = this.getNextTask();
        }
        else
        {
            if (rc.isCoreReady())
            {
                int count = rc.readBroadcast(Channels.buildPathCount);
                buildLoc = getLocation(count + Channels.buildPath);
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
        return toBuild;
    }


    private void incrementTask()
        throws GameActionException
    {
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        BeaverTask myTask = BeaverTask.getTask(taskNum);
        if (!(myTask == BeaverTask.MINE || myTask == BeaverTask.JOIN_ARMY))
        {
            rc.broadcast(Channels.beaverTasksTaken, tasksTaken + 1);
        }
    }
}
