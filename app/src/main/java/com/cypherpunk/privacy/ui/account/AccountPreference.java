package com.cypherpunk.privacy.ui.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.account.Account;
import com.cypherpunk.privacy.datasource.account.Subscription;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
            final boolean isRenews = subscription.isRenews();

            final Context ctx = getContext();

            switch (type) {
                case NONE:
                    expirationViewView.setText("");
                    break;
                case TRIAL:
                case DAILY:
                case SEMIANNUALLY:
                case MONTHLY:
                case ANNUALLY:
                    if (expiration == null) {
                        expirationViewView.setText("");
                    } else {
                        final Calendar now = Calendar.getInstance(Locale.ENGLISH);
                        final Calendar exp = Calendar.getInstance();
                        exp.setTimeInMillis(expiration.getTime());
                        expirationViewView.setText(expirationText(ctx, isRenews, now, exp));
                    }
                    break;
                case FOREVER:
                    expirationViewView.setText(R.string.account_plan_forever);
                    break;
            }
        }
    }

    private static String expirationText(@NonNull Context ctx, boolean isRenews,
                                         @NonNull Calendar now, @NonNull Calendar expiration) {

        final long diff = (expiration.getTimeInMillis() - now.getTimeInMillis()) / (60 * 60 * 1000); // hours
        if (diff < 0) {
            return DateFormat.format(ctx.getString(R.string.account_plan_expired_format), expiration).toString();
        }

        final String prefix = ctx.getString(isRenews
                ? R.string.account_plan_renews : R.string.account_plan_expires);

        if (diff < 1) {
            return prefix + " in less than an hour";
        }
        if (diff < 6) {
            return prefix + " in less than six hours";
        }

        final int expirationDayOfMonth = expiration.get(Calendar.DAY_OF_MONTH);
        final int nowDayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        if (expirationDayOfMonth == nowDayOfMonth) {
            return prefix + " today";
        }

        final Calendar tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        final int tomorrowDayOfMonth = tomorrow.get(Calendar.DAY_OF_MONTH);
        if (expirationDayOfMonth == tomorrowDayOfMonth) {
            return prefix + " tomorrow";
        }

        if (diff <= 30 * 24) {
            return prefix + " in " + ((int) (Math.ceil(diff / 24.0))) + " days";
        } else if (diff <= 50 * 24) {
            return prefix + " in " + ((int) (Math.ceil(diff / 24.0 / 7))) + " weeks";
        } else if (diff <= 300 * 24) {
            return prefix + " in " + ((int) (Math.ceil(diff / 24.0 / 30))) + " months";
        } else if (diff <= 400 * 24) {
            return prefix + " in one year";
        } else {
            return prefix + " in over a year";
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
