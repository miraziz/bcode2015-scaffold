package ridavoy;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public abstract class Atakuyushchiy
    extends Zdaniya
{

    public Atakuyushchiy(RobotController rc)
        throws GameActionException
    {
        super(rc);
        broadcastScores();
    }


    public void run()
        throws GameActionException
    {
        this.attack();
    }


    void broadcastScores()
        throws GameActionException
    {
        int droneVulnerabilityScore = 0;
        int vulnerabilityScore = 150;
        int farmScore = 0;
        MapLocation[] nearby =
            MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 35);
        for (MapLocation loc : nearby)
        {
            if (rc.canSenseLocation(loc))
            {
                TerrainTile tile = rc.senseTerrainTile(loc);
                if (tile == TerrainTile.NORMAL)
                {
                    farmScore += rc.senseOre(loc);
                }
                else if (tile == TerrainTile.OFF_MAP)
                {
                    droneVulnerabilityScore += 12;
                    vulnerabilityScore -= 8;
                }
                else if (tile == TerrainTile.VOID)
                {
                    droneVulnerabilityScore += 6;
                    vulnerabilityScore -= 4;
                }
            }
            RobotInfo[] allies = rc.senseNearbyRobots(25, myTeam);
            for (RobotInfo ally : allies)
            {
                if (ally.type == RobotType.HQ)
                {
                    vulnerabilityScore -= 25;
                }
                else if (ally.type == RobotType.TOWER)
                {
                    vulnerabilityScore -= 15;
                }
            }
        }
        if (rc.getType() == RobotType.HQ)
        {
            vulnerabilityScore -= 25;
        }
        droneVulnerabilityScore += vulnerabilityScore;
    }
}
