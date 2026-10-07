package id.neotica.holomarket.utils;

/**
 * An Android release: its SDK level, version string, and optional codename.
 */
public final class AndroidVersion {

    public final int sdkLevel;
    public final String version;
    public final String codename;

    public AndroidVersion(int sdkLevel, String version, String codename) {
        this.sdkLevel = sdkLevel;
        this.version = version;
        this.codename = codename;
    }

    /**
     * @return e.g. "Eclair (2.1)", just "10" when there is no codename, or "Unknown".
     */
    public String displayName() {
        if (version == null) {
            return "Unknown";
        }
        if (codename == null || codename.length() == 0) {
            return version;
        }
        return codename + " (" + version + ")";
    }

    @Override
    public String toString() {
        return displayName();
    }
}