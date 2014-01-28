package com.luiges90.tiltblocks;

public class Point {
    public final int c;
    public final int r;

    public Point(int r, int c) {
        this.c = c;
        this.r = r;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + c;
        result = prime * result + r;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Point))
            return false;
        Point other = (Point) obj;
        if (c != other.c)
            return false;
        if (r != other.r)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Point [c=" + c + ", r=" + r + "]";
    }
    
    
}