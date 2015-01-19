package serzhant;

import battlecode.common.*;

/**
 * Fighter (mobile) class.
 * 
 * @author Amit Bachchan
 */
public class Fighter
    extends Proletariat
{

    /**
     * Sets rally point.
     * 
     * @param rc
     * @throws GameActionException
     */
    public Fighter(RobotController rc)
        throws GameActionException
    {
        super(rc);

        setDestination(getLocation(Channels.rallyLoc));
    }


    /**
     * Attacks if possible, or bugs otherwise.
     */
    @Override
    public void run()
        throws GameActionException
    {
        super.run();

        // TODO Stop them from moving when they're in a clump to avoid wasting
// supply
        this.setDestination(getLocation(Channels.rallyLoc));
        rc.setIndicatorString(0, "Traveling to: "
            + getLocation(Channels.rallyLoc));
        RobotInfo[] nearby =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
        if (!attack())
        {
            micro(nearby);
        }
    }


    private void micro(RobotInfo[] nearby)
        throws GameActionException
    {
        bug();
    }


    private boolean attack(RobotInfo[] nearby)
        throws GameActionException
    {
        if (!rc.isWeaponReady())
        {
            return false;
        }
        int highestPriority = 0;
        RobotInfo toAttack = null;
        for (RobotInfo r : nearby)
        {
            if (rc.canAttackLocation(r.location))
            {
                int priority = getPriority(r.type);
                if (priority > highestPriority)
                {
                    toAttack = r;
                    highestPriority = priority;
                }
                else if (priority != 0 && priority == highestPriority
                    && r.health < toAttack.health)
                {
                    toAttack = r;
                }
            }
        }
        if (highestPriority == 0)
        {
            return false;
        }
        rc.attackLocation(toAttack.location);
        return true;
    }


    private int getPriority(RobotType type)
    {
        int priority = 0;
        if (type == RobotType.TOWER || type == RobotType.HQ)
        {
            priority = 4;
        }
        else if (isAttackingUnit(type))
        {
            priority = 3;
        }
        else if (type == RobotType.MINER || type == RobotType.BEAVER)
        {
            priority = 2;
        }
        else if (isProductionBuilding(type))
        {
            priority = 1;
        }
        return priority;
    }
}
