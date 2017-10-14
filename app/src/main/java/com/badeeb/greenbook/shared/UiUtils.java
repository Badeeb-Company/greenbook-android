package com.badeeb.greenbook.shared;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.badeeb.greenbook.R;

/**
 * Created by meldeeb on 9/21/17.
 */

public class UiUtils {

    public static void hide(View v) {
        v.setVisibility(View.GONE);
    }

    public static void show(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public static void enable(View v) {
        v.setEnabled(true);
        v.setAlpha(1f);
    }

    public static void disable(View v) {
        v.setEnabled(false);
        v.setAlpha(0.5f);
    }

    public static void hideInputKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void showInputKeyboard(Context context, EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static ProgressDialog createProgressDialog(Context context, int theme){
        return createProgressDialog(context, "Loading. Please wait...", theme);
    }

    public static ProgressDialog createProgressDialog(Context context, String message, int theme){
        ProgressDialog progressDialog = new ProgressDialog(context, theme);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    /**
     * Dialog with both positive and negative listeners
     * @param context
     * @param style
     * @param title
     * @param message
     * @param positiveMessage
     * @param positiveListener
     * @param negativeMessage
     * @param negativeListener
     * @return
     */
    public static AlertDialog showDialog(Context context, int style, int title, int message,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener,
                                         int negativeMessage, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setNegativeButton(negativeMessage, negativeListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    /**
     * Dialog with on positive listener
     * @param context
     * @param style
     * @param title
     * @param message
     * @param positiveMessage
     * @param positiveListener
     * @return
     */
    public static AlertDialog showDialog(Context context, int style, int title, int message,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener
                                         ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    /**
     * Dialog with on positive listener title only
     * @param context
     * @param style
     * @param title
     * @param positiveMessage
     * @param positiveListener
     * @return
     */
    public static AlertDialog showDialog(Context context, int style, int title,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    // Snackbar with message only
    public static Snackbar showSnackBar(View view, String message, int duration) {

        Snackbar snackbar = Snackbar
                .make(view, message, duration)
                ;

        snackbar.show();

        return snackbar;
    }

    // Snackbar with message and action
    public static Snackbar showSnackBar(View view, String message, int duration, String actionName, View.OnClickListener onClickListener) {

        Snackbar snackbar = Snackbar
                .make(view, message, duration)
                .setAction(actionName, onClickListener)
                ;

        snackbar.show();

        return snackbar;
    }

    // Snackbar with message and icon
    public static Snackbar showSnackBar(View view, String message, int duration, int color, int icon, View.OnClickListener actionListener) {

        final Snackbar snackbar = Snackbar
                .make(view, message, duration)
                .setAction(" ", actionListener)
                ;

        View snackbarView = snackbar.getView();
        TextView action = snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        action.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);

        snackbarView.setBackgroundColor(color);
        snackbar.show();

        return snackbar;
    }
}
