package id.neotica.holomarket.utils;

/**
 * Maps an Android SDK level to its {@link AndroidVersion}.
 */
public final class AndroidVersionMapper {

    private static final AndroidVersion[] BY_SDK = new AndroidVersion[37];

    static {
        BY_SDK[1] = new AndroidVersion(1, "1.0", null);
        BY_SDK[2] = new AndroidVersion(2, "1.1", null);
        BY_SDK[3] = new AndroidVersion(3, "1.5", "Cupcake");
        BY_SDK[4] = new AndroidVersion(4, "1.6", "Donut");
        BY_SDK[5] = new AndroidVersion(5, "2.0", "Eclair");
        BY_SDK[6] = new AndroidVersion(6, "2.0.1", "Eclair");
        BY_SDK[7] = new AndroidVersion(7, "2.1", "Eclair");
        BY_SDK[8] = new AndroidVersion(8, "2.2", "Froyo");
        BY_SDK[9] = new AndroidVersion(9, "2.3", "Gingerbread");
        BY_SDK[10] = new AndroidVersion(10, "2.3.3", "Gingerbread");
        BY_SDK[11] = new AndroidVersion(11, "3.0", "Honeycomb");
        BY_SDK[12] = new AndroidVersion(12, "3.1", "Honeycomb");
        BY_SDK[13] = new AndroidVersion(13, "3.2", "Honeycomb");
        BY_SDK[14] = new AndroidVersion(14, "4.0", "Ice Cream Sandwich");
        BY_SDK[15] = new AndroidVersion(15, "4.0.3", "Ice Cream Sandwich");
        BY_SDK[16] = new AndroidVersion(16, "4.1", "Jelly Bean");
        BY_SDK[17] = new AndroidVersion(17, "4.2", "Jelly Bean");
        BY_SDK[18] = new AndroidVersion(18, "4.3", "Jelly Bean");
        BY_SDK[19] = new AndroidVersion(19, "4.4", "KitKat");
        BY_SDK[20] = new AndroidVersion(20, "4.4W", "KitKat");
        BY_SDK[21] = new AndroidVersion(21, "5.0", "Lollipop");
        BY_SDK[22] = new AndroidVersion(22, "5.1", "Lollipop");
        BY_SDK[23] = new AndroidVersion(23, "6.0", "Marshmallow");
        BY_SDK[24] = new AndroidVersion(24, "7.0", "Nougat");
        BY_SDK[25] = new AndroidVersion(25, "7.1", "Nougat");
        BY_SDK[26] = new AndroidVersion(26, "8.0", "Oreo");
        BY_SDK[27] = new AndroidVersion(27, "8.1", "Oreo");
        BY_SDK[28] = new AndroidVersion(28, "9", "Pie");
        BY_SDK[29] = new AndroidVersion(29, "10", null);
        BY_SDK[30] = new AndroidVersion(30, "11", null);
        BY_SDK[31] = new AndroidVersion(31, "12", null);
        BY_SDK[32] = new AndroidVersion(32, "12L", null);
        BY_SDK[33] = new AndroidVersion(33, "13", null);
        BY_SDK[34] = new AndroidVersion(34, "14", null);
        BY_SDK[35] = new AndroidVersion(35, "15", null);
        BY_SDK[36] = new AndroidVersion(36, "16", null);
    }

    private AndroidVersionMapper() { }

    /**
     * @return the matching {@link AndroidVersion}, or an "Unknown" placeholder for unmapped levels.
     */
    public static AndroidVersion fromSdkLevel(int sdkLevel) {
        if (sdkLevel >= 0 && sdkLevel < BY_SDK.length && BY_SDK[sdkLevel] != null) {
            return BY_SDK[sdkLevel];
        }
        return new AndroidVersion(sdkLevel, null, null);
    }
}