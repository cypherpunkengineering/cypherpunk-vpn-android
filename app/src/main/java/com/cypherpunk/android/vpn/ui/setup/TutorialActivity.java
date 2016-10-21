package com.cypherpunk.android.vpn.ui.setup;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkService;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.data.api.json.LoginResult;
import com.cypherpunk.android.vpn.data.api.json.RegionResult;
import com.cypherpunk.android.vpn.databinding.ActivityTutorialBinding;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.FavoriteRegion;
import com.cypherpunk.android.vpn.model.Region;
import com.cypherpunk.android.vpn.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class TutorialActivity extends AppCompatActivity {

    private ActivityTutorialBinding binding;
    private IntroductionPagerAdapter adapter;
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial);

        long count = realm.where(Region.class).count();
        if (count == 0) {
            getServerList();
        }

        adapter = new IntroductionPagerAdapter(this);
        binding.pager.setAdapter(adapter);
        binding.indicator.setViewPager(binding.pager);
        binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.doNotAllowButton.setVisibility(position == 1 ? View.VISIBLE : View.INVISIBLE);
                binding.okButton.setText(
                        position == adapter.getCount() - 1 ? R.string.tutorial_start : R.string.tutorial_allow);
            }
        });

        binding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = binding.pager.getCurrentItem();
                if (currentItem == 0)
                {
                    Intent intent = VpnService.prepare(getApplicationContext());
                    if (intent != null)
                        startActivityForResult(intent, GRANT_VPN_PERMISSION);
                    else
                        onActivityResult(GRANT_VPN_PERMISSION, RESULT_OK, null);
                }
                else
                {
                    if (currentItem == adapter.getCount() - 1) {
                        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
                        TaskStackBuilder builder = TaskStackBuilder.create(TutorialActivity.this);
                        builder.addNextIntent(intent);
                        builder.startActivities();
                    } else {
                        binding.pager.setCurrentItem(++currentItem);
                    }
                }
            }
        });

        binding.doNotAllowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = binding.pager.getCurrentItem();
                binding.pager.setCurrentItem(++currentItem);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
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
                .flatMap(new Func1<LoginResult, Single<Map<String, Map<String, RegionResult[]>>>>() {
                    @Override
                    public Single<Map<String, Map<String, RegionResult[]>>> call(LoginResult result) {
                        return webService.serverList();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Map<String, Map<String, RegionResult[]>>>() {
                               @Override
                               public void onSuccess(Map<String, Map<String, RegionResult[]>> result) {
                                   List<Region> regionList = new ArrayList<>();
                                   for (Map.Entry<String, Map<String, RegionResult[]>> area : result.entrySet()) {
                                       Set<Map.Entry<String, RegionResult[]>> areaSet = area.getValue().entrySet();
                                       for (Map.Entry<String, RegionResult[]> country : areaSet) {
                                           RegionResult[] regions = country.getValue();
                                           for (RegionResult regionResult : regions) {
                                               Region region = new Region(
                                                       regionResult.getId(),
                                                       country.getKey(),
                                                       regionResult.getRegionName(),
                                                       regionResult.getOvHostname(),
                                                       regionResult.getOvDefault(),
                                                       regionResult.getOvNone(),
                                                       regionResult.getOvStrong(),
                                                       regionResult.getOvStealth());

                                               long id = realm.where(FavoriteRegion.class)
                                                       .equalTo("id", regionResult.getId()).count();
                                               if (id != 0) {
                                                   region.setFavorited(true);
                                               }
                                               regionList.add(region);
                                           }
                                       }
                                   }
                                   realm.beginTransaction();
                                   realm.copyToRealm(regionList);
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

    private static class IntroductionPagerAdapter extends PagerAdapter {

        private static final int[] layouts = {R.layout.tutorial_1, R.layout.tutorial_2,
                R.layout.tutorial_3};

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
