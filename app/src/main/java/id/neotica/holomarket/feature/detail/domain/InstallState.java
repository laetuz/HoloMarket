package id.neotica.holomarket.feature.detail.domain;

/**
 * Install state of the app being viewed, relative to the latest published version.
 */
public enum InstallState {
    DOWNLOAD,
    UPDATE,
    OPEN
}