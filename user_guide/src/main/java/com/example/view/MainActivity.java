package com.example.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView textCurrentColor;
    private ConstraintLayout constraintLayout;

    private int colorStart = Color.WHITE;
    private int colorEnd = Color.RED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        textCurrentColor = (TextView) findViewById(R.id.textCurrentColor);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        constraintLayout.setBackgroundColor(colorStart);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValueAnimator animator = ValueAnimator.ofInt(colorStart, colorEnd);
                animator.setDuration(3000);
                animator.setEvaluator(new ArgbEvaluator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (int) animation.getAnimatedValue();
                        textCurrentColor.setText(Integer.toHexString(color));
                        constraintLayout.setBackgroundColor(color);
                    }
                });
                animator.start();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
                    int color = (int) argbEvaluator
                            .evaluate(((float) progress) / seekBar.getMax(),
                                    colorStart, colorEnd);
                    textCurrentColor.setText(Integer.toHexString(color));
                    constraintLayout.setBackgroundColor(color);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
