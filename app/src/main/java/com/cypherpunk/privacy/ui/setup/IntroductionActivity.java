package com.cypherpunk.privacy.ui.setup;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivityIntroductionBinding;
import com.cypherpunk.privacy.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.signin.SignInActivity;


/**
 * unused
 */
public class IntroductionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityIntroductionBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_introduction);

        binding.loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(IntroductionActivity.this, SignInActivity.class));
                    }
                });

        binding.signUpPremiumButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(IntroductionActivity.this, IdentifyEmailActivity.class));
                    }
                });

        binding.getPremiumButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(IntroductionActivity.this, GetPremiumActivity.class));
                    }
                });

        binding.pager.setAdapter(new IntroductionPagerAdapter(this));
        binding.indicator.setViewPager(binding.pager);
    }

    private static class IntroductionPagerAdapter extends PagerAdapter {

        private static final int[] layouts = {R.layout.introduction, R.layout.introduction,
                R.layout.introduction, R.layout.introduction};

        private final LayoutInflater inflater;

        IntroductionPagerAdapter(Context context) {
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
