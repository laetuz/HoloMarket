package id.neotica.holomarket.feature.home.domain;

/**
 * A row on the home screen: either a category topic or the "My Apps" entry.
 */
public class HomeSection {

    public enum Type {
        TOPIC,
        MY_APPS
    }

    public final Type type;
    public final String title;
    public final String value;

    public HomeSection(Type type, String title, String value) {
        this.type = type;
        this.title = title;
        this.value = value;
    }

    public static HomeSection topic(String title, String value) {
        return new HomeSection(Type.TOPIC, title, value);
    }

    public static HomeSection myApps(String title) {
        return new HomeSection(Type.MY_APPS, title, null);
    }
}