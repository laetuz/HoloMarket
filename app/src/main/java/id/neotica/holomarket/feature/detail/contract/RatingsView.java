package id.neotica.holomarket.feature.detail.contract;

/**
 * View contract for the ratings/reviews UI, implemented by the detail Activity.
 * {@code RatingsPresenter} drives all rendering through this interface.
 */
public interface RatingsView {

    void renderRating(boolean loggedIn, int myRating, double averageRating, int totalReviews);

    void showRatingSaved();

    void showReviewDeleted();

    void showRatingError(String message);

    void onUnauthorized();
}