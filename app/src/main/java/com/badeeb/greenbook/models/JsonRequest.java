package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 10/15/2017.
 */

public class JsonRequest<T> {

    @Expose(serialize = true, deserialize = false)
    @SerializedName("data")
    private T request;

    public JsonRequest(T request) {
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }
}
