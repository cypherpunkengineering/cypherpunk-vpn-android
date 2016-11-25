package com.cypherpunk.privacy.ui.setup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.LoginRequest;
import com.cypherpunk.privacy.data.api.json.LoginResult;
import com.cypherpunk.privacy.data.api.json.RegionResult;
import com.cypherpunk.privacy.databinding.ActivityTutorialBinding;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;
import com.cypherpunk.privacy.model.UserSettingPref;
import com.cypherpunk.privacy.ui.main.MainActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class TutorialActivity extends AppCompatActivity {

    private ActivityTutorialBinding binding;
    private Subscription subscription = Subscriptions.empty();

    private static final int GRANT_VPN_PERMISSION = 1;

    @Inject
    Realm realm;

    @Inject
    CypherpunkService webService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial);

        long count = realm.where(Region.class).count();
        if (count == 0) {
            getServerList();
        }

        IntroductionPagerAdapter adapter = new IntroductionPagerAdapter(this);
        binding.pager.setAdapter(adapter);
        binding.indicator.setViewPager(binding.pager);
        binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.doNotAllowButton.setVisibility(position == 1 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        binding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = binding.pager.getCurrentItem();
                switch (currentItem) {
                    case 0: {
                        Intent intent = VpnService.prepare(getApplicationContext());
                        if (intent != null)
                            startActivityForResult(intent, GRANT_VPN_PERMISSION);
                        else
                            onActivityResult(GRANT_VPN_PERMISSION, RESULT_OK, null);
                        break;
                    }
                    case 1: {
                        CypherpunkSetting setting = new CypherpunkSetting();
                        setting.analytics = true;
                        setting.save();

                        // enable firebase
                        FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(true);

                        goToMainScreen();
                        break;
                    }
                }
            }
        });

        binding.doNotAllowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CypherpunkSetting setting = new CypherpunkSetting();
                setting.analytics = false;
                setting.save();

                // disable firebase
                FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(false);

                goToMainScreen();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GRANT_VPN_PERMISSION:
                    int currentItem = binding.pager.getCurrentItem();
                    binding.pager.setCurrentItem(++currentItem);
                    break;
            }
        }
    }

    private void getServerList() {
        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<LoginResult, Single<Map<String, RegionResult>>>() {
                    @Override
                    public Single<Map<String, RegionResult>> call(LoginResult result) {
                        UserManager.saveSecret(result.getSecret());
                        UserSettingPref userPref = new UserSettingPref();
                        userPref.userStatusType = result.getAccount().type;
                        return webService.serverList(userPref.userStatusType);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Map<String, RegionResult>>() {
                               @Override
                               public void onSuccess(Map<String, RegionResult> result) {
                                   realm.beginTransaction();
                                   List<String> updateRegionIdList = new ArrayList<>();
                                   for (Map.Entry<String, RegionResult> resultEntry : result.entrySet()) {
                                       RegionResult regionResult = resultEntry.getValue();
                                       Region region = realm.where(Region.class)
                                               .equalTo("id", regionResult.getId()).findFirst();
                                       if (region != null) {
                                           region.setRegionName(regionResult.getName());
                                           region.setOvHostname(regionResult.getOvHostname());
//                                           region.setOvDefault(regionResult.getOvDefault());
//                                           region.setOvNone(regionResult.getOvNone());
//                                           region.setOvStrong(regionResult.getOvStrong());
//                                           region.setOvStealth(regionResult.getOvStealth());
                                       } else {
//                                           region = new Region(
//                                                   regionResult.getId(),
//                                                   regionResult.getRegion(),
//                                                   regionResult.getCountry(),
//                                                   regionResult.getName(),
//                                                   regionResult.isRegionEnabled(),
//                                                   "",
//                                                   "",
//                                                   "",
//                                                   "",
//                                                   "");
//                                           realm.copyToRealm(region);
                                       }
                                           updateRegionIdList.add(region.getId());
                                   }
                                   RealmResults<Region> oldRegion = realm.where(Region.class)
                                           .not()
                                           .beginGroup()
                                           .in("id", updateRegionIdList.toArray(new String[updateRegionIdList.size()]))
                                           .endGroup()
                                           .findAll();
                                   oldRegion.deleteAllFromRealm();
                                   realm.commitTransaction();

                                   // TODO: 一番上のを選択している
                                   CypherpunkSetting setting = new CypherpunkSetting();
                                   if (TextUtils.isEmpty(setting.regionId)) {
                                       Region first = realm.where(Region.class).findFirst();
                                       setting.regionId = first.getId();
                                       setting.save();
                                   }
                               }

                               @Override
                               public void onError(Throwable error) {
                                   error.printStackTrace();
                               }
                           }

                );

    }

    @Override
    public void onBackPressed() {
        int currentItem = binding.pager.getCurrentItem();
        if (currentItem != 0) {
            binding.pager.setCurrentItem(--currentItem);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        subscription.unsubscribe();
    }

    private void goToMainScreen() {
        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(TutorialActivity.this);
        builder.addNextIntent(intent);
        builder.startActivities();
    }

    private static class IntroductionPagerAdapter extends PagerAdapter {

        private static final int[] layouts = {R.layout.tutorial_1, R.layout.tutorial_2};

        private final LayoutInflater inflater;

        public IntroductionPagerAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object.equals(view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(layouts[position], container, false);
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
