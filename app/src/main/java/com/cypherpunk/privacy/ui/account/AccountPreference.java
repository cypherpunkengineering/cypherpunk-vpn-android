package com.cypherpunk.privacy.ui.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.account.Account;
import com.cypherpunk.privacy.datasource.account.Subscription;

import java.util.Date;

import butterknife.ButterKnife;

public class AccountPreference extends Preference {

    private String username;
    @Nullable
    private Account.Type type;
    @Nullable
    private Subscription subscription;

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
        final TextView usernameView = ButterKnife.findById(holder.itemView, R.id.username);
        final TextView typeView = ButterKnife.findById(holder.itemView, R.id.type);
        final TextView expirationViewView = ButterKnife.findById(holder.itemView, R.id.expiration);
        final ImageView imageView = ButterKnife.findById(holder.itemView, R.id.image);

        usernameView.setText(username);

        if (type != null) {
            switch (type) {
                case FREE:
                    typeView.setText(R.string.account_type_free);
                    imageView.setImageResource(R.drawable.account_banner_free);
                    break;
                case PREMIUM:
                    typeView.setText(R.string.account_type_premium);
                    imageView.setImageResource(R.drawable.account_banner_premium);
                    break;
                case ORGANIZATION:
                    typeView.setText(R.string.account_type_organization);
                    imageView.setImageResource(R.drawable.account_banner_premium);
                    break;
                case DEVELOPER:
                    typeView.setText(R.string.account_type_developer);
                    imageView.setImageResource(R.drawable.account_banner_premium);
                    break;
            }
        }

        if (subscription != null) {
            final Subscription.Type type = subscription.type();
            final Date expiration = subscription.expiration();

            final Context ctx = getContext();

            switch (type) {
                case NONE:
                    expirationViewView.setText("");
                    break;
                case TRIAL:
                    expirationViewView.setText(R.string.account_plan_trial);
                    break;
                case DAILY:
                    expirationViewView.setText(R.string.account_plan_daily);
                    break;
                case MONTHLY:
                    expirationViewView.setText(typeExpirationText(ctx, R.string.account_plan_monthly, expiration));
                    break;
                case ANNUALLY:
                    expirationViewView.setText(typeExpirationText(ctx, R.string.account_plan_annually, expiration));
                    break;
                case FOREVER:
                    expirationViewView.setText(R.string.account_plan_forever);
                    break;
            }
        }
    }

    private static String typeExpirationText(@NonNull Context ctx, @StringRes int typeResId, @Nullable Date expiration) {
        if (expiration != null) {
            return ctx.getString(R.string.account_plan_expiration_date,
                    ctx.getString(typeResId),
                    DateFormat.format(ctx.getString(R.string.account_plan_expiration_format), expiration).toString());
        } else {
            return ctx.getString(R.string.account_plan_expiration_nodate, ctx.getString(typeResId));
        }
    }

    void setUsernameText(@Nullable String username) {
        this.username = username;
        notifyChanged();
    }

    void setInfo(@Nullable String username, @NonNull Account.Type type, @NonNull Subscription subscription) {
        this.username = username;
        this.type = type;
        this.subscription = subscription;
        notifyChanged();
    }
}
