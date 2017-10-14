package com.badeeb.greenbook.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class VolleyWrapper <T, S> {

    private T jsonRequest;
    private VolleyResponse<S> jsonResponse;
    private int requestType; // POST, GET, ...etc
    private String url;
    private VolleyCallback<S> callback;
    private Context context;

    public VolleyWrapper(T jsonRequest, int requestType, String url, VolleyCallback<S> callback, Context context) {
        this.jsonRequest = jsonRequest;
        this.requestType = requestType;
        this.callback = callback;
        this.context = context;
    }

    public void execute() {

        try {

            JSONObject jsonObject = null;

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            if (requestType != Request.Method.GET) {
                jsonObject = new JSONObject(gson.toJson(jsonRequest));
            }
//            Log.d(TAG, "login - Json Request"+ gson.toJson(request));

            // Service Call
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestType, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
//                            Log.d(TAG, "login - onResponse - Start");

//                            Log.d(TAG, "login - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            jsonResponse = gson.fromJson(responseData, jsonResponse.getClass().getGenericSuperclass());

                            // check status  code of response
                            callback.onSuccess(jsonResponse);

//                            Log.d(TAG, "login - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof AuthFailureError && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(context, R.style.DialogTheme,
                                        R.string.login_error, R.string.ok_btn_dialog, null);
                            }
                            else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                jsonResponse = gson.fromJson(responseData, jsonResponse.getClass().getGenericSuperclass());

                                Toast.makeText(context, jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            // Network Error Handling
                            callback.onError();
                        }
                    }
            ) {

                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Accept", "*");
                    callback.requestHeader(headers);
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT, Constants.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(context).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
