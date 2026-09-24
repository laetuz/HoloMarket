package id.neotica.holomarket.feature.detail.domain;

import org.json.JSONObject;

/**
 * Created by ryomartin on 24/09/26.
 */

public class ReviewModel {
    public final String id;
    public final String appId;
    public final String userId;
    public final String username;
    public final int rating;
    public final String comment;
    public final long createdAt;

    public ReviewModel(String id, String appId, String userId, String username,
                       int rating, String comment, long createdAt) {
        this.id = id;
        this.appId = appId;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static ReviewModel fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        return new ReviewModel(
                obj.optString("id", ""),
                obj.optString("app_id", ""),
                obj.optString("user_id", ""),
                obj.optString("username", ""),
                obj.optInt("rating", 0),
                obj.optString("comment", ""),
                obj.optLong("created_at", 0)
        );
    }
}