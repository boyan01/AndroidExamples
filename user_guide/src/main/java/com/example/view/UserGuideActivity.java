package com.example.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

public class UserGuideActivity extends AppCompatActivity implements UserGuideFragment.OnGuideFragmentInteractionListener {

    private static final String TAG = "UserGuideActivity";

    private String[] titles = {"this is title 1", "this is title2", "welcome to this application"};

    private int[] colors = {0xFFF9A825, 0xFF26A69A, 0xFFFFFFFF};

    private ViewPager viewPager;
    private TextView textCurrentColor;

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        textCurrentColor = (TextView) findViewById(R.id.textCurrentColor);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return getUserGuideFragment(position);
            }

            @Override
            public int getCount() {
                return 3;
            }

        });

        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG, "onPageScrolled: " + " position = " + position
                        + "  positionOffset = " + positionOffset + "  positionOffsetPixels = " + positionOffsetPixels);
                int color = colors[position];
                if (positionOffset != 0) {
                    color = (int) argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]);
                }
                viewPager.setBackgroundColor(color);
                textCurrentColor.setText(Integer.toHexString(color));
                textCurrentColor.setTextColor(color);

            }

            @Override
            public void onPageSelected(int position) {
                textCurrentColor.setTextColor(colors[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private UserGuideFragment getUserGuideFragment(int position) {
        return UserGuideFragment.newInstance(titles[position],
                position == 0 ? null : "PREVIOUS",
                position == 2 ? "DONE" : "NEXT");
    }

    @Override
    public void onNext() {
        int index = viewPager.getCurrentItem();
        Log.i(TAG, "onNext: " + index);
        if (index < 2) {
            viewPager.setCurrentItem(index + 1, true);
        } else if (index == 2) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onPrevious() {
        int index = viewPager.getCurrentItem();
        if (index > 0) {
            viewPager.setCurrentItem(index - 1, true);
        }
    }
}
