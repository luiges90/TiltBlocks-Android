package com.luiges90.tiltblocks;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class GameBeatenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.game_beaten_fadein, R.anim.game_beaten_fadeout);
        setContentView(R.layout.activity_game_beaten);
        
        this.findViewById(R.id.btnClearedMenu).setOnClickListener(new OnClickListener(){
            
            @Override
            public void onClick(View arg0) {
                GameBeatenActivity.this.finish();
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
