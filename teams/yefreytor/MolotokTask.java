package yefreytor;

/**
 * Enum class for different molotok tasks.
 * 
 * @author Amit Bachchan
 */
public enum MolotokTask
{
    BUILD_BARRACKS(0),
    BUILD_MINERFACTORY(1),
    BUILD_TANKFACTORY(2),
    BUILD_SUPPLYDEPOT(3),
    BUILD_HELIPAD(4),
    MINE(5),
    JOIN_ARMY(6);

    private int                 value;
    private static MolotokTask[] vals = MolotokTask.values();


    MolotokTask(int id)
    {
        value = id;
    }


    public int value()
    {
        return value;
    }


    public static MolotokTask getTask(int val)
    {
        return vals[val];
    }
}
