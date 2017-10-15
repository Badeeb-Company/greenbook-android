package com.badeeb.greenbook.network;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
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
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class VolleyWrapper <T, S> {

    private final String TAG = VolleyWrapper.class.getSimpleName();

    private T jsonRequest;
    private S jsonResponse;
    private Type responseType;
    private int requestType; // POST, GET, ...etc
    private String url;
    private VolleyCallback<S> callback;
    private Context context;
    private boolean isSnackBarRequired;
    private View sankbarParentLayout;

    public VolleyWrapper(T jsonRequest, Type responseType, int requestType,
                         String url, VolleyCallback<S> callback, Context context,
                         boolean isSnackBarRequired, View sankbarParentLayout) {
        this.jsonRequest = jsonRequest;
        this.requestType = requestType;
        this.url = url;
        this.callback = callback;
        this.context = context;
        this.responseType = responseType;
        this.isSnackBarRequired = isSnackBarRequired;
        this.sankbarParentLayout = sankbarParentLayout;
    }

    public void execute() {

        Log.d(TAG, "execute - Start");

        try {

            JSONObject jsonObject = null;

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            if (requestType != Request.Method.GET) {
                jsonObject = new JSONObject(gson.toJson(jsonRequest));

                Log.d(TAG, "execute - Json Request"+ gson.toJson(jsonRequest));
            }

            // Service Call
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestType, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "execute - onResponse - Start");
                            Log.d(TAG, "execute - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            jsonResponse = gson.fromJson(responseData, responseType);

                            callback.onSuccess(jsonResponse);

                            Log.d(TAG, "execute - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "execute - onErrorResponse: " + error.toString());

                            if (error instanceof AuthFailureError && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(context, R.style.DialogTheme,
                                        R.string.login_error, R.string.ok_btn_dialog, null);
                            }
                            else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonResponse errorResponse = gson.fromJson(responseData, JsonResponse.class);

                                if (isSnackBarRequired) {
                                    UiUtils.showSnackBar(sankbarParentLayout,
                                            errorResponse.getJsonMeta().getMessage(),
                                            Snackbar.LENGTH_INDEFINITE, context.getResources().getColor(R.color.orange), R.drawable.btn_close,
                                            new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(context, errorResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
                                }
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

        Log.d(TAG, "execute - End");
    }

}