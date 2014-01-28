package com.luiges90.tiltblocks.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.luiges90.tiltblocks.Block;
import com.luiges90.tiltblocks.BlockType;
import com.luiges90.tiltblocks.GameField;
import com.luiges90.tiltblocks.Point;

public class BackwardGameGenerator implements GameGeneratorInterface {

    private static final char UP = GameField.UP;
    private static final char DOWN = GameField.DOWN;
    private static final char LEFT = GameField.LEFT;
    private static final char RIGHT = GameField.RIGHT;

    private static final double STICKY_NOT_MOVE_PROB = 60;
    private static final double ADD_GATE_SWITCH_PROB = 40;
    private static final double ADD_ARROW_ON_ROUTE_PROB = 5;
    private static final double ADD_GATE_ON_ROUTE_PROB = 10;
    private static final double STONE_INSTEAD_OF_WALL_PROB = 10;
    private static final double ARROW_INSTEAD_OF_WALL_PROB = 10;
    private static final double RAINBOW_PROB = 50;
    private static final double DOUBLE_RAINBOW_PROB = 5;
    private static final double ADD_STICKY_PROB = 40;
    private static final double ADD_COLOR_SHIFT_PROB = 20;
    private static final double ADD_WRAP_PROB = 15;

    private Set<Point> requiredEmpty = new HashSet<Point>();
    private Set<Point> pendingStickies = new HashSet<Point>();
    private Set<Point> pendingNoMatches = new HashSet<Point>();
    private Map<Point, Integer> pendingWraps = new HashMap<Point, Integer>();
    private GeneratorOption opt;

    private boolean enableOpposite = false;
    private int currentGate = 0;
    private int addedGate = 0;
    private int currentWrap = 0;

    private List<Integer> colorJustAdded = new ArrayList<Integer>();

    private boolean debug = false;

    public BackwardGameGenerator() {
    }

    public BackwardGameGenerator(boolean debug) {
        this.debug = debug;
    }

    private char currentGateCode() {
        switch (currentGate) {
            case 0:
                return BlockType.GATE_1;
            case 1:
                return BlockType.GATE_2;
            case 2:
                return BlockType.GATE_3;
            case 3:
                return BlockType.GATE_4;
        }
        throw new IllegalArgumentException("Wrong ID");
    }

    private char currentGateSwitchCode() {
        switch (addedGate) {
            case 0:
                return BlockType.GATE_SWITCH_1;
            case 1:
                return BlockType.GATE_SWITCH_2;
            case 2:
                return BlockType.GATE_SWITCH_3;
            case 3:
                return BlockType.GATE_SWITCH_4;
        }
        throw new IllegalArgumentException("Wrong ID");
    }

    private char wrapCode(int id) {
        switch (id) {
            case 0:
                return BlockType.WRAP_1;
            case 1:
                return BlockType.WRAP_2;
            case 2:
                return BlockType.WRAP_3;
            case 3:
                return BlockType.WRAP_4;
        }
        throw new IllegalArgumentException("Wrong ID");
    }

    private char colorCodeToColorShift(char code) {
        switch (code) {
            case BlockType.BLOCK_1:
                return BlockType.SHIFT_1;
            case BlockType.BLOCK_2:
                return BlockType.SHIFT_2;
            case BlockType.BLOCK_3:
                return BlockType.SHIFT_3;
            case BlockType.BLOCK_4:
                return BlockType.SHIFT_4;
            case BlockType.BLOCK_RAINBOW:
                return BlockType.SHIFT_RAINBOW;
        }
        throw new IllegalArgumentException("Wrong ID: " + code);
    }

    private char randomColoredBlock() {
        List<Character> c = new ArrayList<Character>();
        c.add(BlockType.BLOCK_1);
        c.add(BlockType.BLOCK_2);
        c.add(BlockType.BLOCK_3);
        c.add(BlockType.BLOCK_4);
        return Utility.randomPick(c);
    }

    private boolean isColorJustAdded(Block b) {
        if (b == null)
            return false;
        for (int i : colorJustAdded) {
            switch (i) {
                case 1:
                    if (b.getType() == BlockType.BLOCK_1)
                        return true;
                    break;
                case 2:
                    if (b.getType() == BlockType.BLOCK_2)
                        return true;
                    break;
                case 3:
                    if (b.getType() == BlockType.BLOCK_3)
                        return true;
                    break;
                case 4:
                    if (b.getType() == BlockType.BLOCK_4)
                        return true;
                    break;
            }
        }
        return false;
    }

