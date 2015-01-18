package yefreytor;

import battlecode.common.*;

public class SupplyDrone
    extends Proletariat
{

    public SupplyDrone(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.droneCount,
            rc.readBroadcast(Channels.droneCount) + 1);
        if (rc.getSupplyLevel() < 100)
        {
            rc.setIndicatorString(1, "Going back to HQ");
            if (rc.isCoreReady())
            {
                moveTowards(allyHQ);
            }
        }
        else
        {
            MapLocation loc = getLocation(Channels.supplyLoc);
            rc.setIndicatorString(1, "Supplying towards: " + loc);
            if (rc.isCoreReady())
            {
                moveTowards(getLocation(Channels.supplyLoc));
            }
        }

    }


    public void transferSupplies()
        throws GameActionException
    {
        RobotInfo[] nearby =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);
        RobotInfo[] allNearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam);
        for (RobotInfo r : nearby)
        {
            if (Clock.getBytecodesLeft() < 550)
            {
                return;
            }
            if (this.isSupplyingUnit(r.type) && r.supplyLevel < 200
                && rc.getSupplyLevel() > 100)
            {
                double supplyTransfer =
                    Math.min(rc.getSupplyLevel() - 50, 2000 * allNearby.length);
                rc.transferSupplies((int)supplyTransfer, r.location);
            }
        }

        rc.broadcast(Channels.supplyPriority, 0);
        rc.broadcast(Channels.supplyDistance, 0);
    }


    private void moveTowards(MapLocation loc)
        throws GameActionException
    {
        Direction dir = rc.getLocation().directionTo(loc);
        Direction left = dir;
        Direction right = dir;
        int count = 0;
        RobotInfo[] nearbyEnemies =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
        while (count < 8
            && (!safeFromTowers(dir) || !safeFromEnemies(dir, nearbyEnemies) || !rc
                .canMove(dir)))
        {
            rc.setIndicatorString(2, "Turning: " + dir);
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
        if (count >= 8)
        {
            return;
        }
        if (rc.canMove(dir))
        {
            rc.move(dir);
        }
    }


    private boolean safeFromEnemies(Direction dir, RobotInfo[] nearbyEnemies)
    {
        MapLocation next = rc.getLocation().add(dir);
        for (RobotInfo r : nearbyEnemies)
        {
            if (r.location.distanceSquaredTo(next) < r.type.attackRadiusSquared)
            {
                return false;
            }
        }
        return true;
    }


    private boolean safeFromTowers(Direction dir)
    {
        MapLocation moveLoc = rc.getLocation().add(dir);
        int distance = moveLoc.distanceSquaredTo(enemyHQ);
        int hqAttackRange = 37;
        if (enemyTowers.length < 2)
        {
            hqAttackRange = 26;
        }
        if (distance <= hqAttackRange)
        {
            return false;
        }
        for (MapLocation r : enemyTowers)
        {
            if (r.distanceSquaredTo(moveLoc) <= RobotType.TOWER.attackRadiusSquared)
            {
                return false;
            }
        }

        return true;
    }
}
