package com.luiges90.tiltblocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

public class Utility {

    public static final String AD_MOB_ID = "a152b9366a16519";

    private Utility() {
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getScreenSize(Context context) {
        Display disp = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            disp.getSize(size);
            return size;
        } else {
            int w, h;
            w = disp.getWidth();
            h = disp.getHeight();
            return new Point(w, h);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void setBackgroundDrawable(View v, Drawable d) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            v.setBackground(d);
        } else {
            v.setBackgroundDrawable(d);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void removeOnGlobalLayoutListener(ViewTreeObserver ob,
            ViewTreeObserver.OnGlobalLayoutListener l) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            ob.removeOnGlobalLayoutListener(l);
        } else {
            ob.removeGlobalOnLayoutListener(l);
        }
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context
                .getResources()
                .getDisplayMetrics());
    }

}
