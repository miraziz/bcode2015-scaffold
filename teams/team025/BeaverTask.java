package team025;

public enum BeaverTask
{
    BUILD_BARRACKS(0),
    BUILD_MINERFACTORY(1),
    BUILD_TANKFACTORY(2),
    BUILD_SUPPLYDEPOT(3),
    BUILD_HELIPAD(4),
    MINE(5),
    JOIN_ARMY(6);

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
