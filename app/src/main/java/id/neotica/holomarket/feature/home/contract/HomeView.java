package id.neotica.holomarket.feature.home.contract;

import org.json.JSONObject;

import java.util.List;

import id.neotica.holomarket.feature.home.domain.AppTopic;

/**
 * View contract for the home screen, implemented by the home Activity.
 * {@code HomePresenter} drives all rendering through this interface.
 */
public interface HomeView {

    void showLogin();

    void showSettings();

    void renderSections(List<AppTopic> topics);

    void renderFeatured(List<JSONObject> items);

    void renderOrganizerSection(String title, List<JSONObject> items);

    void showError(String message);
}