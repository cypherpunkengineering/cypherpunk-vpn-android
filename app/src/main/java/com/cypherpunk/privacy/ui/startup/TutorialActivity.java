package com.cypherpunk.privacy.ui.startup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.AccountStatusResult;
import com.cypherpunk.privacy.data.api.json.RegionResult;
import com.cypherpunk.privacy.domain.model.AccountType;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.service.ServerService;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;
import com.cypherpunk.privacy.model.UserSetting;
import com.cypherpunk.privacy.ui.main.MainActivity;
import com.cypherpunk.privacy.widget.PageIndicator;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import io.realm.Realm;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * get account type and server list during tutorial.
 */
public class TutorialActivity extends AppCompatActivity {

    private static final int GRANT_VPN_PERMISSION = 1;

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @Inject
    Realm realm;

    @Inject
    CypherpunkService webService;

    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.positive_button)
    TextView positiveButton;

    @BindView(R.id.negative_button)
    TextView negativeButton;

    private IntroductionPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final long count = realm.where(Region.class).count();
        if (count == 0) {
            getServerList();
        }

        adapter = new IntroductionPagerAdapter(this);
        pager.setAdapter(adapter);

        final PageIndicator indicator = ButterKnife.findById(this, R.id.indicator);
        indicator.setViewPager(pager);

        onPageSelected(0);
    }

    @OnPageChange(R.id.pager)
    void onPageSelected(int position) {
        final TutorialItem item = adapter.getItem(position);
        positiveButton.setText(item.buttonStringId);
        negativeButton.setVisibility(item.type == TutorialType.ANALYTICS ? View.VISIBLE : View.INVISIBLE);
    }

    @OnClick(R.id.positive_button)
    void onPositiveButtonClicked() {
        final TutorialItem item = adapter.getItem(pager.getCurrentItem());
        switch (item.type) {
            case VPN_PERMISSION:
                final Intent intent = VpnService.prepare(getApplicationContext());
                if (intent != null) {
                    startActivityForResult(intent, GRANT_VPN_PERMISSION);
                } else {
                    moveToNextPage();
                }
                break;

            case ANALYTICS:
                setAnalytics(true);
                moveToNextPage();
                break;
        }
    }

    @OnClick(R.id.negative_button)
    void onNegativeButtonClicked() {
        final TutorialItem item = adapter.getItem(pager.getCurrentItem());
        switch (item.type) {
            case ANALYTICS:
                setAnalytics(false);
                moveToNextPage();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GRANT_VPN_PERMISSION) {
            moveToNextPage();
        }
    }

    private void setAnalytics(boolean enabled) {
        final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();
        vpnSetting.updateAnalyticsEnabled(enabled);

        FirebaseAnalytics.getInstance(getApplicationContext())
                .setAnalyticsCollectionEnabled(enabled);
    }

    private void moveToNextPage() {
        final int position = pager.getCurrentItem();
        if (position < adapter.getCount() - 1) {
            pager.setCurrentItem(position + 1);
        } else {
            TaskStackBuilder.create(this)
                    .addNextIntent(new Intent(this, MainActivity.class))
                    .startActivities();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        final int currentItem = pager.getCurrentItem();
        if (currentItem > 0) {
            pager.setCurrentItem(currentItem - 1);
        }
    }

    private void getServerList() {
        subscription = webService.getAccountStatus()
                .flatMap(new Func1<AccountStatusResult, Single<Map<String, RegionResult>>>() {
                    @Override
                    public Single<Map<String, RegionResult>> call(AccountStatusResult result) {
                        final String type = result.getAccount().type;
                        UserSetting.instance().updateAccountType(AccountType.find(type));
                        return webService.serverList(type);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Map<String, RegionResult>>() {
                    @Override
                    public void onSuccess(Map<String, RegionResult> result) {
                        final ServerService serverService = new ServerService(realm);
                        serverService.updateServerList(result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        subscription.unsubscribe();
    }

    private enum TutorialType {
        VPN_PERMISSION,
        ANALYTICS,
    }

    private static class TutorialItem {
        @NonNull
        private final TutorialType type;
        @LayoutRes
        private final int layoutResId;
        @StringRes
        private final int buttonStringId;

        private TutorialItem(@NonNull TutorialType type, int layoutResId, int buttonStringId) {
            this.type = type;
            this.layoutResId = layoutResId;
            this.buttonStringId = buttonStringId;
        }
    }

    private static class IntroductionPagerAdapter extends PagerAdapter {

        private static final TutorialItem[] ITEMS = {
                new TutorialItem(TutorialType.VPN_PERMISSION, R.layout.tutorial_setup_vpn, R.string.tutorial_allow),
                new TutorialItem(TutorialType.ANALYTICS, R.layout.tutorial_analytics, R.string.tutorial_allow),
        };

        private final LayoutInflater inflater;

        IntroductionPagerAdapter(@NonNull Context context) {
            inflater = LayoutInflater.from(context);
        }

        TutorialItem getItem(int position) {
            return ITEMS[position];
        }

        @Override
        public int getCount() {
            return ITEMS.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object.equals(view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final View view = inflater.inflate(ITEMS[position].layoutResId, container, false);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = (View) object;
            container.removeView(v);
        }
    }
}