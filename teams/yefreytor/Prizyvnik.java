package yefreytor;

import java.util.LinkedList;
import battlecode.common.*;

/**
 * Soldier class.
 * 
 * @author Amit Bachchan
 */
public class Prizyvnik
    extends Boyets
{

    private Direction towardsEnemy;


    public Prizyvnik(RobotController rc)
        throws GameActionException
    {
        super(rc);
    }


    public void run()
        throws GameActionException
    {
        rc.broadcast(
            Channels.soldierCount,
            rc.readBroadcast(Channels.soldierCount) + 1);
        Decision decision = makeDecision();
        if (!rc.isCoreReady())
        {
            return;
        }
        if (decision == Decision.RUN)
        {
            Direction dir = towardsEnemy.opposite();
            if (rc.canMove(dir))
            {
                rc.move(dir);
            }
            else if (rc.canMove(dir.rotateLeft()))
            {
                rc.move(dir.rotateLeft());
            }
            else if (rc.canMove(dir.rotateRight()))
            {
                rc.move(dir.rotateRight());
            }
            else
            {
                attack();
            }
        }
        else if (decision == Decision.ATTACK)
        {
            if (!attack())
            {
                this.setDestination(getLocation(Channels.rallyLoc));
                bug();
            }
        }
        else
        {
            attack();
        }
    }


    private Decision makeDecision()
    {
        LinkedList<RobotInfo> nearby = new LinkedList<RobotInfo>();
        RobotInfo[] nearbyArr =
            rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        for (RobotInfo r : nearbyArr)
        {
            nearby.add(r);
        }
        Decision decision = Decision.ATTACK;
        int enemyX = 0;
        int enemyY = 0;
        int enemyCount = 0;
        int myTeamsHealth = 0;
        int enemyTeamsHealth = 0;
        int len = nearby.size();
        for (int i = 0; i < len; i++)
        {
            RobotInfo r = nearby.pollFirst();
            if (r.team == rc.getTeam())
            {
                nearby.push(r);
                continue;
            }
            if (isAttackingUnit(r.type))
            {
                enemyX += r.location.x;
                enemyY += r.location.y;
                enemyCount++;
                enemyTeamsHealth += r.health;
            }
        }
        if (enemyCount <= 0)
        {
            return Decision.ATTACK;
        }
        enemyX /= enemyCount;
        enemyY /= enemyCount;
        MapLocation enemyLoc = new MapLocation(enemyX, enemyY);
        towardsEnemy = rc.getLocation().directionTo(enemyLoc);
        len = nearby.size();
        for (int i = 0; i < len; i++)
        {
            RobotInfo r = nearby.pollFirst();
            if (isAttackingUnit(r.type)
                && inLine(towardsEnemy, rc.getLocation()
                    .directionTo(r.location)))
            {
                myTeamsHealth += r.health;
            }
        }
        if (myTeamsHealth > enemyTeamsHealth * 2)
        {
            decision = Decision.ATTACK;
        }
        else if (myTeamsHealth < enemyTeamsHealth * 2)
        {
            decision = Decision.RUN;
        }
        else
        {
            decision = Decision.RELAX;
        }
        return decision;
    }


    private boolean inLine(Direction towardsEnemy, Direction towardsFriend)
    {
        return towardsFriend == towardsEnemy
            || towardsFriend.rotateLeft() == towardsEnemy
            || towardsFriend.rotateLeft().rotateLeft() == towardsEnemy
            || towardsFriend.rotateRight().rotateRight() == towardsEnemy
            || towardsFriend.rotateRight() == towardsEnemy;
    }
}
