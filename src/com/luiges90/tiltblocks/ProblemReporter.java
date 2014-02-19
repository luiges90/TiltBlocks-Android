package com.luiges90.tiltblocks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.luiges90.tiltblocks.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

public class ProblemReporter {

    private static final String SCREENSHOT_FILE_NAME = "report_screenshot.png";

    private ProblemReporter() {
    }

    private static void createScreenshot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap screenshot = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();

        FileOutputStream fos;
        try {
            fos = activity.openFileOutput(SCREENSHOT_FILE_NAME, Context.MODE_WORLD_READABLE);
            if (null != fos)
            {
                screenshot.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (IOException ex) {

        }
    }

    private static String composeBody(Activity activity) {
        StringBuilder sb = new StringBuilder();

        sb.append(activity.getString(R.string.report_email_content));

        sb.append(activity.getString(R.string.report_model) + ":" +
                Build.MANUFACTURER + " " + Build.MODEL + "\n");
        sb.append(activity.getString(R.string.report_android_version) + ":" +
                Build.VERSION.RELEASE + "\n");
        PackageInfo pInfo;
        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            sb.append(activity.getString(R.string.report_app_version) + ":" + pInfo.versionName
                    + "\n");
        } catch (NameNotFoundException e) {
        }

        return sb.toString();
    }

    public static void report(Activity activity) {
        EasyTracker.getInstance(activity).send(
                MapBuilder.createEvent("report", "report", "access report issue button", null)
                        .build());

        createScreenshot(activity);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { "luiges90@gmail.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.report_email_title));
        i.putExtra(Intent.EXTRA_TEXT, composeBody(activity));

        String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
        Uri uri = Uri.fromFile(new File(sdCard +
                new String(new char[sdCard.replaceAll("[^/]", "").length()])
                        .replace("\0", "/..") + activity.getFilesDir() + "/"
                + SCREENSHOT_FILE_NAME));
        i.putExtra(android.content.Intent.EXTRA_STREAM, uri);

        try {
            activity.startActivity(Intent.createChooser(i,
                    activity.getString(R.string.report_problem_dialog_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, activity.getString(R.string.report_msg_no_email_client),
                    Toast.LENGTH_SHORT).show();
        }
    }

}
