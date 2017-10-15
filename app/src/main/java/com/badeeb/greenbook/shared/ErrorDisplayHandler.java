package com.badeeb.greenbook.shared;

import android.view.View;

/**
 * Created by ahmed on 10/15/2017.
 */

public interface ErrorDisplayHandler {
    void displayError(String message);
    void displayErrorWithAction(String message,int icon , View.OnClickListener onClickListener);
}
