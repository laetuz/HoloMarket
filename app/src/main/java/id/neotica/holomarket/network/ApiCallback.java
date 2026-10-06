package id.neotica.holomarket.network;

/**
 * Created by ryomartin on 14/03/26.
 */

public interface ApiCallback {
    void onSuccess(String response);
    void onError(String errorMessage);
}