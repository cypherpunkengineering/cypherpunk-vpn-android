package com.cypherpunk.privacy.widget;

import android.app.Dialog;
import android.content.Context;

import com.cypherpunk.privacy.R;

public class ProgressFullScreenDialog extends Dialog {

    public ProgressFullScreenDialog(Context context) {
        super(context, R.style.AppTheme_FullScreenProgressDialog);
        setContentView(R.layout.dialog_progress);
    }
}
