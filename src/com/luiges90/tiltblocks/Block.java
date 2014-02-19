package com.luiges90.tiltblocks;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;

public class Block implements Serializable {

    private static final long serialVersionUID = 7490366358636618291L;

    private char type, shownType;
    private int row, col; // logical position
    private float drawRow, drawCol; // drawn position
    private boolean eliminated;
    private float opacity;

    public Block(char type, int r, int c) {
        this.type = type;
        this.shownType = type;
        this.row = r;
        this.col = c;
        this.drawRow = r;
        this.drawCol = c;
        this.eliminated = false;
        this.opacity = 1;
    }

    public Block(Block b) {
        this.type = b.type;
        this.shownType = b.shownType;
        this.row = b.row;
        this.col = b.col;
        this.drawRow = b.drawRow;
        this.drawCol = b.drawCol;
        this.eliminated = b.eliminated;
        this.opacity = b.opacity;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public float getDrawRow() {
        return drawRow;
    }

    public void setDrawRow(float drawRow) {
        this.drawRow = drawRow;
    }

    public float getDrawCol() {
        return drawCol;
    }

    public void setDrawCol(float drawCol) {
        this.drawCol = drawCol;
    }

    public char getType() {
        return type;
    }

    public Block setType(char type) {
        this.type = type;
        return this;
    }

    public int getShownType() {
        return shownType;
    }

    public Block setShownType(int shownType) {
        this.shownType = (char) shownType;
        return this;
    }

    public Bitmap getBitmap(Context context) {
        return BlockType.getBitmap(context, shownType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + col;
        result = prime * result + row;
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Block))
            return false;
        Block other = (Block) obj;
        if (col != other.col)
            return false;
        if (row != other.row)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Block [type=" + type + ", row=" + row + ", col=" + col + "]";
    }

}
