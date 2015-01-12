package ridavoy;

import battlecode.common.*;

public class Molotok
    extends Proletariat
{
    private BeaverTask  task;
    private boolean     reached;
    private int         steps;
    private MapLocation myBuildLoc;


    public Molotok(RobotController rc)
        throws GameActionException
    {
        super(rc);
        this.setDestination(getLocation(Channel.rallyLoc));
        task = getNextTask();
        reached = false;
    }


    private BeaverTask getNextTask()
        throws GameActionException
    {
        int tasksTaken = rc.readBroadcast(Channel.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channel.beaverTask1 + tasksTaken);
        BeaverTask myTask = Constants.getTask(taskNum);

        return myTask;
    }


    @Override
    public void run()
        throws GameActionException
    {

        rc.broadcast(
            Channel.beaverCount,
            rc.readBroadcast(Channel.beaverCount) + 1);

        rc.setIndicatorString(1, "My task is: " + task);
        if (task == BeaverTask.JOIN_ARMY)
        {
            reached = false;
            this.setDestination(getLocation(Channel.rallyLoc));
            this.bug();
            task = this.getNextTask();
        }
        else if (task == BeaverTask.MINE)
        {
            reached = false;
            mine();
            task = this.getNextTask();
        }
        else
        {
            if (rc.isCoreReady())
            {
                if (!reached)
                {
                    MapLocation goal = getLocation(Channel.buildLoc);
                    this.setDestination(goal);
                    if (rc.getLocation().distanceSquaredTo(goal) <= 2)
                    {
                        reached = true;
                        myBuildLoc = goal;
                        this.setDestination(getLocation(Channel.rallyLoc));
                        steps = 0;
                    }
                }
                else
                {
                    if (steps >= 2)
                    {
                        if (build())
                        {
                            steps = 0;
                            task = getNextTask();
                            reached = false;
                            return;
                        }
                        return;
                    }
                    steps++;
                }
                bug();
            }
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
            int count = 0;
            Direction dir = facing;
            double distance =
                myBuildLoc.distanceSquaredTo(rc.getLocation().add(dir));
            while ((!rc.canBuild(dir, toBuild) || distance >= 15) && count < 8)
            {
                dir = dir.rotateRight();
                distance =
                    myBuildLoc.distanceSquaredTo(rc.getLocation().add(dir));
                count++;
            }
            if (count < 8)
            {
                incrementTask();
                rc.build(dir, toBuild);
                broadcastLocation(Channel.buildLoc, rc.getLocation().add(dir));
                return true;
            }
            else
            {
                this.setDestination(myBuildLoc);
                bug();
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
        int tasksTaken = rc.readBroadcast(Channel.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channel.beaverTask1 + tasksTaken);
        BeaverTask myTask = Constants.getTask(taskNum);
        if (!(myTask == BeaverTask.MINE || myTask == BeaverTask.JOIN_ARMY))
        {
            rc.broadcast(Channel.beaverTasksTaken, tasksTaken + 1);
        }
    }
}
