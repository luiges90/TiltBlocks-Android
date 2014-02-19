package com.luiges90.tiltblocks;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.luiges90.tiltblocks.generator.BackwardGameGenerator;
import com.luiges90.tiltblocks.generator.ForwardGameGenerator;
import com.luiges90.tiltblocks.generator.GameGeneratorInterface;
import com.luiges90.tiltblocks.generator.GeneratorOption;

public class LevelEditorActivity extends Activity {

    private static class SolveTask extends AsyncTask<Void, Integer, char[]> {
        private ProgressDialog pd;

        private int stepLimit;
        private LevelEditorActivity activity;
        private GameField field;

        private int progress;

        public SolveTask(LevelEditorActivity activity, int stepLimit) {
            this.activity = activity;
            this.stepLimit = stepLimit;
            this.field = activity.field;
        }

        public void showDialog() {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            pd = new ProgressDialog(activity);
            pd.setMessage("Solving your level within " + stepLimit + " steps...");
            pd.setCancelable(true);
            pd.setIndeterminate(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(stepLimit);
            pd.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    EasyTracker.getInstance(activity).send(
                            MapBuilder.createEvent("level_editor", "solve", "quit early", null)
                                    .build());
                    cancel(true);
                    activity.solveTask = null;
                }

            });
            pd.show();

