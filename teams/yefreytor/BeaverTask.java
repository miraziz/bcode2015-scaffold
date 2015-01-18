package yefreytor;

/**
 * Enum class for different molotok tasks.
 * 
 * @author Amit Bachchan
 */
public enum BeaverTask
{
    BUILD_BARRACKS(0),
    BUILD_MINERFACTORY(1),
    BUILD_TANKFACTORY(2),
    BUILD_SUPPLYDEPOT(3),
    BUILD_HELIPAD(4),
    BUILD_AEROSPACE(5),
    MINE(6),
    JOIN_ARMY(7);

    private int                 value;
    private static BeaverTask[] vals = BeaverTask.values();


    BeaverTask(int id)
    {
        value = id;
    }


    public int value()
    {
        return value;
    }


    public static BeaverTask getTask(int val)
    {
        return vals[val];
    }
}