    private List<Point> addEliminatedBlocks(GameField f, int color, int cnt, boolean noHoriz,
            boolean noVert) {
        Point a;
        List<Character> dirList = new ArrayList<Character>();
        List<Point> placed = new ArrayList<Point>();

        do {
            dirList.clear();

            a = new Point(Utility.randBetween(1, GameField.BOARD_SIZE - 2),
                    Utility.randBetween(1, GameField.BOARD_SIZE - 2));

            if (!requiredEmpty.contains(a)) {
                dirList.clear();
                if (a.r > 1) {
                    if (!requiredEmpty.contains(new Point(a.r - 1, a.c)) && !noVert) {
                        dirList.add(UP);
                    }
                }
                if (a.r < GameField.BOARD_SIZE - 2) {
                    if (!requiredEmpty.contains(new Point(a.r + 1, a.c)) && !noVert) {
                        dirList.add(DOWN);
                    }
                }
                if (a.c > 1) {
                    if (!requiredEmpty.contains(new Point(a.r, a.c - 1)) && !noHoriz) {
                        dirList.add(LEFT);
                    }
                }
                if (a.c < GameField.BOARD_SIZE - 2) {
                    if (!requiredEmpty.contains(new Point(a.r, a.c + 1)) && !noHoriz) {
                        dirList.add(RIGHT);
                    }
                }
            }
        } while (dirList.size() == 0);

        int dir = Utility.randomPick(dirList);

        Point b;
        switch (dir) {
            case UP: {
                b = new Point(a.r - 1, a.c);
                break;
            }
            case DOWN: {
                b = new Point(a.r + 1, a.c);
                break;
            }
            case LEFT: {
                b = new Point(a.r, a.c - 1);
                break;
            }
            case RIGHT: {
                b = new Point(a.r, a.c + 1);
                break;
            }
            default:
                assert false;
                return null;
        }

        char code = BlockType.BLOCK_1;
        switch (color) {
            case 1: {
                code = BlockType.BLOCK_1;
                break;
            }
            case 2: {
                code = BlockType.BLOCK_2;
                break;
            }
            case 3: {
                code = BlockType.BLOCK_3;
                break;
            }
            case 4: {
                code = BlockType.BLOCK_4;
                break;
            }
        }

        if (opt.rainbow && Utility.chance(RAINBOW_PROB)) {
            if (Utility.chance(DOUBLE_RAINBOW_PROB)) {
                f.setBlockAt(a.r, a.c, BlockType.BLOCK_RAINBOW);
                f.setBlockAt(b.r, b.c, BlockType.BLOCK_RAINBOW);
            } else {
                f.setBlockAt(a.r, a.c, BlockType.BLOCK_RAINBOW);
                f.setBlockAt(b.r, b.c, code);
            }
        } else {
            f.setBlockAt(a.r, a.c, code);
            f.setBlockAt(b.r, b.c, code);
        }

        requiredEmpty.add(a);
        requiredEmpty.add(b);

        placed.add(a);
        placed.add(b);

        if (opt.sticky) {
            if (Utility.chance(ADD_STICKY_PROB)) {
                pendingStickies.add(a);
            }
            if (Utility.chance(ADD_STICKY_PROB)) {
                pendingStickies.add(b);
            }
        }

        if (cnt >= 3) {
            Point c;

            dirList.clear();

            if (a.r > 1) {
                if (!requiredEmpty.contains(new Point(a.r - 1, a.c)) && !noVert) {
                    dirList.add('1');
                }
            }
            if (a.r < GameField.BOARD_SIZE - 2) {
                if (!requiredEmpty.contains(new Point(a.r + 1, a.c)) && !noVert) {
                    dirList.add('2');
                }
            }
            if (a.c > 1) {
                if (!requiredEmpty.contains(new Point(a.r, a.c - 1)) && !noHoriz) {
                    dirList.add('3');
                }
            }
            if (a.c < GameField.BOARD_SIZE - 2) {
                if (!requiredEmpty.contains(new Point(a.r, a.c + 1)) && !noHoriz) {
                    dirList.add('4');
                }
            }

            if (b.r > 1) {
                if (!requiredEmpty.contains(new Point(b.r - 1, b.c)) && !noVert) {
                    dirList.add('5');
                }
            }
            if (b.r < GameField.BOARD_SIZE - 2) {
                if (!requiredEmpty.contains(new Point(b.r + 1, b.c)) && !noVert) {
                    dirList.add('6');
                }
            }
            if (b.c > 1) {
                if (!requiredEmpty.contains(new Point(b.r, b.c - 1)) && !noHoriz) {
                    dirList.add('7');
                }
            }
            if (b.c < GameField.BOARD_SIZE - 2) {
                if (!requiredEmpty.contains(new Point(b.r, b.c + 1)) && !noHoriz) {
                    dirList.add('8');
                }
            }

            if (dirList.size() == 0)
                return placed;

            dir = Utility.randomPick(dirList);

            switch (dir) {
                case '1':
                    c = new Point(a.r - 1, a.c);
                    break;
                case '2':
                    c = new Point(a.r + 1, a.c);
                    break;
                case '3':
                    c = new Point(a.r, a.c - 1);
                    break;
                case '4':
                    c = new Point(a.r, a.c + 1);
                    break;
                case '5':
                    c = new Point(b.r - 1, b.c);
                    break;
                case '6':
                    c = new Point(b.r + 1, b.c);
                    break;
                case '7':
                    c = new Point(b.r, b.c - 1);
                    break;
                case '8':
                    c = new Point(b.r, b.c + 1);
                    break;
                default:
                    assert false;
                    return null;
            }

            if (opt.rainbow && Utility.chance(RAINBOW_PROB)) {
                f.setBlockAt(c.r, c.c, BlockType.BLOCK_RAINBOW);
            } else {
                f.setBlockAt(c.r, c.c, code);
            }

            requiredEmpty.add(c);
            placed.add(c);

            if (opt.sticky) {
                if (Utility.chance(ADD_STICKY_PROB)) {
                    pendingStickies.add(c);
                }
            }
        }

        return placed;
    }

    private boolean isOccupied(GameField f, int r, int c) {
        return r <= 0 || c <= 0 || r >= GameField.BOARD_SIZE - 1 || c >= GameField.BOARD_SIZE - 1 ||
                f.getAnyBlockAt(r, c) != null || requiredEmpty.contains(new Point(r, c));
    }

