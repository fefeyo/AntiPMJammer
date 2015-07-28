package com.sox.android.antipm;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private ImageView mKeikoku;
    private TextView mTimer, title;
    private ImageButton mTinButton;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private long count;

    private long now;

    private boolean isRun;

    private Handler mHandler;

    private Runnable showCount = new Runnable() {
        @Override
        public void run() {
            checkLimit();
            final Date d = new Date();
            d.setTime(count);
            Log.i("count", count + "");
            final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS", Locale.JAPAN);
            final String time = sdf.format(d);
            final StringBuilder builder = new StringBuilder(time);
            builder.setCharAt(time.length() - 1, ' ');

            mTimer.setText(builder.toString());
        }
    };


    private Runnable counter = new Runnable() {
        @Override
        public void run() {
            final long tmp = System.currentTimeMillis();
            count = count - (tmp - now);
            now = tmp;
            runOnUiThread(showCount);
            mHandler.postDelayed(this, 10);
        }
    };

    private void initialize() {
        mKeikoku = (ImageView) findViewById(R.id.keikoku);
        mKeikoku.setVisibility(View.GONE);
        mTimer = (TextView) findViewById(R.id.timer);
        title = (TextView) findViewById(R.id.title);
        mTinButton = (ImageButton) findViewById(R.id.tin_button);
        mTinButton.setOnClickListener(this);
        mHandler = new Handler();
        isRun = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("ANTI　PM　TIMER");
        initialize();
        setAntiPM();
    }

    private void setAntiPM() {
        mPreferences = getSharedPreferences("anti_pm", MODE_PRIVATE);
        mEditor = mPreferences.edit();
        courseOfDay();
        mEditor.apply();
        count = mPreferences.getLong("count", 0);
        final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS", Locale.JAPAN);
        final Date d = new Date();
        d.setTime(count);
        final String time = sdf.format(d);
        final StringBuilder builder = new StringBuilder(time);
        builder.setCharAt(time.length() - 1, ' ');
        checkLimit();
        mTimer.setText(builder.toString());
    }

    /**
     * 一日経過していれば3分にもどす
     */
    private void courseOfDay() {
        final Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        if (mPreferences.getBoolean("check", true)) {
            mEditor.putInt("year", now.get(Calendar.YEAR));
            mEditor.putInt("month", now.get(Calendar.MONTH));
            mEditor.putInt("day", now.get(Calendar.DATE));
            mEditor.putLong("count", 180000);
            mEditor.putBoolean("check", false);
        }

        final Calendar pref = Calendar.getInstance();
        pref.set(mPreferences.getInt("year", 0), mPreferences.getInt("month", 0), mPreferences.getInt("day", 0));

        final long diff = now.getTimeInMillis() - pref.getTimeInMillis();
        if (diff / 1000 > 86400) {
            mEditor.putBoolean("check", true);
        }
    }

    @Override
    public void onClick(View v) {
        if(isRun){
            mHandler.removeCallbacks(counter);
            isRun = false;
        }else{
            now = System.currentTimeMillis();
            mHandler.postDelayed(counter, 0);
            isRun = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEditor = mPreferences.edit();
        mEditor.putLong("count", count);
        mEditor.apply();
    }

    private void checkLimit() {
        if (count < 0) {
            mKeikoku.setVisibility(View.VISIBLE);
            mTimer.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            mTinButton.setVisibility(View.GONE);
            if (mHandler != null) mHandler.removeCallbacks(counter);
        }
    }
}