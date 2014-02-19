package com.luiges90.tiltblocks.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luiges90.tiltblocks.Block;
import com.luiges90.tiltblocks.BlockType;
import com.luiges90.tiltblocks.GameField;
import com.luiges90.tiltblocks.Point;

public class ForwardGameGenerator implements GameGeneratorInterface {

    public ForwardGameGenerator() {
    }

    private GeneratorOption opt;

    /**
     * Create the empty area as a bounded area of our puzzle
     * 
     * @param f
     *            Field. Edge will be drawn (as walls) into this field
     * @return Set of points defining our bounded area
     */
    private Set<Point> createEmptyArea(GameField f) {
        Set<Point> emptyCandidate = new HashSet<Point>();
        for (int i = 1; i < GameField.BOARD_SIZE - 1; ++i) {
            for (int j = 1; j < GameField.BOARD_SIZE - 1; ++j) {
                emptyCandidate.add(new Point(j, i));
            }
        }

        Set<Point> empty = Utility.getRandomArea(emptyCandidate, Utility.randBetween(20, 80));

        int wallCnt = Utility.randBetween(0, 5);
        for (int i = 0; i < wallCnt; ++i) {
            Set<Point> chosen = Utility.getRandomArea(empty,
                    Utility.randBetween(1, Math.max(1, empty.size() / 4 / wallCnt)));
            empty.removeAll(chosen);
        }

        Utility.addWallsAroundArea(f, empty);

        return empty;
    }

