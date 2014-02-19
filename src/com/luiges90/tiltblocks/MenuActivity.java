package com.luiges90.tiltblocks;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

public class MenuActivity extends Activity {

    public static final String TRACK_PREF_KEY = "ga_tracking";

    private Button btnClear;
    private Progress progress;

    private class ClearProgress implements OnClickListener {

        private Toast toast;

        @Override
        public void onClick(View arg0) {
            progress.clearProgress();

            toast = Toast.makeText(MenuActivity.this,
                    MenuActivity.this.getResources().getString(R.string.cleared_progress),
                    Toast.LENGTH_SHORT);
            toast.show();

            btnClear.setText(MenuActivity.this.getResources().getString(R.string.restore_progress));
            btnClear.setOnClickListener(new RestoreProgress());
        }
    }

    private class RestoreProgress implements OnClickListener {

        private Toast toast;

        @Override
        public void onClick(View arg0) {
            progress.restoreProgress();

            toast = Toast.makeText(MenuActivity.this,
                    MenuActivity.this.getResources().getString(R.string.restored_progress),
                    Toast.LENGTH_SHORT);
            toast.show();

            btnClear.setText(MenuActivity.this.getResources().getString(R.string.clear_progress));
            btnClear.setOnClickListener(new ClearProgress());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_report_problem:
                ProblemReporter.report(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(
                        new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(
                                    SharedPreferences sharedPreferences,
                                    String key) {
                                if (key.equals(TRACK_PREF_KEY)) {
                                    GoogleAnalytics.getInstance(getApplicationContext())
                                            .setAppOptOut(
                                                    sharedPreferences.getBoolean(key, false));
                                }
                            }
                        });

        final Button start = (Button) this.findViewById(R.id.btnStartGame);

        start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MenuActivity.this, GameActivity.class);
                startActivity(intent);
            }

        });

        progress = Progress.instance(this);

        btnClear = (Button) this.findViewById(R.id.btnClearProgress);
        btnClear.setOnClickListener(new ClearProgress());

        final Button select = (Button) this.findViewById(R.id.btnLevelSelect);
        select.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MenuActivity.this, LevelSelectActivity.class);
                startActivity(intent);
            }
        });

        final Button editor = (Button) this.findViewById(R.id.btnLevelEditor);
        editor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MenuActivity.this, LevelEditorActivity.class);
                startActivity(intent);
            }
        });

        final ViewGroup content = (ViewGroup) findViewById(android.R.id.content);
        final ViewGroup root = (ViewGroup) findViewById(R.id.menuRoot);
        content.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Utility.removeOnGlobalLayoutListener(content.getViewTreeObserver(), this);

                        View last = findViewById(R.id.btnLevelEditor);
                        int bottom = last.getBottom() + last.getPaddingBottom()
                                + last.getPaddingTop();

                        int totalHeight = root.getBottom() - root.getPaddingBottom()
                                - root.getPaddingTop();

                        if (bottom > totalHeight) {

                            for (int i = 0; i < root.getChildCount(); ++i) {
                                View v = root.getChildAt(i);
                                ViewGroup.LayoutParams vglp = v.getLayoutParams();
                                if (vglp instanceof LinearLayout.LayoutParams) {
                                    LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) vglp;
                                    lllp.bottomMargin /= 3;
                                    lllp.topMargin /= 3;
                                    v.setLayoutParams(lllp);
                                }
                            }

                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

}
