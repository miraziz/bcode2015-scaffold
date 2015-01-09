package minerdraft;

import java.util.HashSet;
import battlecode.common.*;

// abstraction for all buildings
abstract class BuildingAbstraction
    extends BaseBot
{

    public BuildingAbstraction(RobotController myRc)
        throws GameActionException
    {
        super(myRc);
        transferSupplies();
    }


    boolean BuildUnit(RobotType type, int channel, int limit)
        throws GameActionException
    {
        int count = rc.readBroadcast(channel);
        if (count < limit && spawnUnit(type))
        {
            rc.broadcast(channel, count + 1);
            return true;
        }
        return false;
    }
}




// abstraction for buildings that can attack ( tower and hq )
abstract class AttackingBuilding
    extends BuildingAbstraction
{

    public AttackingBuilding(RobotController myRc)
        throws GameActionException
    {
        super(myRc);
        HashSet<MapLocation> visited = new HashSet<MapLocation>();
        int constant = 5;
        int farmScore =
            traverseFarm(rc.getLocation(), visited)
                + (rc.getLocation().distanceSquaredTo(enemyHQ) / constant);
        rc.setIndicatorString(1, "My farm score is: " + farmScore);
        int topScore = rc.readBroadcast(Constants.BestFarmScore);
        if (topScore <= 0 || farmScore > topScore)
        {
            rc.broadcast(Constants.BestFarmScore, topScore);
            rc.broadcast(Constants.BestFarmX, rc.getLocation().x);
            rc.broadcast(Constants.BestFarmY, rc.getLocation().y);
        }
    }


    public void runBot()
        throws GameActionException
    {
        attack();
    }


    private int traverseFarm(MapLocation cur, HashSet<MapLocation> visited)
    {
        if (visited.contains(cur)
            || rc.senseTerrainTile(cur) != TerrainTile.NORMAL
            || cur.distanceSquaredTo(rc.getLocation()) > 35)
        {
            return 0;
        }
        else
        {
            visited.add(cur);
            Direction dir = Direction.EAST;
            int total = (int)rc.senseOre(cur);
            for (int i = 0; i < 8; i++)
            {
                total += traverseFarm(cur.add(dir), visited);
                dir = dir.rotateRight();
            }
            return total;
        }
    }
}




// abstraction for buildings that solely build units
abstract class ProductionBuilding
    extends BuildingAbstraction
{

    public ProductionBuilding(RobotController myRc)
        throws GameActionException
    {
        super(myRc);
    }
}
