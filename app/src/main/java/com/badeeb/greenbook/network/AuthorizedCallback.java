package com.badeeb.greenbook.network;

import java.util.HashMap;

/**
 * Created by Amr Alghawy on 10/15/2017.
 */

public abstract class AuthorizedCallback<T> implements VolleyCallback<T> {

    private String token;

    public AuthorizedCallback(String token) {
        this.token = token;
    }

    public void requestHeader(HashMap<String, String> headers) {
        headers.put("Authorization", "Token token=" + token);
    }

}
