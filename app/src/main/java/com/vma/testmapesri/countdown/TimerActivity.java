package com.vma.testmapesri.countdown;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.skyfishjy.library.RippleBackground;
import com.vma.testmapesri.R;

public class TimerActivity extends AppCompatActivity {
    TextView txvw_count;
    TranslateAnimation animation;
    RippleBackground rple_center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        txvw_count = findViewById(R.id.txvw_count);
        rple_center = findViewById(R.id.rple_center);

        animation = new TranslateAnimation(0.0f, 0.0f, 200.0f, 0.0f);
        animation.setDuration(300);

        new CountDownTimer(10000,1000){
            @Override
            public void onTick(long l) {
                txvw_count.clearAnimation();
                txvw_count.startAnimation(animation);
                rple_center.startRippleAnimation();
                int x = (int) (l/1000);
                txvw_count.setText((x+1)+"");
            }

            @Override
            public void onFinish() {
                txvw_count.setText("F");
                rple_center.stopRippleAnimation();

            }
        }.start();
    }


}