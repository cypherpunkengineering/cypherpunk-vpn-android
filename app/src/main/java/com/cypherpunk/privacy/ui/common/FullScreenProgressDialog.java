package com.cypherpunk.privacy.ui.common;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;

import com.cypherpunk.privacy.R;

/**
 * full screen progress dialog
 */
public class FullScreenProgressDialog extends AppCompatDialog {

    public FullScreenProgressDialog(Context context) {
        super(context, R.style.AppTheme_FullScreenProgressDialog);
        setContentView(R.layout.dialog_progress);
    }
}
