package ridavoy;

import battlecode.common.*;

public class Drone
    extends Proletariat
{

    public Drone(RobotController rc)
        throws GameActionException
    {
        super(rc);
        // TODO Auto-generated constructor stub
    }


    public void run()
        throws GameActionException
    {
        if (rc.isCoreReady())
        {
            if (attack())
            {
                return;
            }
            Direction dir = rc.getLocation().directionTo(enemyHQ);
            Direction right = dir;
            Direction left = dir;
            while (!canMove(dir))
            {
                if (dir == right)
                {
                    left = left.rotateLeft();
                    dir = left;
                }
                else
                {
                    right = right.rotateRight();
                    dir = right;
                }
            }
            if (dir == null)
            {
                return;
            }
            this.move(dir);
        }
    }


    public boolean canMove(Direction dir)
        throws GameActionException
    {
        MapLocation next = rc.getLocation().add(dir);
        if (next.distanceSquaredTo(enemyHQ) < RobotType.HQ.attackRadiusSquared)
        {

        }
        for (int i = 0; i < enemyTowers.length; i++)
        {
            if (next.distanceSquaredTo(enemyTowers[i]) < RobotType.TOWER.attackRadiusSquared)
            {
                return false;
            }
        }
        return true;
    }
}
