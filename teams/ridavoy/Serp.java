package ridavoy;

import battlecode.common.*;

public class Serp
    extends Proletariat
{
    boolean reachedFarm;


    public Serp(RobotController rc)
        throws GameActionException
    {
        super(rc);
        reachedFarm = false;
    }


    @Override
    public void run()
        throws GameActionException
    {

        // if not in any danger
        if (rc.isCoreReady())
        {
            if (reachedFarm)
            {
                if (rc.canMine() && rc.senseOre(rc.getLocation()) > 0)
                {
                    rc.mine();
                }
                else
                {
                    Direction bestDir = Direction.NORTH;
                    double bestScore = 0;
                    Direction dir = Direction.NORTH;
                    for (int i = 0; i < 8; i++)
                    {
                        if (rc.canMove(dir))
                        {
                            double oreCount =
                                rc.senseOre(rc.getLocation().add(dir));
                            if (oreCount > bestScore)
                            {
                                bestDir = dir;
                                bestScore = oreCount;
                            }
                        }
                        dir = dir.rotateRight();
                    }
                    if (bestScore == 0)
                    {
                        // set destination to farm spot.
                        bug();
                    }
                    else
                    {
                        move(bestDir);
                    }
                }
            }
            else
            {
                if (rc.canMine() && rc.senseOre(rc.getLocation()) > 1)
                {
                    rc.mine();
                }
                else
                {
                    // set destination to a farm spot.
                    bug();
                }
            }
        }
    }
}