    /**
     * Add blocks randomly
     * 
     * @param f
     */
    private void addBlocks(GameField f, Set<Point> area) {
        int colorCnt = Utility.randBetween(opt.colorLo, opt.colorHi);
        for (int i = 0; i < colorCnt; ++i) {
            int blockCnt = Utility.randBetween(2, 3);
            for (int j = 0; j < blockCnt; ++j) {
                Point p = Utility.randomPick(area);

                char code;
                if (opt.rainbow && Utility.chance(30)) {
                    code = BlockType.BLOCK_RAINBOW;
                } else {
                    switch (i) {
                        case 0:
                            code = BlockType.BLOCK_1;
                            break;
                        case 1:
                            code = BlockType.BLOCK_2;
                            break;
                        case 2:
                            code = BlockType.BLOCK_3;
                            break;
                        case 3:
                            code = BlockType.BLOCK_4;
                            break;
                        default:
                            throw new AssertionError("color too large");
                    }
                }

                Block a = f.getBlockAt(p.r - 1, p.c);
                Block b = f.getBlockAt(p.r + 1, p.c);
                Block c = f.getBlockAt(p.r, p.c - 1);
                Block d = f.getBlockAt(p.r, p.c + 1);
                if ((a == null || a.getType() != code) &&
                        (b == null || b.getType() != code) &&
                        (c == null || c.getType() != code) &&
                        (d == null || d.getType() != code)) {
                    f.setBlockAt(p.r, p.c, code);
                    area.remove(p);
                } else {
                    j--;
                }

            }
        }

        if (opt.stone) {
            int cnt = Utility.randBetween(0, 3);
            for (int i = 0; i < cnt && area.size() > 0; ++i) {
                Point p = Utility.randomPick(area);
                f.setBlockAt(p.r, p.c, BlockType.BLOCK_STONE);
                area.remove(p);
            }
        }

        if (opt.arrow) {
            int cnt = Utility.randBetween(0, 6);
            for (int i = 0; i < cnt && area.size() > 0; ++i) {
                Point p = Utility.randomPick(area);

                List<Character> arrows = new ArrayList<Character>();
                arrows.add(BlockType.MOVE_UP);
                arrows.add(BlockType.MOVE_DOWN);
                arrows.add(BlockType.MOVE_LEFT);
                arrows.add(BlockType.MOVE_RIGHT);
                f.setBlockAt(p.r, p.c, Utility.randomPick(arrows));
                area.remove(p);
            }
        }

        if (opt.sticky) {
            int cnt = Utility.randBetween(0, 3);
            for (int i = 0; i < cnt && area.size() > 0; ++i) {
                Point p = Utility.randomPick(area);
                f.setBlockAt(p.r, p.c, BlockType.STICKY);
                area.remove(p);
            }
        }

        if (opt.gate) {
            int typeCnt = Utility.randBetween(0, 4);
            for (int i = 0; i < typeCnt && area.size() > 0; ++i) {
                int switchCnt = Utility.randBetween(1, 2);
                for (int j = 0; j < switchCnt && area.size() > 0; ++j) {
                    Point p = Utility.randomPick(area);

                    char code;
                    switch (i) {
                        case 0:
                            code = BlockType.GATE_SWITCH_1;
                            break;
                        case 1:
                            code = BlockType.GATE_SWITCH_2;
                            break;
                        case 2:
                            code = BlockType.GATE_SWITCH_3;
                            break;
                        case 3:
                            code = BlockType.GATE_SWITCH_4;
                            break;
                        default:
                            throw new AssertionError("color too large");
                    }

                    f.setBlockAt(p.r, p.c, code);
                    area.remove(p);
                }

                int eachCnt = Utility.randBetween(1, 8);
                for (int j = 0; j < eachCnt && area.size() > 0; ++j) {
                    Point p = Utility.randomPick(area);

                    char code;
                    switch (i) {
                        case 0:
                            code = BlockType.GATE_1;
                            break;
                        case 1:
                            code = BlockType.GATE_2;
                            break;
                        case 2:
                            code = BlockType.GATE_3;
                            break;
                        case 3:
                            code = BlockType.GATE_4;
                            break;
                        default:
                            throw new AssertionError("color too large");
                    }

                    f.setBlockAt(p.r, p.c, code);
                    area.remove(p);
                }
            }
        }

        if (opt.shift) {
            int cnt = Utility.randBetween(0, 3);
            for (int i = 0; i < cnt && area.size() > 0; ++i) {
                Point p = Utility.randomPick(area);

                List<Character> arrows = new ArrayList<Character>();
                arrows.add(BlockType.SHIFT_1);
                arrows.add(BlockType.SHIFT_2);
                arrows.add(BlockType.SHIFT_3);
                arrows.add(BlockType.SHIFT_4);
                arrows.add(BlockType.SHIFT_RAINBOW);
                f.setBlockAt(p.r, p.c, Utility.randomPick(arrows));
                area.remove(p);
            }
        }

        if (opt.nomatch) {
            int cnt = Utility.randBetween(0, 8);
            for (int i = 0; i < cnt && area.size() > 0; ++i) {
                Point p = Utility.randomPick(area);
                f.setBlockAt(p.r, p.c, BlockType.NO_MATCH_AREA);
                area.remove(p);
            }
        }

        if (opt.wrap) {
            int typeCnt = Utility.randBetween(0, 4);
            for (int i = 0; i < typeCnt && area.size() > 0; ++i) {
                int eachCnt = Utility.randBetween(2, 4);
                for (int j = 0; j < eachCnt && area.size() > 0; ++j) {
                    Point p = Utility.randomPick(area);

                    char code;
                    switch (i) {
                        case 0:
                            code = BlockType.WRAP_1;
                            break;
                        case 1:
                            code = BlockType.WRAP_2;
                            break;
                        case 2:
                            code = BlockType.WRAP_3;
                            break;
                        case 3:
                            code = BlockType.WRAP_4;
                            break;
                        default:
                            throw new AssertionError("color too large");
                    }

                    f.setBlockAt(p.r, p.c, code);
                    area.remove(p);
                }
            }
        }
    }

    public GameField generate(GeneratorOption opt) {
        this.opt = opt;
        do {
            GameField f = new GameField();
            f.setStepLimit(opt.stepHi);

            Set<Point> area = createEmptyArea(f);
            addBlocks(f, area);

            char[] solution = f.solve();
            if (solution != null && solution.length >= opt.stepLo) {
                if (opt.optimal) {
                    f.setStepLimit(solution.length);
                }

                return f;
            }

        } while (true);
    }

}
