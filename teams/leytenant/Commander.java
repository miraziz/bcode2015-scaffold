package leytenant;

import battlecode.common.*;

public class Commander
    extends Fighter
{
    private double  lastRoundHealth;
    private boolean runningAway;


    public Commander(RobotController rc)
        throws GameActionException
    {
        super(rc);
        lastRoundHealth = rc.getHealth();
        runningAway = true;
        this.setDestination(enemyHQ);
    }


    public void run()
        throws GameActionException
    {
        super.run();
        if (bug())
        {
            return;
        }
        else
        {
            return;
        }
// RobotInfo[] nearby =
// rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
// attack(nearby);
// if (runningAway && rc.getSupplyLevel() > 1000 && rc.getHealth() > 60)
// {
// runningAway = false;
// }
// if (runningAway)
// {
// this.setDestination(allyHQ);
// }
// else if (shouldRunAway())
// {
// this.setDestination(allyHQ);
// runningAway = true;
// }
// if (!runningAway)
// {
// this.setDestination(getLocation(Channels.rallyLoc));
// }
// if (rc.isCoreReady())
// {
// micro(nearby);
// }
    }


    private boolean shouldRunAway()
    {
        double healthDifference = lastRoundHealth - rc.getHealth();
        lastRoundHealth = rc.getHealth();
        if (healthDifference > rc.getHealth() || lastRoundHealth < 30
            || rc.getSupplyLevel() < 1000)
        {
            return true;
        }
        return false;
    }


    private void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        int avgX = 0;
        int avgY = 0;
        int enemyCount = 0;
        double enemyPower = 0;
        for (int i = 0; i < nearby.length; i++)
        {
            if (nearby[i].team == enemyTeam)
            {
                avgX += nearby[i].location.x;
                avgY += nearby[i].location.y;
                if (rc.getLocation().distanceSquaredTo(nearby[i].location) <= nearby[i].type.attackRadiusSquared)
                {
                    enemyPower += getDPR(nearby[i].type);
                }
                enemyCount++;
            }
        }
        if (enemyCount != 0 && enemyPower > Constants.COMMANDER_DPR)
        {
            avgX /= enemyCount;
            avgY /= enemyCount;
            Direction away = new MapLocation(avgX, avgY).directionTo(mLocation);
            if (enemyPower > Constants.COMMANDER_DPR)
            {
                this.setDestination(rc.getLocation().add(away, 4));
            }
        }
        if (rc.getFlashCooldown() == 0)
        {
            MapLocation loc = findFlashLoc();
            if (loc != null)
            {
                rc.castFlash(loc);
                return;
            }
        }
        bug();
    }


    private double getDPR(RobotType type)
    {
        return type.attackPower / type.attackDelay;
    }


    private MapLocation findFlashLoc()
        throws GameActionException
    {
        Direction dir = rc.getLocation().directionTo(dest);
        Direction right = dir.rotateRight().rotateRight();
        Direction left = dir.rotateLeft().rotateLeft();
        if (!runningAway && rc.getLocation().distanceSquaredTo(dest) < 25)
        {
            return null;
        }
        if (!dir.isDiagonal())
        {
            MapLocation target = rc.getLocation().add(dir, 3);
            for (int i = 0; i < 6; i++)
            {
                if (canFlash(target))
                {
                    return target;
                }
                if (i % 3 == 1)
                {
                    target.add(left);
                }
                else if (i % 3 == 2)
                {
                    target.add(right, 2);
                }
                if (i == 3)
                {
                    target = rc.getLocation().add(dir, 2);
                }
            }
        }
        else
        {
            MapLocation target = rc.getLocation().add(dir, 2).add(left);

            if (canFlash(target))
            {
                return target;
            }
            target = target.add(right, 2);
            if (canFlash(target))
            {
                return target;
            }
            target = target.add(left);
            if (canFlash(target))
            {
                return target;
            }
            target = rc.getLocation().add(dir).add(left);
            if (canFlash(target))
            {
                return target;
            }
            target = target.add(right, 2);
            if (canFlash(target))
            {
                return target;
            }
        }
        return null;
    }


    private boolean canFlash(MapLocation loc)
        throws GameActionException
    {
        return rc.senseTerrainTile(loc) == TerrainTile.NORMAL
            && !rc.isLocationOccupied(loc)
            && rc.getLocation().distanceSquaredTo(loc) <= 10
            && !inEnemyTowerRange(loc);
    }
}
