package com.luiges90.tiltblocks;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class LevelSelectActivity extends Activity {

    private Progress progress;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.level_select_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        setContentView(R.layout.activity_level_select);

        this.progress = Progress.instance(this);

        GridView gridview = (GridView) findViewById(R.id.gridLevels);
        gridview.setAdapter(new LevelAdapter());
    }

    public class LevelAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return GameField.LEVEL_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Button button;

            if (convertView == null) {
                button = new Button(LevelSelectActivity.this);
                button.setBackgroundResource(0);
            } else {
                button = (Button) convertView;
            }

            if (progress.getCurrentLevel() > position) {
                button.setBackgroundResource(R.drawable.level_select_cleared);
                button.setEnabled(true);
            } else if (progress.getCurrentLevel() == position) {
                button.setBackgroundResource(R.drawable.level_select_current);
                button.setEnabled(true);
            } else {
                button.setBackgroundResource(R.drawable.level_select_disabled);
                button.setEnabled(false);
            }

            ColorStateList csl = null;
            try {
                XmlResourceParser xrp = getResources().getXml(R.drawable.level_select_text_colors);
                csl = ColorStateList.createFromXml(getResources(), xrp);
            } catch (Exception e) {
            }
            if (csl != null) {
                button.setTextColor(csl);
            }

            button.setText(GameField.getLevelNameString(position));
            button.setGravity(Gravity.CENTER);
            button.setPadding(0, button.getPaddingTop(), 0, button.getPaddingBottom());
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LevelSelectActivity.this, GameActivity.class);
                    intent.putExtra(GameActivity.INTENT_SET_LEVEL, position);
                    startActivity(intent);
                    LevelSelectActivity.this.finish();
                }

            });

            return button;
        }

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
