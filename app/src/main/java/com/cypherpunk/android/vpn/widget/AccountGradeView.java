package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ViewAccountGradeBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class AccountGradeView extends FrameLayout {

    public static final int FREE = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;

    private ViewAccountGradeBinding binding;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FREE, MONTHLY, YEARLY})
    public @interface AccountGrade {
    }

    public AccountGradeView(Context context) {
        this(context, null);
    }

    public AccountGradeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccountGradeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_account_grade, this, true);
        setGrade(FREE);
    }

    public void setGrade(@AccountGrade int grade) {
        switch (grade) {
            case FREE:
                binding.free.setChecked(true);
                break;
            case MONTHLY:
                binding.monthly.setChecked(true);
                break;
            case YEARLY:
                binding.yearly.setChecked(true);
                break;
        }
    }
}
