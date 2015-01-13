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
    private int         steps;
    private MapLocation myBuildLoc;


    // TODO If a building is destroyed, what happens?

    public Molotok(RobotController rc)
        throws GameActionException
    {
        super(rc);

        this.setDestination(getLocation(Channels.rallyLoc));
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
            mine();
            task = this.getNextTask();
        }
        else
        {
            if (rc.isCoreReady())
            {
                if (!reached)
                {
                    MapLocation goal = getLocation(Channels.buildLoc);
                    this.setDestination(goal);
                    if (mLocation.distanceSquaredTo(goal) <= 2)
                    {
                        reached = true;
                        myBuildLoc = goal;
                        this.setDestination(getLocation(Channels.rallyLoc));
                        steps = 0;
                    }
                }
                else
                {
                    if (steps >= 1)
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

            if (mLocation == null)
            {
                System.out.println("My location is null");
            }
            if (dir == null)
            {
                System.out.println("Direction is null");
            }
            if (myBuildLoc == null)
            {
                System.out.println("My build location is null");
            }

            double distance = myBuildLoc.distanceSquaredTo(mLocation.add(dir));
            while ((!rc.canBuild(dir, toBuild) || distance >= 15) && count < 8)
            {
                dir = dir.rotateRight();
                distance = myBuildLoc.distanceSquaredTo(mLocation.add(dir));
                count++;
            }
            if (count < 8)
            {
                incrementTask();
                rc.build(dir, toBuild);
                broadcastLocation(Channels.buildLoc, mLocation.add(dir));
                broadcastLocation(Channels.buildPath, rc.getLocation().add(dir));
                rc.broadcast(
                    rc.readBroadcast(Channels.buildPathCount),
                    Channels.buildPathCount + 1);
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
        int tasksTaken = rc.readBroadcast(Channels.beaverTasksTaken);
        int taskNum = rc.readBroadcast(Channels.beaverTask1 + tasksTaken);
        BeaverTask myTask = BeaverTask.getTask(taskNum);
        if (!(myTask == BeaverTask.MINE || myTask == BeaverTask.JOIN_ARMY))
        {
            rc.broadcast(Channels.beaverTasksTaken, tasksTaken + 1);
        }
    }
}
