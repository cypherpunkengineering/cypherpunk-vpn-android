package com.cypherpunk.android.vpn.ui.setup;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial);

        final IntroductionPagerAdapter adapter = new IntroductionPagerAdapter(this);
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
                if (currentItem == adapter.getCount() - 1) {
                    Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
                    TaskStackBuilder builder = TaskStackBuilder.create(TutorialActivity.this);
                    builder.addNextIntent(intent);
                    builder.startActivities();
                } else {
                    binding.pager.setCurrentItem(++currentItem);
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

    private static class IntroductionPagerAdapter extends PagerAdapter {

        private static final int[] layouts = {R.layout.tutorial_1, R.layout.tutorial_2,
                R.layout.tutorial_1};

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
