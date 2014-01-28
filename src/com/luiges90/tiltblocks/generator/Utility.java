package com.luiges90.tiltblocks.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.luiges90.tiltblocks.BlockType;
import com.luiges90.tiltblocks.GameField;
import com.luiges90.tiltblocks.Point;

public class Utility {

    private static final Random rng = new Random();

    private Utility(){}

    public static int randBetween(int lo, int hi) {
        if (lo > hi) {
            int t = hi;
            hi = lo;
            lo = t;
        }
        return rng.nextInt(hi - lo + 1) + lo;
    }

    public static <T> T randomPick(Collection<T> c) {
        if (c.size() == 0)
            return null;
        int index = randBetween(0, c.size() - 1);
        if (c instanceof List<?>) {
            return ((List<T>) c).get(index);
        }
        int j = 0;
        for (T i : c) {
            if (j == index) {
                return i;
            }
            j++;
        }
        assert false;
        return null;
    }

    public static boolean chance(double prob) {
        return (prob / 100.0 >= rng.nextDouble());
    }
    
    public static Set<Point> getRandomArea(Set<Point> region, int cnt) {
        Set<Point> points = new HashSet<Point>(cnt);

        points.add(Utility.randomPick(region));

        for (int i = 0; i < cnt; ++i) {
            Point p = Utility.randomPick(points);

            List<Point> candidates = new ArrayList<Point>(4);
            candidates.add(new Point(p.r, p.c - 1));
            candidates.add(new Point(p.r, p.c + 1));
            candidates.add(new Point(p.r - 1, p.c));
            candidates.add(new Point(p.r + 1, p.c));

            Iterator<Point> it = candidates.iterator();
            while (it.hasNext()) {
                Point q = it.next();
                if (points.contains(q) || q.c < 1 || q.c > GameField.BOARD_SIZE - 2
                        || q.r < 1 || q.r > GameField.BOARD_SIZE - 2) {
                    it.remove();
                }
            }

            if (candidates.size() > 0) {
                Point chosen = Utility.randomPick(candidates);
                points.add(chosen);
            } else {
                --i;
            }
        }

        return points;
    }
    
    public static Set<Point> getEdgeArea(Collection<Point> area) {
        Set<Point> result = new HashSet<Point>();
        
        for (Point p : area) {
            Point edge;

            edge = new Point(p.r, p.c - 1);
            if (!area.contains(edge)) {
                result.add(edge);
            }

            edge = new Point(p.r, p.c + 1);
            if (!area.contains(edge)) {
                result.add(edge);
            }

            edge = new Point(p.r - 1, p.c);
            if (!area.contains(edge)) {
                result.add(edge);
            }

            edge = new Point(p.r + 1, p.c);
            if (!area.contains(edge)) {
                result.add(edge);
            }
        }
        
        return result;
    }
    
    public static void addWallsAroundArea(GameField f, Collection<Point> area) 
    {
        for (Point p : area) {
            Point edge;

            edge = new Point(p.r, p.c - 1);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r, p.c - 1, BlockType.WALL);
            }

            edge = new Point(p.r, p.c + 1);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r, p.c + 1, BlockType.WALL);
            }

            edge = new Point(p.r - 1, p.c);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r - 1, p.c, BlockType.WALL);
            }

            edge = new Point(p.r + 1, p.c);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r + 1, p.c, BlockType.WALL);
            }

            edge = new Point(p.r - 1, p.c - 1);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r - 1, p.c - 1, BlockType.WALL);
            }

            edge = new Point(p.r - 1, p.c + 1);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r - 1, p.c + 1, BlockType.WALL);
            }

            edge = new Point(p.r + 1, p.c + 1);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r + 1, p.c + 1, BlockType.WALL);
            }

            edge = new Point(p.r + 1, p.c - 1);
            if (!area.contains(edge)) {
                f.setBlockAt(p.r + 1, p.c - 1, BlockType.WALL);
            }
        }
    }

}
