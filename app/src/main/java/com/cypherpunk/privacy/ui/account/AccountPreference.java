package com.cypherpunk.privacy.ui.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.privacy.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AccountPreference extends Preference {

    private String username;
    private String type;
    private String expiration;
    private String renewal;

    public AccountPreference(Context context) {
        this(context, null);
    }

    public AccountPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_account);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView usernameView = (TextView) holder.itemView.findViewById(R.id.username);
        TextView typeView = (TextView) holder.itemView.findViewById(R.id.type);
        TextView expirationViewView = (TextView) holder.itemView.findViewById(R.id.expiration);

        usernameView.setText(username);
        typeView.setText(type);

        if (!TextUtils.isEmpty(renewal) && renewal.equalsIgnoreCase("forever"))
            expirationViewView.setText(getContext().getString(R.string.account_plan_expiration_forever));
        else if (!TextUtils.isEmpty(expiration))
            expirationViewView.setText(getContext().getString(R.string.account_plan_expiration_date, renewal, expiration));
        else if (!TextUtils.isEmpty(renewal))
            expirationViewView.setText(getContext().getString(R.string.account_plan_expiration_nodate, renewal));
        else
            expirationViewView.setText("");
    }

    public void setUsernameText(@NonNull String username) {
        this.username = username;
        notifyChanged();
    }

    public void setType(@NonNull String type) {
        switch (type) {
            case "free":
                this.type = getContext().getString(R.string.account_type_free);
                break;
            case "premium":
                this.type = getContext().getString(R.string.account_type_premium);
                break;
            case "organization":
                this.type = getContext().getString(R.string.account_type_organization);
                break;
            case "developer":
                this.type = getContext().getString(R.string.account_type_developer);
                break;
        }
        notifyChanged();
    }

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public void setRenewalAndExpiration(@NonNull String renewal, @NonNull String expiration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault());
        Date date;

        try {
            date = simpleDateFormat.parse(expiration);
        } catch (ParseException e) {
            date = null;
            //e.printStackTrace();
            //return;
        }

        switch (renewal) {
            case "monthly":
                this.renewal = getContext().getString(R.string.account_plan_monthly);
                break;
            case "semiannually":
                this.renewal = getContext().getString(R.string.account_plan_semiannually);
                break;
            case "annually":
                this.renewal = getContext().getString(R.string.account_plan_annually);
                break;
            case "forever":
                this.renewal = getContext().getString(R.string.account_plan_forever);
                break;
            case "none": // free account
            default:
                return;
        }

        int expFmt = R.string.account_plan_expiration_format;

        if (date != null)
            this.expiration = DateFormat.format(getContext().getString(expFmt), date).toString();
        else
            this.expiration = "";
        notifyChanged();
    }
}
