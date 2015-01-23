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
        // this.setDestination(allyHQ);
        lastRoundHealth = rc.getHealth();
        runningAway = true;
        this.setDestination(enemyHQ);
    }


    public void run()
        throws GameActionException
    {
        if (bug())
        {
            return;
        }
        if (runningAway && rc.getSupplyLevel() > 1000 && rc.getHealth() > 60)
        {
            runningAway = false;
        }
        if (runningAway)
        {
            bug();
        }
        else if (shouldRunAway())
        {
            this.setDestination(allyHQ);
            runningAway = true;
        }
        else
        {
            RobotInfo[] nearby =
                rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
            attack(nearby);
            if (rc.isCoreReady())
            {
                micro();
            }
        }
    }


    private boolean shouldRunAway()
    {
        double healthDifference = lastRoundHealth - rc.getHealth();
        lastRoundHealth = rc.getHealth();
        if (healthDifference > rc.getHealth() || lastRoundHealth < 30
            || rc.getSupplyLevel() < 150)
        {
            return true;
        }
        return false;
    }


    private void micro()
        throws GameActionException
    {
        MapLocation rally = getLocation(Channels.rallyLoc);
        this.setDestination(rally);
        if (rc.getFlashCooldown() == 0)
        {
            MapLocation loc = findFlashLoc();
            if (loc != null)
            {
                // rc.castFlash(loc);
                // return;
            }
        }
        bug();
    }


    private MapLocation findFlashLoc()
        throws GameActionException
    {
        Direction dir = rc.getLocation().directionTo(dest);
        Direction right = dir.rotateRight().rotateRight();
        Direction left = dir.rotateLeft().rotateLeft();
        if (!runningAway && rc.getLocation().distanceSquaredTo(dest) < 35)
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
