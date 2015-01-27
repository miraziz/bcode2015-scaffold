package basherbot;

import battlecode.common.*;

/**
 * A class containing a tower's location and vulnerability score.
 * 
 * @author Amit Bachchan
 */
public class TowerRank
    implements Comparable<TowerRank>
{
    MapLocation loc;
    int         score;


    public TowerRank(MapLocation loc, int score)
    {
        this.loc = loc;
        this.score = score;
    }


    public int compareTo(TowerRank o)
    {
        return score - o.score;
    }
}
