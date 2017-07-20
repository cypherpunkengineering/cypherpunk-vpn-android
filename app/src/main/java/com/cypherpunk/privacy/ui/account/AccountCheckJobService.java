package com.cypherpunk.privacy.ui.account;

import android.content.Context;
import android.support.annotation.NonNull;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.vpn.VpnManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import javax.inject.Inject;

import io.reactivex.observers.DisposableSingleObserver;
import retrofit2.HttpException;

/**
 * check account periodically
 */
public class AccountCheckJobService extends JobService {

    @Inject
    AccountSetting accountSetting;

    @Inject
    NetworkRepository networkRepository;

    @Inject
    VpnManager vpnManager;

    @Override
    public boolean onStartJob(JobParameters job) {
        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!accountSetting.isSignedIn()) {
            return false;
        }

        networkRepository.accountStatus()
                .subscribe(new DisposableSingleObserver<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult accountStatus) {
                        accountSetting.updateAccount(accountStatus.account);
                        accountSetting.updateSubscription(accountStatus.subscription);
                        setNextJob(getApplicationContext());
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (error instanceof HttpException) {
                            HttpException httpException = (HttpException) error;
                            switch (httpException.code()) {
                                case 400:
                                case 401:
                                case 402:
                                    // force sign out
                                    vpnManager.stop();
                                    accountSetting.clear();
                                    return;
                            }
                        }
                        setNextJob(getApplicationContext());
                    }
                });
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    public static void setNextJob(@NonNull Context context) {
        // check account periodically
        final FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context.getApplicationContext()));
        final Job job = dispatcher.newJobBuilder()
                .setService(AccountCheckJobService.class)
                .setTag("AccountCheckJobService")
                .setRecurring(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(3600 * 24, 3600 * 24 * 2))
                .build();

        dispatcher.schedule(job);
    }
}
