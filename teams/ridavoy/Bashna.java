package ridavoy;

import battlecode.common.*;

/**
 * Tower class.
 * 
 * @author Miraziz
 */
public class Bashna
    extends Atakuyushchiy
{

    public Bashna(RobotController rc)
        throws GameActionException
    {
        super(rc);
        // broadcastScores();
    }


    protected void broadcastScores()
        throws GameActionException
    {
        int droneVulnerabilityScore = 0;
        int vulnerabilityScore = 150;
        MapLocation[] nearby =
            MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 35);
        for (MapLocation loc : nearby)
        {
            if (rc.canSenseLocation(loc))
            {
                TerrainTile tile = rc.senseTerrainTile(loc);

                if (tile == TerrainTile.OFF_MAP)
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
                    vulnerabilityScore -= 35;
                }
                else if (ally.type == RobotType.TOWER)
                {
                    vulnerabilityScore -= 25;
                }
            }
        }
        droneVulnerabilityScore += vulnerabilityScore;

    }
}
