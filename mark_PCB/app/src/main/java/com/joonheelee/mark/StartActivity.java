package com.joonheelee.mark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by joonheelee on 05/11/2017.
 */

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Handler mHandler = new Handler();

        mHandler.postDelayed(new Runnable()  {
            public void run() {

                Intent intent=new Intent(StartActivity.this,MainPageActivity.class);
                startActivity(intent);

            }
        }, 1500);

    }

}