package com.luiges90.tiltblocks;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class BlockType {

    public static final char EMPTY = '.';
    public static final char BLOCK_1 = '1';
    public static final char BLOCK_2 = '2';
    public static final char BLOCK_3 = '3';
    public static final char BLOCK_4 = '4';
    public static final char BLOCK_RAINBOW = '9';
    public static final char BLOCK_STONE = '0';
    public static final char[] TARGET_BLOCK_CODES = { BLOCK_1, BLOCK_2, BLOCK_3, BLOCK_4,
            BLOCK_RAINBOW };
    public static final char[] MOVABLE_CODES = { BLOCK_1, BLOCK_2, BLOCK_3, BLOCK_4, BLOCK_RAINBOW,
            BLOCK_STONE };
    public static final char GATE_1 = 'U';
    public static final char GATE_2 = 'I';
    public static final char GATE_3 = 'O';
    public static final char GATE_4 = 'P';
    public static final char[] GATE_CODES = { GATE_1, GATE_2, GATE_3, GATE_4 };
    public static final char WRAP_1 = 'Q';
    public static final char WRAP_2 = 'Z';
    public static final char WRAP_3 = 'E';
    public static final char WRAP_4 = 'R';
    public static final char[] WRAP_CODES = { WRAP_1, WRAP_2, WRAP_3, WRAP_4 };
    public static final char WALL_MOVE_LEFT = 'C';
    public static final char WALL_MOVE_UP = 'F';
    public static final char WALL_MOVE_DOWN = 'V';
    public static final char WALL_MOVE_RIGHT = 'B';
    public static final char WALL = 'X';
    public static final char[] SOLID_CODES =
    { BLOCK_1, BLOCK_2, BLOCK_3, BLOCK_4, BLOCK_RAINBOW, BLOCK_STONE,
            GATE_1, GATE_2, GATE_3, GATE_4,
            WALL_MOVE_LEFT, WALL_MOVE_UP, WALL_MOVE_DOWN, WALL_MOVE_RIGHT, WALL };
    public static final char MOVE_LEFT = 'A';
    public static final char MOVE_UP = 'W';
    public static final char MOVE_DOWN = 'S';
    public static final char MOVE_RIGHT = 'D';
    public static final char STICKY = 'T';
    public static final char GATE_SWITCH_1 = 'H';
    public static final char GATE_SWITCH_2 = 'J';
    public static final char GATE_SWITCH_3 = 'K';
    public static final char GATE_SWITCH_4 = 'L';
    public static final char NO_MATCH_AREA = 'N';
    public static final char SHIFT_1 = '6';
    public static final char SHIFT_2 = '7';
    public static final char SHIFT_3 = '8';
    public static final char SHIFT_4 = 'Y';
    public static final char SHIFT_RAINBOW = 'G';
    public static final char[] SHIFT_CODES = { SHIFT_1, SHIFT_2, SHIFT_3, SHIFT_4,
            SHIFT_RAINBOW };
    public static final char[] BOTTOM_LAYER_CODES =
    { MOVE_LEFT, MOVE_UP, MOVE_DOWN, MOVE_RIGHT, STICKY,
            GATE_SWITCH_1, GATE_SWITCH_2, GATE_SWITCH_3, GATE_SWITCH_4, NO_MATCH_AREA,
            WRAP_1, WRAP_2, WRAP_3, WRAP_4,
            SHIFT_1, SHIFT_2, SHIFT_3, SHIFT_4, SHIFT_RAINBOW };
    public static final char[] ALL_CODES =
    { EMPTY, WALL, BLOCK_1, BLOCK_2, BLOCK_3, BLOCK_4, BLOCK_STONE, BLOCK_RAINBOW,
            MOVE_LEFT, MOVE_UP, MOVE_DOWN, MOVE_RIGHT, STICKY,
            GATE_1, GATE_2, GATE_3, GATE_4, GATE_SWITCH_1, GATE_SWITCH_2, GATE_SWITCH_3,
            GATE_SWITCH_4,
            SHIFT_1, SHIFT_2, SHIFT_3, SHIFT_4, SHIFT_RAINBOW,
            WALL_MOVE_LEFT, WALL_MOVE_UP, WALL_MOVE_DOWN, WALL_MOVE_RIGHT,
            NO_MATCH_AREA, WRAP_1, WRAP_2, WRAP_3, WRAP_4 };

    private static HashMap<Character, Bitmap> bitmaps = new HashMap<Character, Bitmap>();

    public static final boolean inArray(char type, char[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (type == array[i]) {
                return true;
            }
        }
        return false;
    }

    public static final int getDrawableId(char code) {
        switch (code) {
            case WALL:
                return R.drawable.wall;

            case BLOCK_1:
                return R.drawable.b1;

            case BLOCK_2:
                return R.drawable.b2;

            case BLOCK_3:
                return R.drawable.b3;

            case BLOCK_4:
                return R.drawable.b4;

            case BLOCK_STONE:
                return R.drawable.stone;

            case BLOCK_RAINBOW:
                return R.drawable.rainbow;

            case MOVE_LEFT:
                return R.drawable.left;

            case MOVE_RIGHT:
                return R.drawable.right;

            case MOVE_UP:
                return R.drawable.up;

            case MOVE_DOWN:
                return R.drawable.down;

            case STICKY:
                return R.drawable.sticky;

            case GATE_1:
                return R.drawable.gate1;

            case GATE_2:
                return R.drawable.gate2;

            case GATE_3:
                return R.drawable.gate3;

            case GATE_4:
                return R.drawable.gate4;

            case GATE_SWITCH_1:
                return R.drawable.button1;

            case GATE_SWITCH_2:
                return R.drawable.button2;

            case GATE_SWITCH_3:
                return R.drawable.button3;

            case GATE_SWITCH_4:
                return R.drawable.button4;

            case WALL_MOVE_LEFT:
                return R.drawable.left_wall;

            case WALL_MOVE_DOWN:
                return R.drawable.down_wall;

            case WALL_MOVE_UP:
                return R.drawable.up_wall;

            case WALL_MOVE_RIGHT:
                return R.drawable.right_wall;

            case NO_MATCH_AREA:
                return R.drawable.blur_area;

            case WRAP_1:
                return R.drawable.wrap1;

            case WRAP_2:
                return R.drawable.wrap2;

            case WRAP_3:
                return R.drawable.wrap3;

            case WRAP_4:
                return R.drawable.wrap4;

            case SHIFT_1:
                return R.drawable.red_shift;

            case SHIFT_2:
                return R.drawable.green_shift;

            case SHIFT_3:
                return R.drawable.blue_shift;

            case SHIFT_4:
                return R.drawable.yellow_shift;

            case SHIFT_RAINBOW:
                return R.drawable.rainbow_shift;

            default:
                return 0;
        }
    }

    public static final Bitmap getBitmap(Context context, char code) {
        if (bitmaps.containsKey(code))
            return bitmaps.get(code);

        Bitmap m;
        int drawable = getDrawableId(code);
        if (drawable == 0) {
            m = null;
        } else {
            m = BitmapFactory.decodeResource(context.getResources(), drawable);
        }

        bitmaps.put(code, m);
        return m;
    }

}
