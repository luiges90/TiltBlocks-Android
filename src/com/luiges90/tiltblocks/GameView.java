package com.luiges90.tiltblocks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GameView extends View {
    
    private GameField field;

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet as) {
        super(context, as);
    }

    public GameView(Context context, AttributeSet as, int i) {
        super(context, as, i);
    }
    
    public void setField(GameField field) {
        this.field = field;
    }

    private Rect src = new Rect();
    private RectF dest = new RectF();
    private Paint paint = new Paint();
    
    public float getBlockWidth() {
        return this.getWidth() / GameField.BOARD_SIZE;
    }
    
    public float getBlockHeight() {
        return this.getHeight() / GameField.BOARD_SIZE;
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        Context context = getContext();
        
        if (field == null) return;

        float hSize = getBlockWidth();
        float vSize = getBlockHeight();

        for (Block block : field.getAllBlocks()) {
            Bitmap bitmap = block.getBitmap(context);
            src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            dest.set(hSize * block.getDrawCol(), vSize * block.getDrawRow(),
                    hSize * block.getDrawCol() + hSize, vSize * block.getDrawRow() + vSize);

            paint.setAlpha((int) (block.getOpacity() * 255));

            canvas.drawBitmap(bitmap, src, dest, paint);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            width = Math.min(widthSize, heightSize);
            height = Math.min(widthSize, heightSize);
        } else if (widthMode == MeasureSpec.EXACTLY) {
            width = height = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            width = height = heightSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            if (heightMode == MeasureSpec.AT_MOST) {
                width = height = Math.min(widthSize, heightSize);
            } else { // heightMode == MeasureSpec.UNSPECIFIED
                width = height = widthSize;
            }
        } else { // widthMode == MeasureSpec.UNSPECIFIED
            width = height = 240;
        }

        setMeasuredDimension(width, height);
    }

}
