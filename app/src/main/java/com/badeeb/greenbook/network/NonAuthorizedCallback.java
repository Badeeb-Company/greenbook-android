package com.badeeb.greenbook.network;

import java.util.HashMap;

/**
 * Created by Amr Alghawy on 10/15/2017.
 */

public abstract class NonAuthorizedCallback<T> implements VolleyCallback<T> {

    public void requestHeader(HashMap<String, String> headers){

    }
}
