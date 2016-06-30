package com.cypherpunk.android.vpn.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityTutorialBinding;

public class TutorialActivity extends AppCompatActivity {

    private ActivityTutorialBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        final TutorialPagerAdapter adapter = new TutorialPagerAdapter(this,
                new TutorialPagerAdapter.TutorialListener() {
                    @Override
                    public void onSignInButtonClick() {
                        startActivity(new Intent(TutorialActivity.this, LoginActivity.class));
                    }

                    @Override
                    public void onCreateAccountButtonClick() {
                        startActivity(new Intent(TutorialActivity.this, SignUpActivity.class));
                    }
                });
        binding.pager.setAdapter(adapter);
        binding.indicator.setViewPager(binding.pager);
        binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.prevButton.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                binding.nextButton.setVisibility(
                        position == adapter.getCount() - 1 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        binding.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = binding.pager.getCurrentItem();
                binding.pager.setCurrentItem(--currentItem);
            }
        });
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = binding.pager.getCurrentItem();
                binding.pager.setCurrentItem(++currentItem);
            }
        });
    }

    private static class TutorialPagerAdapter extends PagerAdapter {

        private static final int[] layouts = {R.layout.tutorial1, R.layout.tutorial2,
                R.layout.tutorial3};

        private final LayoutInflater inflater;
        private TutorialListener listener;

        public interface TutorialListener {
            void onSignInButtonClick();

            void onCreateAccountButtonClick();
        }

        public TutorialPagerAdapter(Context context, @NonNull TutorialListener listener) {
            inflater = LayoutInflater.from(context);
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View v, Object o) {
            return o.equals(v);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(layouts[position], container, false);
            switch (position) {
                case 2:
                    TextView signInButton = (TextView) view.findViewById(R.id.sign_in_button);
                    SpannableStringBuilder stringBuilder1 = new SpannableStringBuilder();
                    stringBuilder1.append(container.getResources().getString(R.string.tutorial_sign_in1));
                    stringBuilder1.setSpan(new UnderlineSpan(), 0,
                            stringBuilder1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    SpannableStringBuilder stringBuilder2 = new SpannableStringBuilder();
                    stringBuilder2.append(container.getResources().getString(R.string.tutorial_sign_in2));
                    stringBuilder2.setSpan(new UnderlineSpan(), 0,
                            stringBuilder2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    stringBuilder2.setSpan(new StyleSpan(Typeface.BOLD), 0,
                            stringBuilder2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    signInButton.setText(stringBuilder1.append(stringBuilder2));

                    signInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.onSignInButtonClick();
                        }
                    });

                    view.findViewById(R.id.create_account_button).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    listener.onCreateAccountButtonClick();
                                }
                            });
                    break;
            }
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