    private boolean setRequiredWall(GameField f, Block b, int rDir, int cDir) {
        Point wall = new Point(b.getRow() + rDir, b.getCol() + cDir);
        while (requiredEmpty.contains(wall)) {
            Block current = f.getBlockAt(wall.r, wall.c);
            if (current == null ||
                    !BlockType.inArray(current.getType(), BlockType.MOVABLE_CODES)) {
                return false;
            } else {
                wall = new Point(wall.r + rDir, wall.c + cDir);
            }
        }

        char wallCode = BlockType.WALL;
        if (opt.arrow && Utility.chance(ARROW_INSTEAD_OF_WALL_PROB)) {
            if (!isOccupied(f, wall.r, wall.c)) {
                List<Character> candidates = new ArrayList<Character>();
                candidates.add(BlockType.MOVE_UP);
                candidates.add(BlockType.MOVE_DOWN);
                candidates.add(BlockType.MOVE_LEFT);
                candidates.add(BlockType.MOVE_RIGHT);
                if (rDir == 1 && cDir == 0) {
                    candidates.remove((Object) BlockType.MOVE_DOWN);
                } else if (rDir == -1 && cDir == 0) {
                    candidates.remove((Object) BlockType.MOVE_UP);
                } else if (rDir == 0 && cDir == 1) {
                    candidates.remove((Object) BlockType.MOVE_RIGHT);
                } else if (rDir == 0 && cDir == -1) {
                    candidates.remove((Object) BlockType.MOVE_LEFT);
                }
                wallCode = Utility.randomPick(candidates);
                requiredEmpty.add(new Point(wall.r, wall.c));
            }
        }

        if (opt.stone && Utility.chance(STONE_INSTEAD_OF_WALL_PROB)) {
            Point next = new Point(wall.r + rDir, wall.c + cDir);
            if (!isOccupied(f, next.r, next.c)) {
                f.setBlockAt(wall.r, wall.c, BlockType.BLOCK_STONE);
                f.setBlockAt(next.r, next.c, wallCode);
                if (opt.sticky && Utility.chance(ADD_STICKY_PROB)) {
                    pendingStickies.add(new Point(wall.r, wall.c));
                }
                requiredEmpty.add(new Point(wall.r, wall.c));
            } else {
                f.setBlockAt(wall.r, wall.c, wallCode);
            }
        } else {
            f.setBlockAt(wall.r, wall.c, wallCode);
        }

        return true;
    }

