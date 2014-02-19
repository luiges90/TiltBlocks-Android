package com.luiges90.tiltblocks;

import java.io.IOException;
import java.io.InputStream;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameActivity extends Activity {

    public static final String LOG_TAG = "GAME_LOG";
    public static final String INTENT_SET_LEVEL = "playLevel";
    public static final String INTENT_FIELD = "field";

    public static final String STATE_LEVEL = "tiltblocks_level";
    public static final String STATE_FIELD = "tiltblocks_field";
    public static final String STATE_GAME_STATE = "tiltblocks_gamestate";

    public static final int GAME_STATE_PLAYING = 0;
    public static final int GAME_STATE_FAILED = -1;
    public static final int GAME_STATE_CLEARED = 1;

    private AnimatedGameField field;

    private Progress progress;
    private int level;

    private int gameState = GAME_STATE_PLAYING;

    private void unexpectedErrorOccurred(String msg, Exception e) {
        Log.e(LOG_TAG, msg, e);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.app_name);
        b.setMessage(R.string.internal_error);

        AlertDialog dialog = b.create();

        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                GameActivity.this.finish();
            }

        });

        dialog.show();
    }

    private boolean isInEditor() {
        return this.getIntent().getStringExtra(INTENT_FIELD) != null;
    }

    private void loadLevel(int level) {
        InputStream is = null;
        try {
            String loadField = this.getIntent().getStringExtra(INTENT_FIELD);

            if (loadField == null) {
                String fileName = GameField.getLevelNameString(level);

                is = this.getAssets().open("levels/" + fileName + ".txt",
                        AssetManager.ACCESS_BUFFER);
                byte[] levelData = new byte[GameField.getLevelDataLength()];
                is.read(levelData);

                loadField = new String(levelData);
            }

            field = new AnimatedGameField(this.getGameView(), loadField, level);
            updateText();
            setArrowDrawable();
        } catch (IOException e) {
            unexpectedErrorOccurred("Unable to load level. ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void setArrowDrawable() {
        final ImageButton up = (ImageButton) this.findViewById(R.id.btnUp);
        final ImageButton down = (ImageButton) this.findViewById(R.id.btnDown);
        final ImageButton left = (ImageButton) this.findViewById(R.id.btnLeft);
        final ImageButton right = (ImageButton) this.findViewById(R.id.btnRight);

        if (field.getLastStep() == GameField.UP) {
            up.setImageDrawable(getResources().getDrawable(R.drawable.up_button_done));
        } else {
            up.setImageDrawable(getResources().getDrawable(R.drawable.up_button));
        }

        if (field.getLastStep() == GameField.DOWN) {
            down.setImageDrawable(getResources().getDrawable(R.drawable.down_button_done));
        } else {
            down.setImageDrawable(getResources().getDrawable(R.drawable.down_button));
        }

        if (field.getLastStep() == GameField.LEFT) {
            left.setImageDrawable(getResources().getDrawable(R.drawable.left_button_done));
        } else {
            left.setImageDrawable(getResources().getDrawable(R.drawable.left_button));
        }

        if (field.getLastStep() == GameField.RIGHT) {
            right.setImageDrawable(getResources().getDrawable(R.drawable.right_button_done));
        } else {
            right.setImageDrawable(getResources().getDrawable(R.drawable.right_button));
        }

    }

    private void showDialogAfterMove(long delay) {
        if (field.checkComplete()) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    showClearedDialog();
                }
            }, delay);
        } else if (field.checkFailure()) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    showFailedDialog();
                }
            }, delay);
        }
    }

    private void bindArrows() {
        final ImageButton up = (ImageButton) this.findViewById(R.id.btnUp);
        final ImageButton down = (ImageButton) this.findViewById(R.id.btnDown);
        final ImageButton left = (ImageButton) this.findViewById(R.id.btnLeft);
        final ImageButton right = (ImageButton) this.findViewById(R.id.btnRight);

        up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!field.isAnimationRunning() && field.getStepRemain() > 0) {
                    long delay = field.moveUp();
                    updateText();
                    setArrowDrawable();
                    showDialogAfterMove(delay);
                }
            }
        });

        down.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!field.isAnimationRunning() && field.getStepRemain() > 0) {
                    long delay = field.moveDown();
                    updateText();
                    setArrowDrawable();
                    showDialogAfterMove(delay);
                }
            }
        });

        left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!field.isAnimationRunning() && field.getStepRemain() > 0) {
                    long delay = field.moveLeft();
                    updateText();
                    setArrowDrawable();
                    showDialogAfterMove(delay);
                }
            }
        });

        right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!field.isAnimationRunning() && field.getStepRemain() > 0) {
                    long delay = field.moveRight();
                    updateText();
                    setArrowDrawable();
                    showDialogAfterMove(delay);
                }
            }
        });
    }

    private void bindButtons() {
        Button retry = (Button) this.findViewById(R.id.btnRetry);
        Button menu = (Button) this.findViewById(R.id.btnMenu);

        retry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loadLevel(level);
            }

        });

        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                GameActivity.this.finish();
            }

        });
    }

    private void updateText() {
        TextView level = (TextView) this.findViewById(R.id.txLevel);
        if (level != null) {
            level.setText(field.getLevelNameString());
        }

        TextView step = (TextView) this.findViewById(R.id.txStepRemain);
        step.setText(field.getStepRemain() + "/" + field.getStepLimit());
        if (field.getStepRemain() <= 2) {
            step.setTextColor(Color.RED);
        } else {
            step.setTextColor(Color.WHITE);
        }
    }

    private void showClearedDialog() {
        gameState = GAME_STATE_CLEARED;

        boolean beaten = level + 1 >= GameField.LEVEL_COUNT;
        if (beaten && !isInEditor()) {
            level++;
            progress.advanceLevel(level);
            Intent intent = new Intent(this, GameBeatenActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cleared_dialog);

        ((TextView) dialog.findViewById(R.id.txClearedStepRemain)).setText(
                getResources().getString(R.string.steps_remain) + " " + field.getStepRemain()
                );

        dialog.findViewById(R.id.btnClearedRetry).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                loadLevel(level);
                gameState = GAME_STATE_PLAYING;
                if (!isInEditor()) {
                    progress.advanceLevel(level + 1);
                }
                dialog.dismiss();
            }

        });

        Button nextLevel = (Button) dialog.findViewById(R.id.btnNextLevel);
        if (isInEditor()) {
            nextLevel.setVisibility(View.GONE);
        } else {
            nextLevel.setVisibility(View.VISIBLE);
            nextLevel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!isInEditor()) {
                        level++;
                        progress.advanceLevel(level);
                    }
                    loadLevel(level);
                    gameState = GAME_STATE_PLAYING;
                    dialog.dismiss();
                }

            });
        }

        Button menu = (Button) dialog.findViewById(R.id.btnClearedMenu);
        menu.setText(
                isInEditor() ? this.getResources().getString(R.string.back) :
                        this.getResources().getString(R.string.menu)
                );
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!isInEditor()) {
                    level++;
                    progress.advanceLevel(level);
                }
                dialog.dismiss();
                GameActivity.this.finish();
            }

        });

        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (!isInEditor()) {
                    level++;
                    progress.advanceLevel(level);
                    loadLevel(level);
                    gameState = GAME_STATE_PLAYING;
                } else {
                    dialog.dismiss();
                    GameActivity.this.finish();
                }
            }

        });

        dialog.show();
    }

    private void showFailedDialog() {
        gameState = GAME_STATE_FAILED;

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fail_dialog);

        dialog.findViewById(R.id.btnFailedRetry).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                loadLevel(level);
                gameState = GAME_STATE_PLAYING;
                dialog.dismiss();
            }

        });

        Button menu = (Button) dialog.findViewById(R.id.btnFailedMenu);
        menu.setText(
                isInEditor() ? this.getResources().getString(R.string.back) :
                        this.getResources().getString(R.string.menu)
                );
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                GameActivity.this.finish();
            }

        });

        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                loadLevel(level);
                gameState = GAME_STATE_PLAYING;
            }

        });

        dialog.show();
    }

    private GameView gameView = null;

    public GameView getGameView() {
        if (gameView != null)
            return gameView;
        gameView = (GameView) this.findViewById(R.id.gameView);
        return gameView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable(STATE_FIELD, field);
        savedInstanceState.putInt(STATE_GAME_STATE, gameState);
        savedInstanceState.putInt(STATE_LEVEL, level);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                showHintDialog();
                return true;
            case R.id.action_report_problem:
                ProblemReporter.report(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHintDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.hint_dialog);
        dialog.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_game_horizontal);
        } else {
            setContentView(R.layout.activity_game);
        }

        progress = Progress.instance(this);

        if (savedInstanceState == null) {
            level = this.getIntent().getIntExtra(INTENT_SET_LEVEL, progress.getCurrentLevel());
            if (level >= GameField.LEVEL_COUNT) {
                level = 0;
            }
            loadLevel(level);

            if (progress.getCurrentLevel() == 0 && !isInEditor()) {
                showHintDialog();
            }
        } else {
            level = savedInstanceState.getInt(STATE_LEVEL);

            field = (AnimatedGameField) savedInstanceState.getSerializable(STATE_FIELD);
            field.setGameView(this.getGameView());
            updateText();

            setArrowDrawable();

            gameState = savedInstanceState.getInt(STATE_GAME_STATE);
            if (gameState == GAME_STATE_CLEARED) {
                showClearedDialog();
            } else if (gameState == GAME_STATE_FAILED) {
                showFailedDialog();
            }
        }

        if (isInEditor()) {
            Button menu = (Button) this.findViewById(R.id.btnMenu);
            menu.setText(getResources().getString(R.string.back));
        }

        bindArrows();
        bindButtons();
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