            publishProgress(progress);
        }

        @Override
        protected void onPreExecute() {
            progress = 0;
            showDialog();
        }

        protected void onProgressUpdate(Integer... values) {
            progress = values[0];
            pd.setProgress(values[0]);
        }

        @Override
        protected char[] doInBackground(Void... params) {
            return field.solve(new GameSolver.OnProgressListener() {

                @Override
                public void onProgressUpdate(int step) {
                    publishProgress(step);
                }
            });
        }

        @Override
        protected void onPostExecute(char[] result) {
            if (pd != null) {
                EasyTracker.getInstance(activity).send(
                        MapBuilder.createEvent("level_editor", "solve", "solved", null)
                                .build());

                pd.dismiss();
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                String msg = activity.getResources().getString(R.string.solution_of_level);

                if (result == null) {
                    msg = activity.getResources().getString(R.string.no_solution);
                } else if (result.length == 0) {
                    msg = activity.getResources().getString(R.string.not_enough_step, stepLimit);
                } else {
                    msg += Arrays.toString(result);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(msg);
                builder.show();
            }
            activity.solveTask = null;
        }
    }

    private static class GenerateTask extends AsyncTask<GeneratorOption, Void, GameField> {
        private ProgressDialog pd;

        private LevelEditorActivity activity;
        private GameField field;

        public GenerateTask(LevelEditorActivity activity) {
            this.activity = activity;
            this.field = activity.field;
        }

        public void showDialog() {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            pd = new ProgressDialog(activity);
            pd.setMessage("Generating your level...");
            pd.setCancelable(true);
            pd.setIndeterminate(true);
            pd.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    EasyTracker.getInstance(activity).send(
                            MapBuilder.createEvent("level_editor", "generate", "quit early", null)
                                    .build());
                    cancel(true);
                    activity.generateTask = null;
                }

            });
            pd.show();
        }

        @Override
        protected void onPreExecute() {
            showDialog();
        }

        @Override
        protected GameField doInBackground(GeneratorOption... params) {
            GameGeneratorInterface gg;

            if ((params[0].optimal && params[0].stepLo > 6) ||
                    (!params[0].optimal && params[0].stepLo > 10)) {
                gg = new ForwardGameGenerator();
            } else {
                gg = new BackwardGameGenerator();
            }

            return gg.generate(params[0]);
        }

        @Override
        protected void onPostExecute(GameField result) {
            if (pd != null) {
                EasyTracker.getInstance(activity).send(
                        MapBuilder.createEvent("level_editor", "generate", "generated", null)
                                .build());

                pd.dismiss();
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                field.setField(result);
                activity.setStepRemainText();

                activity.findViewById(R.id.gameView).postInvalidate();
            }
            activity.generateTask = null;
        }
    }

    private char currentType = BlockType.EMPTY;

    private static final int LOAD_LEVEL_REQUEST_CODE = 1;

    public static final String STATE_FIELD = "tiltblocks_field";
    public static final String STATE_GENERATOR_OPTION = "tiltblocks_generator_option";
    public static final String STATE_CURRENT_ELEMENT = "tiltblocks_current_element";

    public static final String INTENT_LOAD_DATA = "tiltblocks_load_level_data";

    public static final String FILE_PREFIX = "leveleditor";
    public static final String FILE_LAST = "last";

    private AnimatedGameField field;

    private SolveTask solveTask = null;
    private GenerateTask generateTask = null;

    private GeneratorOption lastOpt = new GeneratorOption();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.level_editor_help_title);
                builder.setMessage(R.string.level_editor_help);
                builder.create().show();
                return true;
            case R.id.action_report_problem:
                ProblemReporter.report(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable(STATE_FIELD, field);
        savedInstanceState.putSerializable(STATE_GENERATOR_OPTION, lastOpt);
        savedInstanceState.putChar(STATE_CURRENT_ELEMENT, currentType);
    }

    private void saveLevel(String filename, boolean showToasts) {
        int stepLimit = readStepLimit();

        if (stepLimit > 0) {
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(FILE_PREFIX + filename, Context.MODE_PRIVATE);
                fos.write(field.getLevelData().getBytes());
                if (showToasts) {
                    Toast.makeText(LevelEditorActivity.this,
                            getResources().getString(R.string.msg_saved_level),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (IOException ex) {
                if (showToasts) {
                    Toast.makeText(LevelEditorActivity.this,
                            getResources().getString(R.string.msg_fail_to_save_level),
                            Toast.LENGTH_SHORT).show();
                }
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    private void setStepRemainText() {
        EditText txStepRemain = (EditText) findViewById(R.id.tbStep);
        txStepRemain.setText(Integer.toString(field.getStepLimit()));
    }

    private void loadLevel(String levelData) {
        GameView gameView = (GameView) this.findViewById(R.id.gameView);
        field = new AnimatedGameField(gameView, levelData, 0);

        setStepRemainText();
    }

    protected void onDestroy() {
        saveLevel(FILE_LAST, false);
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == LOAD_LEVEL_REQUEST_CODE) {
            if (data.hasExtra(INTENT_LOAD_DATA)) {
                String levelData = data.getExtras().getString(INTENT_LOAD_DATA);
                loadLevel(levelData);
            }
        }
    }

    public Object onRetainNonConfigurationInstance() {
        if (solveTask != null) {
            solveTask.activity = null;
            return solveTask;
        } else if (generateTask != null) {
            generateTask.activity = null;
            return generateTask;
        }

        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_level_editor_horizontal);
        } else {
            setContentView(R.layout.activity_level_editor);
        }

        if (getLastNonConfigurationInstance() != null) {
            Object instance = getLastNonConfigurationInstance();

            if (instance instanceof SolveTask) {
                solveTask = (SolveTask) instance;
                solveTask.activity = this;
                solveTask.showDialog();
            } else if (instance instanceof GenerateTask) {
                generateTask = (GenerateTask) instance;
                generateTask.activity = this;
                generateTask.showDialog();
            }
        }

        GameView gameView = (GameView) this.findViewById(R.id.gameView);
        if (savedInstanceState == null) {
            FileInputStream fis = null;
            try {
                fis = openFileInput(FILE_PREFIX + FILE_LAST);
                byte[] buffer = new byte[GameField.getLevelDataLength()];
                fis.read(buffer);
                loadLevel(new String(buffer));
            } catch (Exception ex) {
                field = new AnimatedGameField(gameView);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }

        } else {
            field = (AnimatedGameField) savedInstanceState.getSerializable(STATE_FIELD);
            field.setGameView(gameView);

            lastOpt = (GeneratorOption) savedInstanceState.getSerializable(STATE_GENERATOR_OPTION);

            currentType = savedInstanceState.getChar(STATE_CURRENT_ELEMENT);
        }

        GridView gridview = (GridView) findViewById(R.id.gridElements);
        gridview.setAdapter(new ElementsAdapter());

        bindGameViewDrawer();

        bindButtons();
    }

    private int readStepLimit() {
        int stepLimit;

        EditText txStepRemain = (EditText) findViewById(R.id.tbStep);
        try {
            stepLimit = Integer.parseInt(txStepRemain.getText().toString());
        } catch (NumberFormatException ex) {
            txStepRemain.setError(getResources()
                    .getString(R.string.msg_invalid_input));
            return -1;
        }

        if (stepLimit <= 0) {
            txStepRemain.setError(getResources()
                    .getString(R.string.msg_invalid_input));
            return -1;
        }

        field.setStepLimit(stepLimit);

        return stepLimit;
    }

    private int readIntFromEditText(Dialog d, int viewId) {
        EditText et = (EditText) d.findViewById(viewId);
        String s = et.getText().toString();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            et.setError(getResources().getString(R.string.msg_invalid_input));
            throw ex;
        }
    }

    private void bindButtons() {
        Button save = (Button) this.findViewById(R.id.btnSave);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EasyTracker.getInstance(LevelEditorActivity.this).send(
                        MapBuilder.createEvent("level_editor", "ui_button", "save", null)
                                .build());
                String fileName = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.US)
                        .format(Calendar.getInstance().getTime());
                saveLevel(fileName, true);
            }

        });

        Button load = (Button) this.findViewById(R.id.btnLoad);
        load.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getInstance(LevelEditorActivity.this).send(
                        MapBuilder.createEvent("level_editor", "ui_button", "load", null)
                                .build());
                Intent intent = new Intent(LevelEditorActivity.this, LevelEditorLoadActivity.class);
                startActivityForResult(intent, LOAD_LEVEL_REQUEST_CODE);
            }
        });

        Button generate = (Button) this.findViewById(R.id.btnGenerate);
        generate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EasyTracker.getInstance(LevelEditorActivity.this).send(
                        MapBuilder.createEvent("level_editor", "ui_button", "generate", null)
                                .build());

                final Dialog d = new Dialog(LevelEditorActivity.this);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);

                d.setContentView(R.layout.generator_options_dialog);

                d.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }

                });

                d.findViewById(R.id.btnDialogGenerate).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        GeneratorOption opt = new GeneratorOption();

                        try {
                            opt.stepLo = readIntFromEditText(d, R.id.tbStepLo);
                            opt.stepHi = readIntFromEditText(d, R.id.tbStepHi);
                            opt.colorLo = readIntFromEditText(d, R.id.tbColorLo);
                            opt.colorHi = readIntFromEditText(d, R.id.tbColorHi);
                        } catch (NumberFormatException ex) {
                            return;
                        }
                        if (opt.stepLo > opt.stepHi) {
                            int temp = opt.stepLo;
                            opt.stepLo = opt.stepHi;
                            opt.stepHi = temp;
                        }
                        if (opt.colorLo > opt.colorHi) {
                            int temp = opt.colorLo;
                            opt.colorLo = opt.colorHi;
                            opt.colorHi = temp;
                        }
                        if (opt.colorHi > 4) {
                            opt.colorHi = 4;
                        }
                        opt.stone = ((CheckBox) d.findViewById(R.id.cbStone)).isChecked();
                        opt.rainbow = ((CheckBox) d.findViewById(R.id.cbRainbow)).isChecked();
                        opt.arrow = ((CheckBox) d.findViewById(R.id.cbArrow)).isChecked();
                        opt.sticky = ((CheckBox) d.findViewById(R.id.cbSticky)).isChecked();
                        opt.gate = ((CheckBox) d.findViewById(R.id.cbGate)).isChecked();
                        opt.shift = ((CheckBox) d.findViewById(R.id.cbColorShift)).isChecked();
                        opt.nomatch = ((CheckBox) d.findViewById(R.id.cbNoMatchArea)).isChecked();
                        opt.wrap = ((CheckBox) d.findViewById(R.id.cbWrap)).isChecked();

                        opt.optimal = ((CheckBox) d.findViewById(R.id.cbOptimal)).isChecked();

                        lastOpt = opt;

                        generateTask = new GenerateTask(LevelEditorActivity.this);
                        generateTask.execute(opt);

                        d.dismiss();
                    }

                });

                ((EditText) d.findViewById(R.id.tbStepLo)).setText(Integer.toString(lastOpt.stepLo));
                ((EditText) d.findViewById(R.id.tbStepHi)).setText(Integer.toString(lastOpt.stepHi));
                ((EditText) d.findViewById(R.id.tbColorLo)).setText(Integer
                        .toString(lastOpt.colorLo));
                ((EditText) d.findViewById(R.id.tbColorHi)).setText(Integer
                        .toString(lastOpt.colorHi));
                ((CheckBox) d.findViewById(R.id.cbStone)).setChecked(lastOpt.stone);
                ((CheckBox) d.findViewById(R.id.cbRainbow)).setChecked(lastOpt.rainbow);
                ((CheckBox) d.findViewById(R.id.cbArrow)).setChecked(lastOpt.arrow);
                ((CheckBox) d.findViewById(R.id.cbSticky)).setChecked(lastOpt.sticky);
                ((CheckBox) d.findViewById(R.id.cbGate)).setChecked(lastOpt.gate);
                ((CheckBox) d.findViewById(R.id.cbColorShift)).setChecked(lastOpt.shift);
                ((CheckBox) d.findViewById(R.id.cbNoMatchArea)).setChecked(lastOpt.nomatch);
                ((CheckBox) d.findViewById(R.id.cbWrap)).setChecked(lastOpt.wrap);
                ((CheckBox) d.findViewById(R.id.cbOptimal)).setChecked(lastOpt.optimal);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(d.getWindow().getAttributes());
                lp.width = Utility.dpToPx(LevelEditorActivity.this, 300);

                d.show();
                d.getWindow().setAttributes(lp);
            }

        });

        Button solve = (Button) this.findViewById(R.id.btnSolve);
        solve.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                EasyTracker.getInstance(LevelEditorActivity.this).send(
                        MapBuilder.createEvent("level_editor", "ui_button", "solve", null)
                                .build());

                final int stepLimit = readStepLimit();

                if (stepLimit > 0) {
                    solveTask = new SolveTask(LevelEditorActivity.this, stepLimit);
                    solveTask.execute();
                }
            }
        });

        Button play = (Button) this.findViewById(R.id.btnPlay);
        play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EasyTracker.getInstance(LevelEditorActivity.this).send(
                        MapBuilder.createEvent("level_editor", "ui_button", "play", null)
                                .build());

                int stepLimit = readStepLimit();

                if (stepLimit > 0) {
                    Intent intent = new Intent(LevelEditorActivity.this, GameActivity.class);
                    intent.putExtra(GameActivity.INTENT_FIELD, field.getLevelData());
                    startActivity(intent);
                }
            }

        });

        Button menu = (Button) this.findViewById(R.id.btnEditorMenu);
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LevelEditorActivity.this.finish();
            }

        });
    }

    private void bindGameViewDrawer() {
        final GameView view = (GameView) this.findViewById(R.id.gameView);

        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_MOVE) {
                    float x = event.getX();
                    float y = event.getY();
                    int r = (int) (y / view.getBlockHeight());
                    int c = (int) (x / view.getBlockWidth());
                    field.setBlockAt(r, c, currentType);
                    view.postInvalidate();
                }
                return true;
            }
        });
    }

    public class ElementsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return BlockType.ALL_CODES.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        private Drawable getDrawableAt(int position) {
            int id = BlockType.getDrawableId(BlockType.ALL_CODES[position]);
            Drawable background;
            if (id > 0) {
                background = getResources().getDrawable(id);
            } else {
                background = getResources().getDrawable(R.drawable.black_shape);
            }

            if (BlockType.ALL_CODES[position] == currentType) {
                return new LayerDrawable(
                        new Drawable[] {
                                background,
                                getResources().getDrawable(R.drawable.active_element)
                        });
            } else {
                return background;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageButton button;

            final char[] allTypes = BlockType.ALL_CODES;
            final GridView grid = (GridView) findViewById(R.id.gridElements);

            if (convertView == null) {
                button = new ImageButton(LevelEditorActivity.this);

                int elementSize = Utility.dpToPx(LevelEditorActivity.this, 48);

                button.setLayoutParams(new GridView.LayoutParams(elementSize, elementSize));
            } else {
                button = (ImageButton) convertView;
            }

            Utility.setBackgroundDrawable(button, getDrawableAt(position));

            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < grid.getChildCount(); ++i) {
                        Drawable old = grid.getChildAt(i).getBackground();
                        if (old instanceof LayerDrawable) {
                            Utility.setBackgroundDrawable(grid.getChildAt(i),
                                    ((LayerDrawable) old).getDrawable(0));
                        }
                    }

                    currentType = allTypes[position];

                    Utility.setBackgroundDrawable(v, getDrawableAt(position));
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
