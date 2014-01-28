package com.luiges90.tiltblocks;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class LevelEditorLoadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_editor_load);
        
        ((GridView) this.findViewById(R.id.gridCustomLevel)).setAdapter(new CustomLevelAdapter());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.level_load_action, menu);
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

    public class CustomLevelAdapter extends BaseAdapter {
        
        private String getFileNameAtPosition(int position) {
            String[] fileList = fileList();
            
            int index = 0;
            for (int i = 0; i < fileList.length; ++i) {
                if (!fileList[i].startsWith(LevelEditorActivity.FILE_PREFIX)) continue;
                if (fileList[i].equals(LevelEditorActivity.FILE_PREFIX + LevelEditorActivity.FILE_LAST)) continue;
                if (index == position) return fileList[i];
                index++;
            }
            
            return null;
        }

        @Override
        public int getCount() {
            String[] fileList = fileList();
            int cnt = 0;
            for (int i = 0; i < fileList.length; ++i) {
                if (!fileList[i].startsWith(LevelEditorActivity.FILE_PREFIX)) continue;
                if (fileList[i].equals(LevelEditorActivity.FILE_PREFIX + LevelEditorActivity.FILE_LAST)) continue;
                cnt++;
            }
            return cnt;
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
            final String filename = getFileNameAtPosition(position);
            
            GameView view;
            if (convertView == null) {
                view = new GameView(LevelEditorLoadActivity.this);
            } else {
                view = (GameView) convertView;
            }
            
            FileInputStream fis = null;
            byte[] buffer = null;
            try {
                fis = openFileInput(filename);
                buffer = new byte[GameField.getLevelDataLength()];
                fis.read(buffer);
            } catch (IOException e) {
                Toast.makeText(LevelEditorLoadActivity.this, 
                        getResources().getString(R.string.msg_fail_to_load_level), Toast.LENGTH_LONG).show();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            
            if (buffer != null) {
                final AnimatedGameField field = new AnimatedGameField(view, new String(buffer), 0);
                
                view.setOnClickListener(new OnClickListener() {
    
                    @Override
                    public void onClick(View v) {
                        Intent data = new Intent();
                        data.putExtra(LevelEditorActivity.INTENT_LOAD_DATA, field.getLevelData());
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    
                });
                
                view.setOnLongClickListener(new OnLongClickListener(){

                    @Override
                    public boolean onLongClick(final View v) {
                        new AlertDialog.Builder(LevelEditorLoadActivity.this)
                            .setMessage(getResources().getString(R.string.msg_are_you_sure_to_delete_level))
                            .setPositiveButton(R.string.delete_level, new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteFile(filename);
                                    v.setVisibility(View.INVISIBLE);
                                    Toast.makeText(LevelEditorLoadActivity.this, 
                                            getResources().getString(R.string.msg_deleted_level), 
                                            Toast.LENGTH_LONG).show();
                                }
                                
                            }).setNegativeButton(R.string.cancel, null).show();
                            
                        return true;
                    }
                    
                });
            }
            
            return view;
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
