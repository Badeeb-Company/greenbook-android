package com.badeeb.greenbook.network;

import java.util.HashMap;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public interface VolleyCallback<T> {
    public void onSuccess(VolleyResponse<T> jsonResponse);
    public void onError();
    public void requestHeader(HashMap<String, String> headers);
}