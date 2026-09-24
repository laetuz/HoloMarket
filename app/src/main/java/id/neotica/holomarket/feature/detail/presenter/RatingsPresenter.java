package id.neotica.holomarket.feature.detail.presenter;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.detail.domain.ReviewModel;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.feature.detail.contract.RatingsView;
import id.neotica.holomarket.utils.AuthManager;

/**
 * MVP presenter owning the ratings/reviews state and business logic for the app-detail screen.
 * Rendering is delegated to a {@link RatingsView}; networking stays here (via {@link ApiTask}).
 */
public class RatingsPresenter {

    private final Context context;
    private final AuthManager authManager;

    private RatingsView view;
    private String packageName;
    private double averageRating = 0;
    private int totalReviews = 0;
    private int myRating = 0;
    private boolean isSubmitting = false;

    public RatingsPresenter(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public void attach(RatingsView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    /**
     * Seeds the initial stats from the app-detail response, then (if logged in) loads the
     * user's own review so the star row can be prefilled.
     */
    public void load(String packageName, double averageRating, int totalReviews) {
        this.packageName = packageName;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.myRating = 0;
        render();

        if (authManager.isLoggedIn()) {
            fetchMyReview();
        }
    }

    public void submitRating(final int rating) {
        if (isSubmitting || rating < 1) {
            return;
        }

        isSubmitting = true;

        requestSubmit(packageName, rating, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                isSubmitting = false;
                myRating = rating;
                if (view != null) {
                    view.showRatingSaved();
                }
                fetchMyReview();
            }

            @Override
            public void onError(String errorMessage) {
                isSubmitting = false;
                if (isUnauthorized(errorMessage)) {
                    if (view != null) {
                        view.onUnauthorized();
                    }
                } else {
                    if (view != null) {
                        view.showRatingError("Failed to save rating.");
                    }
                    render();
                }
            }
        });
    }

    public void deleteReview() {
        if (isSubmitting) {
            return;
        }

        isSubmitting = true;

        requestDelete(packageName, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                isSubmitting = false;
                myRating = 0;
                if (view != null) {
                    view.showReviewDeleted();
                }
                fetchMyReview();
            }

            @Override
            public void onError(String errorMessage) {
                isSubmitting = false;
                if (isUnauthorized(errorMessage)) {
                    if (view != null) {
                        view.onUnauthorized();
                    }
                } else {
                    if (view != null) {
                        view.showRatingError("Failed to delete review.");
                    }
                    render();
                }
            }
        });
    }

    private void fetchMyReview() {
        requestReviews(packageName, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    averageRating = root.optDouble("average_rating", averageRating);
                    totalReviews = root.optInt("total_reviews", totalReviews);

                    myRating = 0;
                    String me = currentUsername();

                    JSONArray reviewsArray = root.optJSONArray("reviews");
                    if (reviewsArray != null && me != null) {
                        for (int i = 0; i < reviewsArray.length(); i++) {
                            ReviewModel review = ReviewModel.fromJson(reviewsArray.optJSONObject(i));
                            if (review != null && me.equals(review.username)) {
                                myRating = review.rating;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                render();
            }

            @Override
            public void onError(String errorMessage) {
                render();
            }
        });
    }

    private void render() {
        if (view != null) {
            view.renderRating(authManager.isLoggedIn(), myRating, averageRating, totalReviews);
        }
    }

    private String currentUsername() {
        String me = authManager.getUsername();
        if (me == null) {
            me = authManager.getUsernameFromToken();
        }
        return me;
    }

    private boolean isUnauthorized(String errorMessage) {
        return errorMessage != null && errorMessage.contains("HTTP_ERROR_401");
    }

    // --- I/O seams: ApiTask lives here; overridden in tests to run synchronously ---

    void requestReviews(String pkg, ApiCallback callback) {
        String url = BuildConfig.BASE_URL + "/apps/" + pkg + "/reviews?limit=100";
        new ApiTask(context, "GET", url, null, null, callback, authManager.getAuthHeaders()).execute();
    }

    void requestSubmit(String pkg, int rating, ApiCallback callback) {
        String url = BuildConfig.BASE_URL + "/apps/" + pkg + "/reviews";
        try {
            JSONObject payload = new JSONObject();
            payload.put("rating", rating);
            new ApiTask(context, "POST", url, payload.toString(), "Saving rating...", callback,
                    authManager.getAuthHeaders()).execute();
        } catch (Exception e) {
            callback.onError("Error creating request");
        }
    }

    void requestDelete(String pkg, ApiCallback callback) {
        String url = BuildConfig.BASE_URL + "/apps/" + pkg + "/reviews";
        new ApiTask(context, "DELETE", url, null, "Deleting review...", callback,
                authManager.getAuthHeaders()).execute();
    }
}