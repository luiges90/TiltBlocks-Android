package com.luiges90.tiltblocks;

import java.util.ArrayList;
import java.util.List;

import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class AnimatedGameField extends GameField {

    private static final long serialVersionUID = -1855975806098589742L;

    public static final int MOVING_SPEED = 100;
    public static final int WRAPPING_SPEED = 400;
    public static final int ELIMINATING_SPEED = 400;

    private transient GameView gameView;
    private transient List<AnimatorSet> animatorSets = new ArrayList<AnimatorSet>();
    private transient List<ObjectAnimator> currentAnimators = new ArrayList<ObjectAnimator>();

    /**
     * This function is to set the context only when first constructed from serialized state
     * 
     * @param gameView
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
        this.gameView.setField(this);
        this.gameView.postInvalidate();
    }

    public AnimatedGameField(GameView gameView) {
        super();
        this.gameView = gameView;
        this.gameView.setField(this);
        this.gameView.postInvalidate();
    }

    public AnimatedGameField(GameView gameView, String levelStr, int level) {
        super(levelStr, level);
        this.gameView = gameView;
        this.gameView.setField(this);
        this.gameView.postInvalidate();
    }

    private void invalidateGameView() {
        gameView.postInvalidate();
    }

    private List<Animator> prepareCurrentAnimators() {
        List<Animator> toPlay = new ArrayList<Animator>();
        for (ObjectAnimator a : currentAnimators) {
            a.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    AnimatedGameField.this.invalidateGameView();
                }
            });

            toPlay.add(a);
        }

        currentAnimators.clear();

        return toPlay;
    }

    protected void moveBlock(Block b, int newR, int newC) {
        ObjectAnimator row = ObjectAnimator.ofFloat(b, "drawRow", newR);
        ObjectAnimator col = ObjectAnimator.ofFloat(b, "drawCol", newC);
        row.setDuration(Math.abs(newR - b.getRow()) * MOVING_SPEED);
        col.setDuration(Math.abs(newC - b.getCol()) * MOVING_SPEED);
        row.setInterpolator(new LinearInterpolator());
        col.setInterpolator(new LinearInterpolator());

        super.moveBlock(b, newR, newC);

        currentAnimators.add(row);
        currentAnimators.add(col);
    }

    public int moveBlocks(int dirR, int dirC) {
        int result = super.moveBlocks(dirR, dirC);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(prepareCurrentAnimators());

        animatorSets.add(set);

        return result;
    }

    protected void eliminateBlock(Block b) {
        ObjectAnimator r = ObjectAnimator.ofFloat(b, "opacity", 0);
        r.setDuration(ELIMINATING_SPEED);

        super.eliminateBlock(b);

        currentAnimators.add(r);
    }

    public int eliminateBlocks() {
        int result = super.eliminateBlocks();

        AnimatorSet set = new AnimatorSet();
        set.playTogether(prepareCurrentAnimators());

        animatorSets.add(set);

        return result;
    }

    public void moveWalls() {
        super.moveWalls();

        AnimatorSet set = new AnimatorSet();
        set.playTogether(prepareCurrentAnimators());

        animatorSets.add(set);
    }

    protected void changeBlock(Block b, char newCode) {
        if (b.getType() != newCode) {
            ObjectAnimator disappear = ObjectAnimator.ofFloat(b, "opacity", 0);
            disappear.setDuration(WRAPPING_SPEED);

            ObjectAnimator changeType = ObjectAnimator.ofInt(b, "shownType", newCode);
            changeType.setDuration(0);
            changeType.setStartDelay(WRAPPING_SPEED);

            ObjectAnimator reappear = ObjectAnimator.ofFloat(b, "opacity", 1);
            reappear.setDuration(WRAPPING_SPEED);
            reappear.setStartDelay(WRAPPING_SPEED);

            super.changeBlock(b, newCode);

            currentAnimators.add(disappear);
            currentAnimators.add(changeType);
            currentAnimators.add(reappear);
        }
    }

    public void changeBlocksColor() {
        super.changeBlocksColor();

        AnimatorSet set = new AnimatorSet();
        set.playTogether(prepareCurrentAnimators());

        animatorSets.add(set);
    }

    protected void wrapBlock(Block b, int newR, int newC) {
        ObjectAnimator disappear = ObjectAnimator.ofFloat(b, "opacity", 0);
        disappear.setDuration(WRAPPING_SPEED);

        ObjectAnimator moveRow = ObjectAnimator.ofFloat(b, "drawRow", newR);
        ObjectAnimator moveCol = ObjectAnimator.ofFloat(b, "drawCol", newC);
        moveRow.setDuration(0);
        moveCol.setDuration(0);
        moveRow.setStartDelay(WRAPPING_SPEED);
        moveCol.setStartDelay(WRAPPING_SPEED);

        ObjectAnimator reappear = ObjectAnimator.ofFloat(b, "opacity", 1);
        reappear.setDuration(WRAPPING_SPEED);
        reappear.setStartDelay(WRAPPING_SPEED);

        super.wrapBlock(b, newR, newC);

        currentAnimators.add(disappear);
        currentAnimators.add(moveRow);
        currentAnimators.add(moveCol);
        currentAnimators.add(reappear);
    }

    public void wrapBlocks() {
        super.wrapBlocks();

        AnimatorSet set = new AnimatorSet();
        set.playTogether(prepareCurrentAnimators());

        animatorSets.add(set);
    }

    public long makeStep(int dirR, int dirC) {
        animatorSets.clear();

        super.makeStep(dirR, dirC);

        long delay = 0;
        if (animatorSets.size() > 0) {
            for (AnimatorSet i : animatorSets) {
                i.setStartDelay(delay);
                i.start();

                long maxDuration = 0;
                for (Animator j : i.getChildAnimations()) {
                    if (j.getDuration() + j.getStartDelay() > maxDuration) {
                        maxDuration = j.getDuration() + j.getStartDelay();
                    }
                }
                delay += maxDuration;
            }
        }

        return delay;
    }

    public boolean isAnimationRunning() {
        if (animatorSets.size() <= 0)
            return false;
        for (AnimatorSet s : animatorSets) {
            if (s.isRunning()) {
                return true;
            }
        }
        return false;
    }

}
