package supremecommander;

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
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        attack(nearby);
        if (!rc.isCoreReady())
        {
            enemyTowers = rc.senseEnemyTowerLocations();
            return;
        }
        if (runningAway && rc.getSupplyLevel() > 1000 && rc.getHealth() > 100)
        {
            runningAway = false;
        }
        if (runningAway || shouldRunAway())
        {
            this.setDestination(allyHQ);
            runningAway = true;
        }
        if (!runningAway)
        {
            this.setDestination(getLocation(Channels.rallyLoc));
        }
        micro(nearby);

    }


    private boolean shouldRunAway()
    {
        double healthDifference = lastRoundHealth - rc.getHealth();
        lastRoundHealth = rc.getHealth();
        if (healthDifference > rc.getHealth() || lastRoundHealth < 100
            || rc.getSupplyLevel() < 100)
        {
            return true;
        }
        return false;
    }


    protected void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        if (runningAway)
        {
            travel(runningAway);
            return;
        }
        int avgX = 0;
        int avgY = 0;
        int enemyCount = 0;
        double enemyAttackDamage = 0;
        boolean inEnemyAttackRange = false;
        boolean inDangerousArea = false;
        boolean canAttackSomeone = false;
        MapLocation lowestHealthLoc = null;
        double lowestHealth = Double.MAX_VALUE;
        for (int i = 0; i < nearby.length; i++)
        {
            if (nearby[i].team == enemyTeam && nearby[i].type.canAttack()
                && !nearby[i].type.isBuilding)
            {
                avgX += nearby[i].location.x;
                avgY += nearby[i].location.y;
                int dist =
                    rc.getLocation().distanceSquaredTo(nearby[i].location);
                if (dist <= rc.getType().attackRadiusSquared)
                {
                    canAttackSomeone = true;
                }
                if (dist <= nearby[i].type.attackRadiusSquared)
                {
                    enemyAttackDamage += getDPR(nearby[i].type);
                    inEnemyAttackRange = true;
                }
                if (nearby[i].health < lowestHealth)
                {
                    lowestHealth = nearby[i].health;
                    lowestHealthLoc = nearby[i].location;
                }
                enemyCount++;
                inDangerousArea = true;
            }
        }
        if (enemyCount != 0)
        {
            avgX /= enemyCount;
            avgY /= enemyCount;
            if (inEnemyAttackRange)
            {
                this.setDestination(new MapLocation(avgX, avgY));
            }
            else
            {
                this.setDestination(lowestHealthLoc);
            }
            if (enemyAttackDamage > rc.getHealth() + 15)
            {
                runningAway = true;
                this.setDestination(allyHQ);
            }
        }
        if (runningAway && enemyCount == 0 && rc.getSupplyLevel() > 1000)
        {
            return;
        }
        if (runningAway || !inDangerousArea)
        {
            travel(true);
        }
        else if (!inEnemyAttackRange)
        {
            travel(false);
        }
        else if (!canAttackSomeone)
        {
            travel(false);
        }

    }


    private void travel(boolean withFlash)
        throws GameActionException
    {
        if (rc.getFlashCooldown() == 0 && withFlash)
        {
            MapLocation loc = findFlashLoc();
            if (loc != null)
            {
                rc.castFlash(loc);
                return;
            }
        }
        bugWithCounter();
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
