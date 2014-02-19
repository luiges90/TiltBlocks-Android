package com.luiges90.tiltblocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSolver {

    private static final int COMPLETED = 0;
    private static final int NOT_ENOUGH_STEP = 1;
    private static final int HARD_IMPOSSIBLE = 2;

    public GameField field;

    private int currentLimit;

    private final boolean containMoveWall;
    private final boolean containChangeColor;

    private int totalStepTracked = 0;
    private int totalStepPruned = 0;

    public interface OnProgressListener {
        public void onProgressUpdate(int step);
    }

    private OnProgressListener progressListener;

    private final boolean containMoveWall() {
        for (Block b : field.getAllBlocks()) {
            if (b.getType() == BlockType.WALL_MOVE_DOWN
                    || b.getType() == BlockType.WALL_MOVE_LEFT
                    || b.getType() == BlockType.WALL_MOVE_UP
                    || b.getType() == BlockType.WALL_MOVE_RIGHT) {
                return true;
            }
        }
        return false;
    }

    private final boolean containChangeColor() {
        for (Block b : field.getAllBlocks()) {
            if (b.getType() == BlockType.SHIFT_1
                    || b.getType() == BlockType.SHIFT_2
                    || b.getType() == BlockType.SHIFT_3
                    || b.getType() == BlockType.SHIFT_4
                    || b.getType() == BlockType.SHIFT_RAINBOW) {
                return true;
            }
        }
        return false;
    }

    public GameSolver(GameField field) {
        this.field = new GameField(field);

        containMoveWall = containMoveWall();
        containChangeColor = containChangeColor();
    }

    public GameSolver(GameField field, OnProgressListener listener) {
        this(field);

        progressListener = listener;
    }

    /**
     * Determine whether a given board is definitely impossible to clear.
     * This function is meant to prune some subtrees early in the IDS game tree search,
     * and hence must be able to execute quickly.
     * 
     * @param field
     * @return
     */
    private boolean isImpossible(GameField field) {
        Map<Character, List<Point>> blockPositions = new HashMap<Character, List<Point>>();
        for (Block b : field.getAllBlocks()) {
            if (!b.isEliminated()) {
                if (!blockPositions.containsKey(b.getType())) {
                    blockPositions.put(b.getType(), new ArrayList<Point>());
                }
                blockPositions.get(b.getType()).add(new Point(b.getRow(), b.getCol()));
            }
        }

        // color block counts
        int totalBlock = 0;
        if (!containChangeColor) {
            if (!blockPositions.containsKey(BlockType.BLOCK_RAINBOW)) {
                for (Map.Entry<Character, List<Point>> kv : blockPositions.entrySet()) {
                    if ((kv.getKey() == BlockType.BLOCK_1 || kv.getKey() == BlockType.BLOCK_2 ||
                            kv.getKey() == BlockType.BLOCK_3 || kv.getKey() == BlockType.BLOCK_4) &&
                            kv.getValue().size() == 1) {
                        return true;
                    }
                }
            }

            if (blockPositions.containsKey(BlockType.BLOCK_RAINBOW) &&
                    blockPositions.get(BlockType.BLOCK_RAINBOW).size() == 1 &&
                    (!blockPositions.containsKey(BlockType.BLOCK_1) &&
                            !blockPositions.containsKey(BlockType.BLOCK_2) &&
                            !blockPositions.containsKey(BlockType.BLOCK_3) &&
                    !blockPositions.containsKey(BlockType.BLOCK_4))) {
                return true;
            }
        } else {
            for (Map.Entry<Character, List<Point>> e : blockPositions.entrySet()) {
                if (e.getKey() == BlockType.BLOCK_1 ||
                        e.getKey() == BlockType.BLOCK_2 ||
                        e.getKey() == BlockType.BLOCK_3 ||
                        e.getKey() == BlockType.BLOCK_4 ||
                        e.getKey() == BlockType.BLOCK_RAINBOW) {
                    totalBlock += e.getValue().size();
                }
            }
            if (totalBlock == 1) {
                return true;
            }
        }

        // stickies
        int rainbowSize = blockPositions.containsKey(BlockType.BLOCK_RAINBOW) ?
                blockPositions.get(BlockType.BLOCK_RAINBOW).size() : 0;
        for (Map.Entry<Character, List<Point>> kv : blockPositions.entrySet()) {
            if (kv.getKey() == BlockType.BLOCK_1 || kv.getKey() == BlockType.BLOCK_2 ||
                    kv.getKey() == BlockType.BLOCK_3 || kv.getKey() == BlockType.BLOCK_4) {
                int stuck = 0;
                for (Point lp : kv.getValue()) {
                    Block bottom = field.getBottomLayerAt(lp.r, lp.c);
                    if (bottom != null && bottom.getType() == BlockType.STICKY) {
                        stuck++;
                    }
                }

                if (containChangeColor) {
                    if (stuck >= totalBlock) {
                        return true;
                    }
                } else {
                    if (stuck >= blockPositions.get(kv.getKey()).size() + rainbowSize) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isBoardDupicated(GameField field, List<GameField> pastSteps) {
        for (GameField f : pastSteps) {
            if (field.hasEqualFieldTo(f)) {
                return true;
            }
        }
        return false;
    }

    private int dls_r(GameField field, int currentStep, char dir,
            List<Character> steps, List<GameField> pastFields) {
        int solvedSteps;

        totalStepTracked++;

        steps.add(dir);
        pastFields.add(new GameField(field));

        switch (dir) {
            case GameField.UP:
                field.moveUp();
                break;
            case GameField.DOWN:
                field.moveDown();
                break;
            case GameField.LEFT:
                field.moveLeft();
                break;
            case GameField.RIGHT:
                field.moveRight();
                break;
        }

        boolean completed = field.checkComplete();
        boolean ignoreOpposite = !field.hasAnyBlockEliminated() && !field.hasAnyBlockWrapped() &&
                !field.hasAnyBlockCrossedArrow() && !field.hasAnyBlockSticked() &&
                !field.hasAnyBlockShifted() && !containMoveWall;

        if (completed) {
            return COMPLETED;
        } else if (isImpossible(field) || isBoardDupicated(field, pastFields)) {
            totalStepPruned++;
            steps.remove(steps.size() - 1);
            pastFields.remove(pastFields.size() - 1);
            return HARD_IMPOSSIBLE;
        } else if (currentStep > currentLimit) {
            steps.remove(steps.size() - 1);
            pastFields.remove(pastFields.size() - 1);
            return NOT_ENOUGH_STEP;
        } else {
            boolean hardImpossible = true;

            if ((dir != GameField.UP) && (dir != GameField.DOWN || !ignoreOpposite)) {
                solvedSteps = dls_r(new GameField(field), currentStep + 1, GameField.UP, steps,
                        pastFields);
                if (solvedSteps == COMPLETED) {
                    return COMPLETED;
                }
                if (solvedSteps != HARD_IMPOSSIBLE) {
                    hardImpossible = false;
                }
            }

            if ((dir != GameField.LEFT) && (dir != GameField.RIGHT || !ignoreOpposite)) {
                solvedSteps = dls_r(new GameField(field), currentStep + 1, GameField.LEFT, steps,
                        pastFields);
                if (solvedSteps == COMPLETED) {
                    return COMPLETED;
                }
                if (solvedSteps != HARD_IMPOSSIBLE) {
                    hardImpossible = false;
                }
            }

            if ((dir != GameField.DOWN) && (dir != GameField.UP || !ignoreOpposite)) {
                solvedSteps = dls_r(new GameField(field), currentStep + 1, GameField.DOWN, steps,
                        pastFields);
                if (solvedSteps == COMPLETED) {
                    return COMPLETED;
                }
                if (solvedSteps != HARD_IMPOSSIBLE) {
                    hardImpossible = false;
                }
            }

            if ((dir != GameField.RIGHT) && (dir != GameField.LEFT || !ignoreOpposite)) {
                solvedSteps = dls_r(new GameField(field), currentStep + 1, GameField.RIGHT, steps,
                        pastFields);
                if (solvedSteps == COMPLETED) {
                    return COMPLETED;
                }
                if (solvedSteps != HARD_IMPOSSIBLE) {
                    hardImpossible = false;
                }
            }

            steps.remove(steps.size() - 1);
            pastFields.remove(pastFields.size() - 1);
            if (hardImpossible) {
                return HARD_IMPOSSIBLE;
            }

            return NOT_ENOUGH_STEP;
        }
    }

    /**
     * 
     * @return solution as list of integer. "null" if the board is definitely impossible.
     *         Empty list if not enough step is given
     */
    private List<Character> dls(int limit) {
        int solvedSteps;
        boolean hardImpossible = true;
        List<Character> solution = new ArrayList<Character>();

        currentLimit = limit;

        solvedSteps = dls_r(new GameField(field), 1, GameField.UP, solution,
                new ArrayList<GameField>());
        if (solvedSteps == COMPLETED) {
            return solution;
        }
        if (solvedSteps != HARD_IMPOSSIBLE) {
            hardImpossible = false;
        }

        solvedSteps = dls_r(new GameField(field), 1, GameField.LEFT, solution,
                new ArrayList<GameField>());
        if (solvedSteps == COMPLETED) {
            return solution;
        }
        if (solvedSteps != HARD_IMPOSSIBLE) {
            hardImpossible = false;
        }

        solvedSteps = dls_r(new GameField(field), 1, GameField.DOWN, solution,
                new ArrayList<GameField>());
        if (solvedSteps == COMPLETED) {
            return solution;
        }
        if (solvedSteps != HARD_IMPOSSIBLE) {
            hardImpossible = false;
        }

        solvedSteps = dls_r(new GameField(field), 1, GameField.RIGHT, solution,
                new ArrayList<GameField>());
        if (solvedSteps == COMPLETED) {
            return solution;
        }
        if (solvedSteps != HARD_IMPOSSIBLE) {
            hardImpossible = false;
        }

        if (hardImpossible) {
            return null;
        }

        return new ArrayList<Character>();
    }

    public int getTotalStepTracked() {
        return totalStepTracked;
    }

    public int getTotalStepPruned() {
        return totalStepPruned;
    }

    public boolean solvableInSteps(int step) {
        List<Character> solution = null;
        for (int i = 0; i < step; ++i) {
            solution = dls(i);
            if (solution == null || solution.size() > 0)
                break;
        }
        return solution != null && solution.size() > 0;
    }

    /**
     * 
     * @return solution as array of integer. "null" if the board is definitely impossible.
     *         Empty array if not enough step is given
     */
    public char[] solve() {
        List<Character> solution = null;
        totalStepTracked = 0;
        totalStepPruned = 0;

        for (int i = 0; i < field.getStepLimit(); ++i) {
            if (progressListener != null) {
                progressListener.onProgressUpdate(i);
            }
            solution = dls(i);
            if (solution == null || solution.size() > 0)
                break;
        }

        char[] result;

        if (solution == null) {
            result = null;
        } else if (solution.size() == 0) {
            result = new char[0];
        } else {
            result = new char[solution.size()];
            for (int i = 0; i < solution.size(); ++i) {
                result[i] = solution.get(i);
            }
        }

        return result;
    }

}
