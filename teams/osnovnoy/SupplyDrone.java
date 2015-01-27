package osnovnoy;

import battlecode.common.*;

public class SupplyDrone
    extends Proletariat
{

    private boolean     returningToBase;
    private MapLocation supplyLoc;
    private int         supplyId;
    int                 steps;
    private int         dist;
    private boolean     atBase;

    boolean             supplyTowardsDest;


    public SupplyDrone(RobotController rc)
        throws GameActionException
    {
        super(rc);
        returningToBase = true;
        atBase = true;
        mTypeChannel = Channels.droneCount;
        steps = 0;
        dist = 0;
        enemyHQ = rc.senseEnemyHQLocation();
        if (rc.readBroadcast(Channels.minerCount) % 2 == 0)
        {
            supplyTowardsDest = false;
        }
        else
        {
            supplyTowardsDest = false;
        }
        rc.setIndicatorString(0, "Supplying towards destination: "
            + supplyTowardsDest);
    }


    public void run()
        throws GameActionException
    {
        if (!rc.isCoreReady())
        {
            enemyTowers = rc.senseEnemyTowerLocations();
        }
        rc.broadcast(
            Channels.droneCount,
            rc.readBroadcast(Channels.droneCount) + 1);
        if (rc.getLocation().distanceSquaredTo(allyHQ) <= 15)
        {
            atBase = true;
        }
        else
        {
            atBase = false;
        }
        if (steps % 20 == 0)
        {
            returningToBase = false;
            supplyLoc = getLocation(Channels.supplyLoc);
            supplyId = rc.readBroadcast(Channels.supplyId);
            dist = rc.getLocation().distanceSquaredTo(supplyLoc);
            steps = 0;
        }
        if (!returningToBase && (steps * 1.5) > dist)
        {
            returningToBase = true;
            this.setDestination(allyHQ);
        }
        else if (!returningToBase
            && rc.getLocation().distanceSquaredTo(supplyLoc) <= 4)
        {
            returningToBase = true;
            this.setDestination(allyHQ);
        }
        else if (returningToBase && rc.getSupplyLevel() > 10000)
        {
            returningToBase = false;
            supplyLoc = getLocation(Channels.supplyLoc);
            supplyId = rc.readBroadcast(Channels.supplyId);
            dist = rc.getLocation().distanceSquaredTo(supplyLoc);
            steps = 0;
        }
        else if (returningToBase || rc.getSupplyLevel() < 500)
        {
            this.setDestination(allyHQ);
            returningToBase = true;
            rc.setIndicatorString(1, "Going back to HQ");
        }
        else
        {
            if (rc.canSenseRobot(supplyId))
            {
                supplyLoc = rc.senseRobot(supplyId).location;
            }
            this.setDestination(supplyLoc);
            rc.setIndicatorString(1, "Supplying towards: " + supplyLoc);
        }
        if (rc.isCoreReady())
        {
            droneMove(rc.getLocation().directionTo(dest));
        }
    }


    private void droneMove(Direction dir)
        throws GameActionException
    {
        if (!returningToBase)
        {
            steps++;
        }
        Direction safe = this.findSafeDir(dir);
        if (safe != null)
        {
            rc.move(safe);
        }
    }


    public void transferSupplies()
        throws GameActionException
    {
        rc.setIndicatorString(2, "Returning to base: " + returningToBase);
        if (atBase || !returningToBase || rc.getSupplyLevel() < 500)
        {
            return;
        }
        double ratio = .8;
        if (supplyTowardsDest)
        {
            ratio = .2;
        }
        RobotInfo[] nearby =
            rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                myTeam);
        RobotInfo toGive = null;
        for (RobotInfo r : nearby)
        {
            if (isSupplyingUnit(r.type) && r.supplyLevel < rc.getSupplyLevel())
            {
                if (toGive == null)
                {
                    toGive = r;
                }
                else if (r.supplyLevel < toGive.supplyLevel)
                {
                    toGive = r;
                }
            }
        }
        if (toGive == null || Clock.getBytecodesLeft() < 600)
        {
            return;
        }
        if (toGive.type == RobotType.MINER)
        {
            ratio = .1;
        }
        double supplyTransferAmount = (rc.getSupplyLevel() * ratio);
        if (rc.getSupplyLevel() - supplyTransferAmount < 500)
        {
            supplyTransferAmount = (rc.getSupplyLevel() - 500);
        }
        rc.transferSupplies((int)supplyTransferAmount, toGive.location);

// if (rc.getSupplyLevel() < 100)
// {
// return;
// }
// RobotInfo[] nearby =
// rc.senseNearbyRobots(
// GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
// myTeam);
// for (RobotInfo r : nearby)
// {
// if (Clock.getBytecodesLeft() < 600)
// {
// return;
// }
// if (this.isSupplyingUnit(r.type) && r.supplyLevel < 200
// && rc.getSupplyLevel() > 100)
// {
// double supplyTransfer =
// Math.min(
// rc.getSupplyLevel() - 50,
// (getSupplyPriority(r.type) * 1000) - r.supplyLevel);
// if (supplyTransfer > 0)
// {
// rc.transferSupplies((int)supplyTransfer, r.location);
// }
// }
// }
// rc.broadcast(Channels.supplyPriority, 0);
// rc.broadcast(Channels.supplyDistance, 0);
    }
}
