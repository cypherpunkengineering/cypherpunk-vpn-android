package com.cypherpunk.android.vpn.ui.setup;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityTutorialBinding;
import com.cypherpunk.android.vpn.ui.main.MainActivity;


public class TutorialActivity extends AppCompatActivity {

    private ActivityTutorialBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial);

        final IntroductionPagerAdapter adapter = new IntroductionPagerAdapter(this);
        binding.pager.setAdapter(adapter);
        binding.indicator.setViewPager(binding.pager);
        binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.skipButton.setVisibility(position == adapter.getCount() - 1 ? View.GONE : View.VISIBLE);
                binding.tutorialFinishButton.setVisibility(
                        position == adapter.getCount() - 1 ? View.VISIBLE : View.GONE);
            }
        });

        binding.tutorialFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
                TaskStackBuilder builder = TaskStackBuilder.create(TutorialActivity.this);
                builder.addNextIntent(intent);
                builder.startActivities();
            }
        });

        binding.skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = binding.pager.getCurrentItem();
                binding.pager.setCurrentItem(++currentItem);
            }
        });
    }

    private static class IntroductionPagerAdapter extends PagerAdapter {

        private static final int[] layouts = {R.layout.introduction, R.layout.introduction,
                R.layout.introduction};

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