    /**
     * Generate a backward step, given by step parameter
     * 
     * @param oldField
     * @param step
     * @return list of directions which the next step could be. empty set is returned if
     *         current field is not possible to generate given step;
     */
    private boolean generateBackwardStep(GameField oldField, int step) {
        Set<Point> emptyNeeded = new HashSet<Point>();
        GameField newField = new GameField(oldField);

        Set<Point> fixed = new HashSet<Point>();
        switch (step) {
            case UP: {
                char forceNextDir = 0;
                for (int c = 0; c < GameField.BOARD_SIZE; ++c) {
                    for (int r = GameField.BOARD_SIZE - 1; r >= 0; --r) {
                        Block b = newField.getBlockAt(r, c);
                        char bType = b == null ? BlockType.EMPTY : b.getType();
                        Point bLocation = new Point(r, c);
                        if (fixed.contains(bLocation))
                            continue;
                        if (b != null && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                            // clear required wall
                            if (!setRequiredWall(newField, b, -1, 0)) {
                                return false;
                            }

                            // clear required empties
                            int maxDisplacement = 0;
                            for (int i = b.getRow() + 1; i <= GameField.BOARD_SIZE - 2; ++i, ++maxDisplacement) {
                                Block current = newField.getBlockAt(i, b.getCol());
                                Block bottom = newField.getBottomLayerAt(i, b.getCol());
                                if ((current != null
                                        && BlockType.inArray(current.getType(),
                                        BlockType.SOLID_CODES)) ||
                                        (bottom != null &&
                                        (
                                        bottom.getType() == BlockType.MOVE_LEFT ||
                                                bottom.getType() == BlockType.MOVE_DOWN ||
                                                bottom.getType() == BlockType.MOVE_RIGHT ||
                                        bottom.getType() == BlockType.STICKY)
                                        )) {
                                    if (current != null) {
                                        if (current.getType() == BlockType.BLOCK_STONE) {
                                            break;
                                        }
                                        if (current.getType() == b.getType() ||
                                                current.getType() == BlockType.BLOCK_RAINBOW ||
                                                b.getType() == BlockType.BLOCK_RAINBOW) {
                                            maxDisplacement--;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (maxDisplacement < 0)
                                return false;

                            // move block
                            List<Integer> candidate = new ArrayList<Integer>();
                            List<Integer> needNoMatch = new ArrayList<Integer>();
                            for (int i = 0; i <= maxDisplacement; ++i) {
                                Block b1 = newField.getBlockAt(b.getRow() + i, b.getCol() - 1);
                                Block b2 = newField.getBlockAt(b.getRow() + i, b.getCol() + 1);
                                Block m1 = newField
                                        .getBottomLayerAt(b.getRow() + i, b.getCol());
                                if (m1 != null) {
                                    // do nothing
                                } else if (b1 != null &&
                                        (b1.getType() == b.getType() ||
                                                b1.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else if (b2 != null &&
                                        (b2.getType() == b.getType() ||
                                                b2.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else {
                                    if (requiredEmpty.contains(b1) && requiredEmpty.contains(b2)) {
                                        // do nothing
                                    } else if (requiredEmpty.contains(b1)) {
                                        if (forceNextDir != LEFT) {
                                            forceNextDir = RIGHT;
                                            candidate.add(i);
                                        }
                                    } else if (requiredEmpty.contains(b2)) {
                                        if (forceNextDir != RIGHT) {
                                            forceNextDir = LEFT;
                                            candidate.add(i);
                                        }
                                    } else {
                                        candidate.add(i);
                                    }
                                }
                            }

                            if (pendingWraps.containsKey(bLocation)) {
                                candidate.remove((Object) 0);
                            }

                            if (candidate.size() <= 0) {
                                return false;
                            }

                            int displacement = Utility.randomPick(candidate);

                            if (needNoMatch.contains(displacement)) {
                                pendingNoMatches.add(new Point(b.getRow() + displacement, b
                                        .getCol()));
                            }

                            boolean leaveSticky = false;
                            if (pendingStickies.contains(bLocation) &&
                                    !pendingNoMatches.contains(bLocation)) {
                                if (Utility.chance(STICKY_NOT_MOVE_PROB) && candidate.contains(0)) {
                                    displacement = 0;
                                } else {
                                    leaveSticky = true;
                                }
                            }

                            if (displacement > 0) {
                                boolean colorJustAdded = isColorJustAdded(b);
                                newField.setBlockAt(b.getRow() + displacement, b.getCol(),
                                        b.getType());
                                if (pendingNoMatches.contains(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            BlockType.NO_MATCH_AREA);
                                    pendingNoMatches.remove(bLocation);
                                } else if (pendingWraps.containsKey(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            wrapCode(pendingWraps.get(bLocation)));
                                    pendingWraps.remove(bLocation);
                                } else if (leaveSticky) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.STICKY);
                                } else if (currentGate > addedGate
                                        && Utility.chance(ADD_GATE_SWITCH_PROB)) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            currentGateSwitchCode());
                                    addedGate++;
                                } else if (opt.shift && Utility.chance(ADD_COLOR_SHIFT_PROB)
                                        && bType != BlockType.BLOCK_STONE) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            colorCodeToColorShift(b.getType()));
                                    newField.setBlockAt(b.getRow() + displacement, b.getCol(),
                                            randomColoredBlock());
                                } else {
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                                }

                                if (opt.wrap && Utility.chance(ADD_WRAP_PROB) && currentWrap < 4) {
                                    Point target;
                                    do {
                                        target = new Point(Utility.randBetween(1,
                                                GameField.BOARD_SIZE - 1),
                                                Utility.randBetween(1, GameField.BOARD_SIZE - 1));
                                    } while (isOccupied(newField, target.r, target.c));
                                    pendingWraps.put(target, currentWrap);
                                    fixed.add(target);
                                    newField.setBlockAt(target.r, target.c, bType);
                                    newField.setBlockAt(b.getRow() + displacement, b.getCol(),
                                            wrapCode(currentWrap));
                                    currentWrap++;
                                }

                                for (int i = b.getRow(); i <= b.getRow() + displacement; ++i) {
                                    emptyNeeded.add(new Point(i, b.getCol()));
                                    if (opt.arrow && Utility.chance(ADD_ARROW_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, i, b.getCol())) {
                                        enableOpposite = true;
                                        newField.setBlockAt(i, b.getCol(), BlockType.MOVE_UP);
                                    } else if (opt.gate && Utility.chance(ADD_GATE_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, i, b.getCol()) &&
                                            currentGate < 4 && !colorJustAdded) {
                                        newField.setBlockAt(i, b.getCol(), currentGateCode());
                                        currentGate++;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }

            case DOWN: {
                char forceNextDir = 0;
                for (int c = 0; c < GameField.BOARD_SIZE; ++c) {
                    for (int r = 0; r < GameField.BOARD_SIZE; ++r) {
                        Block b = newField.getBlockAt(r, c);
                        Point bLocation = new Point(r, c);
                        char bType = b == null ? BlockType.EMPTY : b.getType();
                        if (fixed.contains(bLocation))
                            continue;
                        if (b != null && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                            // clear required wall
                            if (!setRequiredWall(newField, b, 1, 0)) {
                                return false;
                            }

                            // clear required empties
                            int maxDisplacement = 0;
                            for (int i = b.getRow() - 1; i > 0; --i, ++maxDisplacement) {
                                Block current = newField.getBlockAt(i, b.getCol());
                                Block bottom = newField.getBottomLayerAt(i, b.getCol());
                                if ((current != null
                                        && BlockType.inArray(current.getType(),
                                        BlockType.SOLID_CODES)) ||
                                        (bottom != null &&
                                        (
                                        bottom.getType() == BlockType.MOVE_LEFT ||
                                                bottom.getType() == BlockType.MOVE_UP ||
                                                bottom.getType() == BlockType.MOVE_RIGHT ||
                                        bottom.getType() == BlockType.STICKY)
                                        )) {
                                    if (current != null) {
                                        if (current.getType() == BlockType.BLOCK_STONE) {
                                            break;
                                        }
                                        if (current.getType() == b.getType() ||
                                                current.getType() == BlockType.BLOCK_RAINBOW ||
                                                b.getType() == BlockType.BLOCK_RAINBOW) {
                                            maxDisplacement--;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (maxDisplacement < 0)
                                return false;

                            // move block
                            List<Integer> candidate = new ArrayList<Integer>();
                            List<Integer> needNoMatch = new ArrayList<Integer>();
                            for (int i = 0; i <= maxDisplacement; ++i) {
                                Block b1 = newField.getBlockAt(b.getRow() - i, b.getCol() - 1);
                                Block b2 = newField.getBlockAt(b.getRow() - i, b.getCol() + 1);
                                Block m1 = newField
                                        .getBottomLayerAt(b.getRow() - i, b.getCol());
                                if (m1 != null) {
                                    // do nothing
                                } else if (b1 != null &&
                                        (b1.getType() == b.getType() ||
                                                b1.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else if (b2 != null &&
                                        (b2.getType() == b.getType() ||
                                                b2.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else {
                                    if (requiredEmpty.contains(b1) && requiredEmpty.contains(b2)) {
                                        // do nothing
                                    } else if (requiredEmpty.contains(b1)) {
                                        if (forceNextDir != LEFT) {
                                            forceNextDir = RIGHT;
                                            candidate.add(i);
                                        }
                                    } else if (requiredEmpty.contains(b2)) {
                                        if (forceNextDir != RIGHT) {
                                            forceNextDir = LEFT;
                                            candidate.add(i);
                                        }
                                    } else {
                                        candidate.add(i);
                                    }
                                }
                            }

                            if (pendingWraps.containsKey(bLocation)) {
                                candidate.remove((Object) 0);
                            }

                            if (candidate.size() <= 0) {
                                return false;
                            }

                            int displacement = Utility.randomPick(candidate);

                            if (needNoMatch.contains(displacement)) {
                                pendingNoMatches.add(new Point(b.getRow() - displacement, b
                                        .getCol()));
                            }

                            boolean leaveSticky = false;
                            if (pendingStickies.contains(bLocation) &&
                                    !pendingNoMatches.contains(bLocation)) {
                                if (Utility.chance(STICKY_NOT_MOVE_PROB) && candidate.contains(0)) {
                                    displacement = 0;
                                } else {
                                    leaveSticky = true;
                                }
                            }

                            if (displacement > 0) {
                                boolean colorJustAdded = isColorJustAdded(b);
                                newField.setBlockAt(b.getRow() - displacement, b.getCol(),
                                        b.getType());
                                if (pendingNoMatches.contains(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            BlockType.NO_MATCH_AREA);
                                    pendingNoMatches.remove(bLocation);
                                } else if (pendingWraps.containsKey(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            wrapCode(pendingWraps.get(bLocation)));
                                    pendingWraps.remove(bLocation);
                                } else if (leaveSticky) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.STICKY);
                                } else if (currentGate > addedGate
                                        && Utility.chance(ADD_GATE_SWITCH_PROB)) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            currentGateSwitchCode());
                                    addedGate++;
                                } else if (opt.shift && Utility.chance(ADD_COLOR_SHIFT_PROB)
                                        && bType != BlockType.BLOCK_STONE) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            colorCodeToColorShift(b.getType()));
                                    newField.setBlockAt(b.getRow() - displacement, b.getCol(),
                                            randomColoredBlock());
                                } else {
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                                }

                                if (opt.wrap && Utility.chance(ADD_WRAP_PROB) && currentWrap < 4) {
                                    Point target;
                                    do {
                                        target = new Point(Utility.randBetween(1,
                                                GameField.BOARD_SIZE - 1),
                                                Utility.randBetween(1, GameField.BOARD_SIZE - 1));
                                    } while (isOccupied(newField, target.r, target.c));
                                    fixed.add(target);
                                    pendingWraps.put(target, currentWrap);
                                    newField.setBlockAt(target.r, target.c, bType);
                                    newField.setBlockAt(b.getRow() - displacement, b.getCol(),
                                            wrapCode(currentWrap));
                                    currentWrap++;
                                }

                                for (int i = b.getRow() - displacement; i <= b.getRow(); ++i) {
                                    emptyNeeded.add(new Point(i, b.getCol()));
                                    if (opt.arrow && Utility.chance(ADD_ARROW_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, i, b.getCol())) {
                                        enableOpposite = true;
                                        newField.setBlockAt(i, b.getCol(), BlockType.MOVE_DOWN);
                                    } else if (opt.gate && Utility.chance(ADD_GATE_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, i, b.getCol()) &&
                                            currentGate < 4 && !colorJustAdded) {
                                        newField.setBlockAt(i, b.getCol(), currentGateCode());
                                        currentGate++;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }

            case LEFT: {
                char forceNextDir = 0;
                for (int r = 0; r < GameField.BOARD_SIZE; ++r) {
                    for (int c = GameField.BOARD_SIZE - 1; c >= 0; --c) {
                        Block b = newField.getBlockAt(r, c);
                        Point bLocation = new Point(r, c);
                        char bType = b == null ? BlockType.EMPTY : b.getType();
                        if (fixed.contains(bLocation))
                            continue;
                        if (b != null && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                            // clear required wall
                            if (!setRequiredWall(newField, b, 0, -1)) {
                                return false;
                            }

                            // clear required empties
                            int maxDisplacement = 0;
                            for (int i = b.getCol() + 1; i <= GameField.BOARD_SIZE - 2; ++i, ++maxDisplacement) {
                                Block current = newField.getBlockAt(b.getRow(), i);
                                Block bottom = newField.getBottomLayerAt(b.getRow(), i);
                                if ((current != null
                                        && BlockType.inArray(current.getType(),
                                        BlockType.SOLID_CODES)) ||
                                        (bottom != null &&
                                        (
                                        bottom.getType() == BlockType.MOVE_UP ||
                                                bottom.getType() == BlockType.MOVE_DOWN ||
                                                bottom.getType() == BlockType.MOVE_RIGHT ||
                                        bottom.getType() == BlockType.STICKY)
                                        )) {
                                    if (current != null) {
                                        if (current.getType() == BlockType.BLOCK_STONE) {
                                            break;
                                        }
                                        if (current.getType() == b.getType() ||
                                                current.getType() == BlockType.BLOCK_RAINBOW ||
                                                b.getType() == BlockType.BLOCK_RAINBOW) {
                                            maxDisplacement--;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (maxDisplacement < 0)
                                return false;

                            // move block
                            List<Integer> candidate = new ArrayList<Integer>();
                            List<Integer> needNoMatch = new ArrayList<Integer>();
                            for (int i = 0; i <= maxDisplacement; ++i) {
                                Block b1 = newField.getBlockAt(b.getRow() - 1, b.getCol() + i);
                                Block b2 = newField.getBlockAt(b.getRow() + 1, b.getCol() + i);
                                Block m1 = newField
                                        .getBottomLayerAt(b.getRow(), b.getCol() + i);
                                if (m1 != null) {
                                    // do nothing
                                } else if (b1 != null &&
                                        (b1.getType() == b.getType() ||
                                                b1.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else if (b2 != null &&
                                        (b2.getType() == b.getType() ||
                                                b2.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else {
                                    if (requiredEmpty.contains(b1) && requiredEmpty.contains(b2)) {
                                        // do nothing
                                    } else if (requiredEmpty.contains(b1)) {
                                        if (forceNextDir != UP) {
                                            forceNextDir = DOWN;
                                            candidate.add(i);
                                        }
                                    } else if (requiredEmpty.contains(b2)) {
                                        if (forceNextDir != DOWN) {
                                            forceNextDir = UP;
                                            candidate.add(i);
                                        }
                                    } else {
                                        candidate.add(i);
                                    }
                                }
                            }

                            if (pendingWraps.containsKey(bLocation)) {
                                candidate.remove((Object) 0);
                            }

                            if (candidate.size() <= 0) {
                                return false;
                            }

                            int displacement = Utility.randomPick(candidate);

                            if (needNoMatch.contains(displacement)) {
                                pendingNoMatches.add(new Point(b.getRow(), b.getCol()
                                        + displacement));
                            }

                            boolean leaveSticky = false;
                            if (pendingStickies.contains(bLocation) &&
                                    !pendingNoMatches.contains(bLocation)) {
                                if (Utility.chance(STICKY_NOT_MOVE_PROB) && candidate.contains(0)) {
                                    displacement = 0;
                                } else {
                                    leaveSticky = true;
                                }
                            }

                            if (displacement > 0) {
                                boolean colorJustAdded = isColorJustAdded(b);
                                newField.setBlockAt(b.getRow(), b.getCol() + displacement,
                                        b.getType());
                                if (pendingNoMatches.contains(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            BlockType.NO_MATCH_AREA);
                                    pendingNoMatches.remove(bLocation);
                                } else if (pendingWraps.containsKey(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            wrapCode(pendingWraps.get(bLocation)));
                                    pendingWraps.remove(bLocation);
                                } else if (leaveSticky) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.STICKY);
                                } else if (currentGate > addedGate
                                        && Utility.chance(ADD_GATE_SWITCH_PROB)) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            currentGateSwitchCode());
                                    addedGate++;
                                } else if (opt.shift && Utility.chance(ADD_COLOR_SHIFT_PROB)
                                        && bType != BlockType.BLOCK_STONE) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            colorCodeToColorShift(b.getType()));
                                    newField.setBlockAt(b.getRow(), b.getCol() + displacement,
                                            randomColoredBlock());
                                } else {
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                                }

                                if (opt.wrap && Utility.chance(ADD_WRAP_PROB) && currentWrap < 4) {
                                    Point target;
                                    do {
                                        target = new Point(Utility.randBetween(1,
                                                GameField.BOARD_SIZE - 1),
                                                Utility.randBetween(1, GameField.BOARD_SIZE - 1));
                                    } while (isOccupied(newField, target.r, target.c));
                                    fixed.add(target);
                                    pendingWraps.put(target, currentWrap);
                                    newField.setBlockAt(target.r, target.c, bType);
                                    newField.setBlockAt(b.getRow(), b.getCol() + displacement,
                                            wrapCode(currentWrap));
                                    currentWrap++;
                                }

                                for (int i = b.getCol(); i <= b.getCol() + displacement; ++i) {
                                    emptyNeeded.add(new Point(b.getRow(), i));
                                    if (opt.arrow && Utility.chance(ADD_ARROW_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, b.getRow(), i)) {
                                        enableOpposite = true;
                                        newField.setBlockAt(b.getRow(), i, BlockType.MOVE_LEFT);
                                    } else if (opt.gate && Utility.chance(ADD_GATE_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, b.getRow(), i) &&
                                            currentGate < 4 && !colorJustAdded) {
                                        newField.setBlockAt(b.getRow(), i, currentGateCode());
                                        currentGate++;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }

            case RIGHT: {
                char forceNextDir = 0;
                for (int r = 0; r < GameField.BOARD_SIZE; ++r) {
                    for (int c = 0; c < GameField.BOARD_SIZE; ++c) {
                        Block b = newField.getBlockAt(r, c);
                        Point bLocation = new Point(r, c);
                        char bType = b == null ? BlockType.EMPTY : b.getType();
                        if (fixed.contains(bLocation))
                            continue;
                        if (b != null && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                            // clear required wall
                            if (!setRequiredWall(newField, b, 0, 1)) {
                                return false;
                            }

                            // clear required empties
                            int maxDisplacement = 0;
                            for (int i = b.getCol() - 1; i > 0; --i, ++maxDisplacement) {
                                Block current = newField.getBlockAt(b.getRow(), i);
                                Block bottom = newField.getBottomLayerAt(b.getRow(), i);
                                if ((current != null
                                        && BlockType.inArray(current.getType(),
                                        BlockType.SOLID_CODES)) ||
                                        (bottom != null &&
                                        (
                                        bottom.getType() == BlockType.MOVE_LEFT ||
                                                bottom.getType() == BlockType.MOVE_DOWN ||
                                                bottom.getType() == BlockType.MOVE_UP ||
                                        bottom.getType() == BlockType.STICKY)
                                        )) {
                                    if (current != null) {
                                        if (current.getType() == BlockType.BLOCK_STONE) {
                                            break;
                                        }
                                        if (current.getType() == b.getType() ||
                                                current.getType() == BlockType.BLOCK_RAINBOW ||
                                                b.getType() == BlockType.BLOCK_RAINBOW) {
                                            maxDisplacement--;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (maxDisplacement < 0)
                                return false;

                            // move block
                            List<Integer> candidate = new ArrayList<Integer>();
                            List<Integer> needNoMatch = new ArrayList<Integer>();
                            for (int i = 0; i <= maxDisplacement; ++i) {
                                Block b1 = newField.getBlockAt(b.getRow() - 1, b.getCol() - i);
                                Block b2 = newField.getBlockAt(b.getRow() + 1, b.getCol() - i);
                                Block m1 = newField
                                        .getBottomLayerAt(b.getRow(), b.getCol() - i);
                                if (m1 != null) {
                                    // do nothing
                                } else if (b1 != null &&
                                        (b1.getType() == b.getType() ||
                                                b1.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else if (b2 != null &&
                                        (b2.getType() == b.getType() ||
                                                b2.getType() == BlockType.BLOCK_RAINBOW ||
                                        b.getType() == BlockType.BLOCK_RAINBOW)) {
                                    if (opt.nomatch && i > 0) {
                                        candidate.add(i);
                                        needNoMatch.add(i);
                                    }
                                } else {
                                    if (requiredEmpty.contains(b1) && requiredEmpty.contains(b2)) {
                                        // do nothing
                                    } else if (requiredEmpty.contains(b1)) {
                                        if (forceNextDir != UP) {
                                            forceNextDir = DOWN;
                                            candidate.add(i);
                                        }
                                    } else if (requiredEmpty.contains(b2)) {
                                        if (forceNextDir != DOWN) {
                                            forceNextDir = UP;
                                            candidate.add(i);
                                        }
                                    } else {
                                        candidate.add(i);
                                    }
                                }
                            }

                            if (pendingWraps.containsKey(bLocation)) {
                                candidate.remove((Object) 0);
                            }

                            if (candidate.size() <= 0) {
                                return false;
                            }

                            int displacement = Utility.randomPick(candidate);

                            if (needNoMatch.contains(displacement)) {
                                pendingNoMatches.add(new Point(b.getRow(), b.getCol()
                                        - displacement));
                            }

                            boolean leaveSticky = false;
                            if (pendingStickies.contains(bLocation) &&
                                    !pendingNoMatches.contains(bLocation)) {
                                if (Utility.chance(STICKY_NOT_MOVE_PROB) && candidate.contains(0)) {
                                    displacement = 0;
                                } else {
                                    leaveSticky = true;
                                }
                            }

                            if (displacement > 0) {
                                boolean colorJustAdded = isColorJustAdded(b);
                                newField.setBlockAt(b.getRow(), b.getCol() - displacement,
                                        b.getType());
                                if (pendingNoMatches.contains(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            BlockType.NO_MATCH_AREA);
                                    pendingNoMatches.remove(bLocation);
                                } else if (pendingWraps.containsKey(bLocation)) {
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            wrapCode(pendingWraps.get(bLocation)));
                                    pendingWraps.remove(bLocation);
                                } else if (leaveSticky) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.STICKY);
                                } else if (currentGate > addedGate
                                        && Utility.chance(ADD_GATE_SWITCH_PROB)) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            currentGateSwitchCode());
                                    addedGate++;
                                } else if (opt.shift && Utility.chance(ADD_COLOR_SHIFT_PROB)
                                        && bType != BlockType.BLOCK_STONE) {
                                    enableOpposite = true;
                                    newField.setBlockAt(b.getRow(), b.getCol(),
                                            colorCodeToColorShift(b.getType()));
                                    newField.setBlockAt(b.getRow(), b.getCol() - displacement,
                                            randomColoredBlock());
                                } else {
                                    newField.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                                }

                                if (opt.wrap && Utility.chance(ADD_WRAP_PROB) && currentWrap < 4) {
                                    Point target;
                                    do {
                                        target = new Point(Utility.randBetween(1,
                                                GameField.BOARD_SIZE - 1),
                                                Utility.randBetween(1, GameField.BOARD_SIZE - 1));
                                    } while (isOccupied(newField, target.r, target.c));
                                    fixed.add(target);
                                    pendingWraps.put(target, currentWrap);
                                    newField.setBlockAt(target.r, target.c, bType);
                                    newField.setBlockAt(b.getRow(), b.getCol() - displacement,
                                            wrapCode(currentWrap));
                                    currentWrap++;
                                }

                                for (int i = b.getCol() - displacement; i <= b.getCol(); ++i) {
                                    emptyNeeded.add(new Point(b.getRow(), i));
                                    if (opt.arrow && Utility.chance(ADD_ARROW_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, b.getRow(), i)) {
                                        enableOpposite = true;
                                        newField.setBlockAt(b.getRow(), i, BlockType.MOVE_RIGHT);
                                    } else if (opt.gate && Utility.chance(ADD_GATE_ON_ROUTE_PROB) &&
                                            !isOccupied(newField, b.getRow(), i) &&
                                            currentGate < 4 && !colorJustAdded) {
                                        newField.setBlockAt(b.getRow(), i, currentGateCode());
                                        currentGate++;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }

        if (oldField.getAllBlocks().equals(newField.getAllBlocks())) {
            return false;
        }

        oldField.setField(newField);
        requiredEmpty.addAll(emptyNeeded);

        return true;
    }

    private boolean verifySolution(GameField f, List<Character> solution) {
        GameField verify = new GameField(f);
        for (char c : solution) {
            switch (c) {
                case UP:
                    verify.moveUp();
                    break;
                case DOWN:
                    verify.moveDown();
                    break;
                case LEFT:
                    verify.moveLeft();
                    break;
                case RIGHT:
                    verify.moveRight();
                    break;
            }
        }

        return verify.checkComplete();
    }

    @Override
    public GameField generate(GeneratorOption opt) {
        GameField f;

        boolean ok;
        char lastDir;

        this.opt = opt;

        List<Character> solution = new ArrayList<Character>();
        do {
            int color = 1;
            int colorCnt = Utility.randBetween(opt.colorLo, Math.min(4, opt.colorHi));

            ok = true;
            lastDir = 0;
            solution.clear();
            requiredEmpty.clear();
            pendingStickies.clear();
            pendingNoMatches.clear();
            pendingWraps.clear();
            currentGate = 0;
            addedGate = 0;
            currentWrap = 0;

            int steps = Utility.randBetween(opt.stepLo, opt.stepHi);

            f = new GameField();
            f.setStepLimit(steps);

            int blockCnt = Utility.randBetween(2, 3);
            addEliminatedBlocks(f, color, blockCnt, false, false);
            colorJustAdded.add(color);
            color++;

            if (debug) {
                System.out.println(f.getLevelData());
                System.out.println(solution);
            }

            int actualSteps;
            for (actualSteps = 0; actualSteps < steps; ++actualSteps) {
                List<Character> directions = new ArrayList<Character>();
                if (lastDir == 0) {
                    directions.add(UP);
                    directions.add(DOWN);
                    directions.add(LEFT);
                    directions.add(RIGHT);
                } else if (lastDir == UP || lastDir == DOWN) {
                    if (enableOpposite) {
                        if (lastDir == UP) {
                            directions.add(DOWN);
                        } else if (lastDir == DOWN) {
                            directions.add(UP);
                        }
                    }
                    directions.add(LEFT);
                    directions.add(RIGHT);
                } else if (lastDir == LEFT || lastDir == RIGHT) {
                    if (enableOpposite) {
                        if (lastDir == LEFT) {
                            directions.add(RIGHT);
                        } else if (lastDir == RIGHT) {
                            directions.add(LEFT);
                        }
                    }
                    directions.add(UP);
                    directions.add(DOWN);
                }

                enableOpposite = false;

                if (color <= colorCnt
                        && Utility.chance(300 / Math.max(1, opt.stepLo - actualSteps))) {
                    blockCnt = Utility.randBetween(2, 3);
                    addEliminatedBlocks(f, color, blockCnt, false, false);
                    colorJustAdded.add(color);
                    color++;
                }

                Collections.shuffle(directions);

                boolean moved = false;
                for (char dir : directions) {
                    if (generateBackwardStep(f, dir)) {
                        lastDir = dir;
                        moved = true;
                        break;
                    }
                }

                colorJustAdded.clear();

                if (!moved) {
                    if (actualSteps < opt.stepLo) {
                        ok = false;
                    }
                    break;
                }

                solution.add(lastDir);

                if (debug) {
                    System.out.println(f.getLevelData());
                    System.out.println(solution);
                }

            }

            if (ok) {
                f.setStepLimit(actualSteps);
                if (pendingNoMatches.size() > 0) {
                    ok = false;
                } else {
                    if (opt.gate) {
                        boolean[] gates = new boolean[4];
                        for (Block b : f.getAllBlocks()) {
                            switch (b.getType()) {
                                case BlockType.GATE_SWITCH_1:
                                    gates[0] = true;
                                    break;
                                case BlockType.GATE_SWITCH_2:
                                    gates[1] = true;
                                    break;
                                case BlockType.GATE_SWITCH_3:
                                    gates[2] = true;
                                    break;
                                case BlockType.GATE_SWITCH_4:
                                    gates[3] = true;
                                    break;
                            }
                        }

                        for (Block b : f.getAllBlocks()) {
                            if (b.getType() == BlockType.GATE_1 && !gates[0]) {
                                f.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                            } else if (b.getType() == BlockType.GATE_2 && !gates[1]) {
                                f.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                            } else if (b.getType() == BlockType.GATE_3 && !gates[2]) {
                                f.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                            } else if (b.getType() == BlockType.GATE_4 && !gates[3]) {
                                f.setBlockAt(b.getRow(), b.getCol(), BlockType.EMPTY);
                            }
                        }
                    }

                    if (opt.wrap && pendingWraps.size() > 0) {
                        for (Map.Entry<Point, Integer> i : pendingWraps.entrySet()) {
                            char wrapCode = wrapCode(i.getValue());
                            for (Block b : f.getAllBlocks()) {
                                if (b.getType() == wrapCode) {
                                    if (f.getBlockAt(i.getKey().r, i.getKey().c) != null) {
                                        f.setBlockAt(b.getRow(), b.getCol(),
                                                f.getBlockAt(i.getKey().r, i.getKey().c).getType());
                                    }
                                    f.setBlockAt(i.getKey().r, i.getKey().c, BlockType.EMPTY);
                                    break;
                                }
                            }
                        }
                    }

                    GameField old = new GameField(f);
                    Utility.addWallsAroundArea(old, requiredEmpty);

                    int extraSpace = Utility.randBetween(0, 20);
                    for (int i = 0; i < extraSpace; ++i) {
                        Set<Point> edge = Utility.getEdgeArea(requiredEmpty);
                        Point space = Utility.randomPick(edge);
                        if (!isOccupied(f, space.r, space.c)) {
                            if (opt.sticky && Utility.chance(15)) {
                                f.setBlockAt(space.r, space.c, BlockType.STICKY);
                            } else if (opt.nomatch && Utility.chance(15)) {
                                f.setBlockAt(space.r, space.c, BlockType.NO_MATCH_AREA);
                            } else if (opt.shift && Utility.chance(15)) {
                                List<Character> shifts = new ArrayList<Character>();
                                shifts.add(BlockType.SHIFT_1);
                                shifts.add(BlockType.SHIFT_2);
                                shifts.add(BlockType.SHIFT_3);
                                shifts.add(BlockType.SHIFT_4);
                                shifts.add(BlockType.SHIFT_RAINBOW);
                                f.setBlockAt(space.r, space.c, Utility.randomPick(shifts));
                            }
                            requiredEmpty.add(space);
                        }
                    }

                    Utility.addWallsAroundArea(f, requiredEmpty);

                    Collections.reverse(solution);

                    if (!verifySolution(f, solution)) {
                        if (!verifySolution(old, solution)) {
                            ok = false;
                        } else {
                            f = old;
                        }
                    }
                    if (f.solvableInSteps(opt.optimal ? actualSteps - 1 : 1)) {
                        ok = false;
                    }
                }
            }

        } while (!ok);

        char[] solc = new char[solution.size()];
        for (int i = 0; i < solc.length; ++i) {
            solc[i] = solution.get(i);
        }
        f.setSolution(solc);

        return f;
    }

}
