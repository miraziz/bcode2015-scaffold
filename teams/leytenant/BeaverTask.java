package leytenant;

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
    BUILD_TECHINSTITUTE(6),
    BUILD_TRAININGFIELD(7),
    MINE(8),
    JOIN_ARMY(9);

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
