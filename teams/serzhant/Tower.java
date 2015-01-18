package serzhant;

import battlecode.common.*;

/**
 * Tower class.
 * 
 * @author Amit Bachchan
 */
public class Tower
    extends AttackBuilding
{
    private int vulnChannel;


    public Tower(RobotController rc)
        throws GameActionException
    {
        super(rc);

        setVulnChannel();

        analyzeTower();
    }


    private void analyzeTower()
        throws GameActionException
    {
        int droneVulnerabilityScore = 0;
        int vulnerabilityScore = 100 + mLocation.distanceSquaredTo(allyHQ);
        MapLocation[] nearby =
            MapLocation.getAllMapLocationsWithinRadiusSq(
                mLocation,
                RobotType.TOWER.sensorRadiusSquared);
        for (MapLocation loc : nearby)
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

        RobotInfo[] allies =
            rc.senseNearbyRobots(RobotType.TOWER.sensorRadiusSquared, myTeam);
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

        droneVulnerabilityScore += vulnerabilityScore;

        rc.broadcast(vulnChannel + 1, vulnerabilityScore);
        rc.broadcast(vulnChannel + 2, droneVulnerabilityScore);
    }


    private void setVulnChannel()
    {
        for (int i = 0; i < allyTowers.length; i++)
        {
            if (allyTowers[i].equals(mLocation))
            {
                vulnChannel =
                    Channels.towerVulnerability
                        + Constants.CHANNELS_PER_TOWER_VULN * i;
                break;
            }
        }
    }
}
