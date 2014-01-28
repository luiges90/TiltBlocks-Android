package com.luiges90.tiltblocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import android.content.Context;
import android.util.Log;

public class Progress {

    public static final String PROGRESS_FILE = "tiltblocks_progress";
    
    private int currentLevel;
    private int storedProgress;
    private Context context;
    
    private File getFile() {
        return new File(context.getFilesDir(), PROGRESS_FILE);
    }
    
    private static Progress instance;
    public static Progress instance(Context context) {
        if (instance == null) {
            instance = new Progress(context);
        }
        return instance;
    }

    private Progress(Context context) {
        this.context = context;
        File file = getFile();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            currentLevel = Integer.parseInt(br.readLine());
            br.close();
        } catch (IOException ex) {
            EasyTracker.getInstance(context).send(
                    MapBuilder.createEvent("progress", "status", "no progress found", null)
                        .build());
            currentLevel = 0;
            saveProgress();
        } catch (NumberFormatException ex) {
            EasyTracker.getInstance(context).send(
                    MapBuilder.createEvent("progress", "status", "no progress found", null)
                        .build());
            currentLevel = 0;
            saveProgress();
        }
    }
    
    private void saveProgress() {
        try {
            File file = getFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(Integer.toString(currentLevel));
            bw.close();
        } catch (IOException e) {
            Log.e(GameActivity.LOG_TAG, "Unable to save progress", e);
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void advanceLevel(int newLevel) {
        if (newLevel > currentLevel) {
            EasyTracker.getInstance(context).send(
                    MapBuilder.createEvent("progress", "progress", "reached level " + newLevel, null)
                        .build());
            currentLevel = newLevel;
            saveProgress();
        }
    }
    
    public boolean clearProgress() {
        EasyTracker.getInstance(context).send(
                MapBuilder.createEvent("progress", "progress", " progress cleared", null)
                    .build());
        storedProgress = currentLevel;
        currentLevel = 0;
        File file = getFile();
        return file.delete();
    }
    
    public void restoreProgress() {
        EasyTracker.getInstance(context).send(
                MapBuilder.createEvent("progress", "progress", " progress restored", null)
                    .build());
        currentLevel = storedProgress;
        saveProgress();
    }

}
