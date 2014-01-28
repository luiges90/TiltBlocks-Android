package com.luiges90.tiltblocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameField implements Serializable {

    private static final long serialVersionUID = -2907410361064153250L;

    public static final int BOARD_SIZE = 12;
    public static final int LEVEL_COUNT = 100;

    private List<Block> blocks = new ArrayList<Block>();
    private int level;
    private int stepLimit;
    private int stepRemain;

    private boolean solutionFound = false;
    private char[] solution = null;

    private boolean anyBlockEliminated = false;
    private boolean anyBlockWrapped = false;
    private boolean anyBlockCrossedArrow = false;
    private boolean anyBlockSticked = false;
    private boolean anyBlockShifted = false;

    private char lastStep;

    public static final char RIGHT = '→';
    public static final char DOWN = '↓';
    public static final char LEFT = '←';
    public static final char UP = '↑';

    public GameField(GameField f) {
        this.level = f.level;
        this.stepLimit = f.stepLimit;
        this.stepRemain = f.stepRemain;
        this.solutionFound = false;

        this.blocks = new ArrayList<Block>();
        for (Block b : f.blocks) {
            this.blocks.add(new Block(b));
        }
    }

    public GameField() {
        this.level = -1;
        this.blocks = new ArrayList<Block>();
        this.stepLimit = 0;
        this.stepRemain = 0;
        this.solutionFound = false;
    }

    public GameField(String levelStr, int level) {
        if (level >= LEVEL_COUNT)
            level = 0;

        this.level = level;
        this.solutionFound = false;

        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                char type = levelStr.charAt(i * BOARD_SIZE + j);
                if (type != BlockType.EMPTY) {
                    blocks.add(new Block(levelStr.charAt(i * BOARD_SIZE + j), i, j));
                }
            }
        }

        stepLimit = Integer.parseInt(levelStr.substring(BOARD_SIZE * BOARD_SIZE));
        stepRemain = stepLimit;
    }

    public final String getLevelNameString() {
        return getLevelNameString(level);
    }

    public static final String getLevelNameString(int level) {
        return (level / 10 + 1) + "-" + (level % 10 + 1);
    }

    public void setStepLimit(int remain) {
        if (solution != null) {
            if (remain > stepRemain) {
                if (solution.length == 0) {
                    solutionFound = false;
                }
            } else if (remain < stepRemain) {
                if (solution.length > remain) {
                    solution = new char[0];
                }
            }
        }
        stepRemain = remain;
        stepLimit = remain;
    }

    public Block getAnyBlockAt(int r, int c) {
        for (Block b : blocks) {
            if (b.getRow() == r && b.getCol() == c && !b.isEliminated()) {
                return b;
            }
        }
        return null;
    }

    public Block getBlockAt(int r, int c) {
        for (Block b : blocks) {
            if (b.getRow() == r && b.getCol() == c && !b.isEliminated() &&
                    !BlockType.inArray(b.getType(), BlockType.BOTTOM_LAYER_CODES)) {
                return b;
            }
        }
        return null;
    }

    public Block getBottomLayerAt(int r, int c) {
        for (Block b : blocks) {
            if (b.getRow() == r && b.getCol() == c && !b.isEliminated() &&
                    BlockType.inArray(b.getType(), BlockType.BOTTOM_LAYER_CODES)) {
                return b;
            }
        }
        return null;
    }

    public List<Block> getAllBlocks() {
        List<Block> result = new ArrayList<Block>();
        for (Block b : blocks) {
            if (BlockType.inArray(b.getType(), BlockType.BOTTOM_LAYER_CODES)) {
                result.add(b);
            }
        }
        for (Block b : blocks) {
            if (!BlockType.inArray(b.getType(), BlockType.BOTTOM_LAYER_CODES)) {
                result.add(b);
            }
        }
        return result;
    }

    public boolean setBlockAt(int r, int c, char code) {
        if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE)
            return false;

        this.solutionFound = false;

        Block oldBlock = getAnyBlockAt(r, c);
        if (oldBlock == null) {
            if (code != BlockType.EMPTY) {
                Block newBlock = new Block(code, r, c);
                blocks.add(newBlock);
            }
        } else {
            if (code == BlockType.EMPTY) {
                blocks.remove(oldBlock);
            } else {
                oldBlock.setType(code);
                oldBlock.setShownType(code);
            }
        }

        return true;
    }

    public void setField(GameField f) {
        this.blocks = f.getAllBlocks();
        this.stepLimit = f.stepLimit;
        this.solutionFound = false;
    }

    public static int getLevelDataLength() {
        return BOARD_SIZE * BOARD_SIZE + 2;
    }

    public String getLevelData() {
        char[][] map = new char[BOARD_SIZE][BOARD_SIZE];
        for (Block b : blocks) {
            map[b.getRow()][b.getCol()] = b.getType();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                char c = map[i][j];
                if (c == 0) {
                    sb.append(BlockType.EMPTY);
                } else {
                    sb.append(map[i][j]);
                }
            }
        }

        int limit = this.getStepLimit();

        sb.append((limit < 10 ? "0" : "") + limit);
        return sb.toString();
    }

    protected void moveBlock(Block b, int newR, int newC) {
        b.setRow(newR);
        b.setCol(newC);
    }

    protected void eliminateBlock(Block b) {
        b.setEliminated(true);
        anyBlockEliminated = true;
    }

    protected void changeBlock(Block b, char newCode) {
        if (b.getType() != newCode) {
            b.setType(newCode);
        }
    }

    protected void wrapBlock(Block b, int newR, int newC) {
        b.setRow(newR);
        b.setCol(newC);

        anyBlockWrapped = true;
    }

    public int moveBlocks(int dirR, int dirC) {
        int moved = 0;

        // moving left
        if (dirC < 0) {
            for (int r = 0; r < BOARD_SIZE; ++r) {
                for (int c = 0; c < BOARD_SIZE; ++c) {
                    Block b = this.getBlockAt(r, c);
                    if (b != null && !b.isEliminated()
                            && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                        Block bottom = this.getBottomLayerAt(r, c);
                        if (bottom == null || bottom.getType() != BlockType.STICKY) {
                            for (int k = c - 1;; --k) {
                                Block nextBlock = getBlockAt(r, k);
                                Block nextBottom = getBottomLayerAt(r, k);
                                char next = nextBlock == null ? BlockType.EMPTY : nextBlock
                                        .getType();
                                char nextB = nextBottom == null ? BlockType.EMPTY : nextBottom
                                        .getType();
                                if (k == -1 ||
                                        BlockType.inArray(next, BlockType.SOLID_CODES) ||
                                        BlockType.inArray(nextB, new char[] {
                                                BlockType.MOVE_UP,
                                                BlockType.MOVE_DOWN,
                                                BlockType.MOVE_RIGHT
                                        })) {
                                    moveBlock(b, r, k + 1);
                                    moved++;
                                    break;
                                } else if (nextB == BlockType.STICKY) {
                                    moveBlock(b, r, k);
                                    moved++;
                                    anyBlockSticked = true;
                                    break;
                                } else if (nextB == BlockType.MOVE_LEFT) {
                                    anyBlockCrossedArrow = true;
                                } else if (BlockType.inArray(nextB, BlockType.SHIFT_CODES)) {
                                    anyBlockShifted = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // moving right
        if (dirC > 0) {
            for (int r = 0; r < BOARD_SIZE; ++r) {
                for (int c = BOARD_SIZE - 1; c >= 0; --c) {
                    Block b = this.getBlockAt(r, c);
                    if (b != null && !b.isEliminated()
                            && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                        Block bottom = this.getBottomLayerAt(r, c);
                        if (bottom == null || bottom.getType() != BlockType.STICKY) {
                            for (int k = c + 1;; ++k) {
                                Block nextBlock = getBlockAt(r, k);
                                Block nextBottom = getBottomLayerAt(r, k);
                                char next = nextBlock == null ? BlockType.EMPTY : nextBlock
                                        .getType();
                                char nextB = nextBottom == null ? BlockType.EMPTY : nextBottom
                                        .getType();
                                if (k == BOARD_SIZE ||
                                        BlockType.inArray(next, BlockType.SOLID_CODES) ||
                                        BlockType.inArray(nextB, new char[] {
                                                BlockType.MOVE_UP,
                                                BlockType.MOVE_DOWN,
                                                BlockType.MOVE_LEFT
                                        })) {
                                    moveBlock(b, r, k - 1);
                                    moved++;
                                    break;
                                } else if (nextB == BlockType.STICKY) {
                                    moveBlock(b, r, k);
                                    moved++;
                                    anyBlockSticked = true;
                                    break;
                                } else if (nextB == BlockType.MOVE_RIGHT) {
                                    anyBlockCrossedArrow = true;
                                } else if (BlockType.inArray(nextB, BlockType.SHIFT_CODES)) {
                                    anyBlockShifted = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // moving up
        if (dirR < 0) {
            for (int c = 0; c < BOARD_SIZE; ++c) {
                for (int r = 0; r < BOARD_SIZE; ++r) {
                    Block b = this.getBlockAt(r, c);
                    if (b != null && !b.isEliminated()
                            && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                        Block bottom = this.getBottomLayerAt(r, c);
                        if (bottom == null || bottom.getType() != BlockType.STICKY) {
                            for (int k = r - 1;; --k) {
                                Block nextBlock = getBlockAt(k, c);
                                Block nextBottom = getBottomLayerAt(k, c);
                                char next = nextBlock == null ? BlockType.EMPTY : nextBlock
                                        .getType();
                                char nextB = nextBottom == null ? BlockType.EMPTY : nextBottom
                                        .getType();
                                if (k == -1 ||
                                        BlockType.inArray(next, BlockType.SOLID_CODES) ||
                                        BlockType.inArray(nextB, new char[] {
                                                BlockType.MOVE_LEFT,
                                                BlockType.MOVE_DOWN,
                                                BlockType.MOVE_RIGHT
                                        })) {
                                    moveBlock(b, k + 1, c);
                                    moved++;
                                    break;
                                } else if (nextB == BlockType.STICKY) {
                                    moveBlock(b, k, c);
                                    moved++;
                                    anyBlockSticked = true;
                                    break;
                                } else if (nextB == BlockType.MOVE_UP) {
                                    anyBlockCrossedArrow = true;
                                } else if (BlockType.inArray(nextB, BlockType.SHIFT_CODES)) {
                                    anyBlockShifted = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // moving down
        if (dirR > 0) {
            for (int c = 0; c < BOARD_SIZE; ++c) {
                for (int r = BOARD_SIZE - 1; r >= 0; --r) {
                    Block b = this.getBlockAt(r, c);
                    if (b != null && !b.isEliminated()
                            && BlockType.inArray(b.getType(), BlockType.MOVABLE_CODES)) {
                        Block bottom = this.getBottomLayerAt(r, c);
                        if (bottom == null || bottom.getType() != BlockType.STICKY) {
                            for (int k = r + 1;; ++k) {
                                Block nextBlock = getBlockAt(k, c);
                                Block nextBottom = getBottomLayerAt(k, c);
                                char next = nextBlock == null ? BlockType.EMPTY : nextBlock
                                        .getType();
                                char nextB = nextBottom == null ? BlockType.EMPTY : nextBottom
                                        .getType();
                                if (k == BOARD_SIZE ||
                                        BlockType.inArray(next, BlockType.SOLID_CODES) ||
                                        BlockType.inArray(nextB, new char[] {
                                                BlockType.MOVE_LEFT,
                                                BlockType.MOVE_UP,
                                                BlockType.MOVE_RIGHT
                                        })) {
                                    moveBlock(b, k - 1, c);
                                    moved++;
                                    break;
                                } else if (nextB == BlockType.STICKY) {
                                    moveBlock(b, k, c);
                                    moved++;
                                    anyBlockSticked = true;
                                    break;
                                } else if (nextB == BlockType.MOVE_DOWN) {
                                    anyBlockCrossedArrow = true;
                                } else if (BlockType.inArray(nextB, BlockType.SHIFT_CODES)) {
                                    anyBlockShifted = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return moved;
    }

    private void eliminateBlocks_r(List<Block> result, boolean[][] visited, Block block) {
        int r = block.getRow();
        int c = block.getCol();

        visited[r][c] = true;

        Block bottom = getBottomLayerAt(r, c);
        char code = block == null ? BlockType.EMPTY : block.getType();
        char bottomCode = bottom == null ? BlockType.EMPTY : bottom.getType();

        if (bottomCode == BlockType.NO_MATCH_AREA)
            return;
        result.add(block);

        if (code == BlockType.BLOCK_RAINBOW) {
            Block next;

            next = getBlockAt(r - 1, c);
            if (next != null && !next.isEliminated() &&
                    BlockType.inArray(next.getType(), BlockType.TARGET_BLOCK_CODES) &&
                    !visited[r - 1][c]) {
                eliminateBlocks_r(result, visited, next);
            }

            next = getBlockAt(r + 1, c);
            if (next != null && !next.isEliminated() &&
                    BlockType.inArray(next.getType(), BlockType.TARGET_BLOCK_CODES) &&
                    !visited[r + 1][c]) {
                eliminateBlocks_r(result, visited, next);
            }

            next = getBlockAt(r, c - 1);
            if (next != null && !next.isEliminated() &&
                    BlockType.inArray(next.getType(), BlockType.TARGET_BLOCK_CODES) &&
                    !visited[r][c - 1]) {
                eliminateBlocks_r(result, visited, next);
            }

            next = getBlockAt(r, c + 1);
            if (next != null && !next.isEliminated() &&
                    BlockType.inArray(next.getType(), BlockType.TARGET_BLOCK_CODES) &&
                    !visited[r][c + 1]) {
                eliminateBlocks_r(result, visited, next);
            }
        } else {
            Block next;

            next = getBlockAt(r - 1, c);
            if (next != null && !next.isEliminated()
                    && (next.getType() == code || next.getType() == BlockType.BLOCK_RAINBOW) &&
                    !visited[r - 1][c]) {
                eliminateBlocks_r(result, visited, next);
            }

            next = getBlockAt(r + 1, c);
            if (next != null && !next.isEliminated()
                    && (next.getType() == code || next.getType() == BlockType.BLOCK_RAINBOW) &&
                    !visited[r + 1][c]) {
                eliminateBlocks_r(result, visited, next);
            }

            next = getBlockAt(r, c - 1);
            if (next != null && !next.isEliminated()
                    && (next.getType() == code || next.getType() == BlockType.BLOCK_RAINBOW) &&
                    !visited[r][c - 1]) {
                eliminateBlocks_r(result, visited, next);
            }

            next = getBlockAt(r, c + 1);
            if (next != null && !next.isEliminated()
                    && (next.getType() == code || next.getType() == BlockType.BLOCK_RAINBOW) &&
                    !visited[r][c + 1]) {
                eliminateBlocks_r(result, visited, next);
            }
        }
    }

    public int eliminateBlocks() {
        return eliminateBlocks(true);
    }

    public int eliminateBlocks(boolean includeGates) {
        int eliminated = 0;

        for (Block block : blocks) {
            if (block.isEliminated()) {
                continue;
            }

            Block bottom = getBottomLayerAt(block.getRow(), block.getCol());
            char bottomCode = bottom == null ? BlockType.EMPTY : bottom.getType();

            if (BlockType.inArray(block.getType(), BlockType.TARGET_BLOCK_CODES) &&
                    bottomCode != BlockType.NO_MATCH_AREA) {
                boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
                List<Block> toEliminate = new ArrayList<Block>();
                eliminateBlocks_r(toEliminate, visited, block);

                if (toEliminate.size() > 1) {
                    for (Block b : toEliminate) {
                        eliminateBlock(b);
                        eliminated++;
                    }
                }
            }

            if (includeGates) {
                if (BlockType.inArray(block.getType(), BlockType.MOVABLE_CODES)) {
                    eliminated += eliminateGates(bottomCode, BlockType.GATE_SWITCH_1,
                            BlockType.GATE_1);
                    eliminated += eliminateGates(bottomCode, BlockType.GATE_SWITCH_2,
                            BlockType.GATE_2);
                    eliminated += eliminateGates(bottomCode, BlockType.GATE_SWITCH_3,
                            BlockType.GATE_3);
                    eliminated += eliminateGates(bottomCode, BlockType.GATE_SWITCH_4,
                            BlockType.GATE_4);
                }
            }
        }

        return eliminated;
    }

    private int eliminateGates(char bottom, char gateSwitch, char gate) {
        int eliminated = 0;
        if (bottom == gateSwitch) {
            for (Block b : blocks) {
                if (!b.isEliminated() && b.getType() == gate) {
                    eliminateBlock(b);
                    eliminated++;
                }
            }
        }
        return eliminated;
    }

    public void moveWalls() {
        for (Block block : blocks) {
            if (block.getType() == BlockType.WALL_MOVE_UP) {
                Block next = getBlockAt(block.getRow() - 1, block.getCol());
                if (block.getRow() > 0 &&
                        (next == null || !BlockType.inArray(next.getType(), BlockType.SOLID_CODES))) {
                    moveBlock(block, block.getRow() - 1, block.getCol());
                } else {
                    Block prev = getBlockAt(block.getRow() + 1, block.getCol());
                    if (prev == null || !BlockType.inArray(prev.getType(), BlockType.SOLID_CODES)) {
                        block.setType(BlockType.WALL_MOVE_DOWN);
                    }
                }
            }

            if (block.getType() == BlockType.WALL_MOVE_DOWN) {
                Block next = getBlockAt(block.getRow() + 1, block.getCol());
                if (block.getRow() > 0 &&
                        (next == null || !BlockType.inArray(next.getType(), BlockType.SOLID_CODES))) {
                    moveBlock(block, block.getRow() + 1, block.getCol());
                } else {
                    Block prev = getBlockAt(block.getRow() - 1, block.getCol());
                    if (prev == null || !BlockType.inArray(prev.getType(), BlockType.SOLID_CODES)) {
                        block.setType(BlockType.WALL_MOVE_UP);
                        moveBlock(block, block.getRow() - 1, block.getCol());
                    }
                }
            }

            if (block.getType() == BlockType.WALL_MOVE_LEFT) {
                Block next = getBlockAt(block.getRow(), block.getCol() - 1);
                if (block.getRow() > 0 &&
                        (next == null || !BlockType.inArray(next.getType(), BlockType.SOLID_CODES))) {
                    moveBlock(block, block.getRow(), block.getCol() - 1);
                } else {
                    Block prev = getBlockAt(block.getRow(), block.getCol() + 1);
                    if (prev == null || !BlockType.inArray(prev.getType(), BlockType.SOLID_CODES)) {
                        block.setType(BlockType.WALL_MOVE_RIGHT);
                    }
                }
            }

            if (block.getType() == BlockType.WALL_MOVE_RIGHT) {
                Block next = getBlockAt(block.getRow(), block.getCol() + 1);
                if (block.getRow() > 0 &&
                        (next == null || !BlockType.inArray(next.getType(), BlockType.SOLID_CODES))) {
                    moveBlock(block, block.getRow(), block.getCol() + 1);
                } else {
                    Block prev = getBlockAt(block.getRow(), block.getCol() - 1);
                    if (prev == null || !BlockType.inArray(prev.getType(), BlockType.SOLID_CODES)) {
                        block.setType(BlockType.WALL_MOVE_LEFT);
                        moveBlock(block, block.getRow(), block.getCol() - 1);
                    }
                }
            }
        }
    }

    public void changeBlocksColor() {
        for (Block block : blocks) {
            if (BlockType.inArray(block.getType(), BlockType.TARGET_BLOCK_CODES)
                    && !block.isEliminated()) {
                Block bottom = this.getBottomLayerAt(block.getRow(), block.getCol());
                if (bottom != null && BlockType.inArray(bottom.getType(), BlockType.SHIFT_CODES)) {
                    char shiftType;
                    switch (bottom.getType()) {
                        case BlockType.SHIFT_1:
                            shiftType = BlockType.BLOCK_1;
                            break;
                        case BlockType.SHIFT_2:
                            shiftType = BlockType.BLOCK_2;
                            break;
                        case BlockType.SHIFT_3:
                            shiftType = BlockType.BLOCK_3;
                            break;
                        case BlockType.SHIFT_4:
                            shiftType = BlockType.BLOCK_4;
                            break;
                        case BlockType.SHIFT_RAINBOW:
                            shiftType = BlockType.BLOCK_RAINBOW;
                            break;
                        default:
                            continue;
                    }
                    changeBlock(block, shiftType);
                }
            }
        }

    }

    public void wrapBlocks() {
        for (Block block : blocks) {
            if (BlockType.inArray(block.getType(), BlockType.MOVABLE_CODES)
                    && !block.isEliminated()) {
                Block bottom = this.getBottomLayerAt(block.getRow(), block.getCol());
                if (bottom != null && BlockType.inArray(bottom.getType(), BlockType.WRAP_CODES)) {
                    char wrapType = bottom.getType();
                    boolean done = false;
                    for (int i = block.getRow(); i < BOARD_SIZE; ++i) {
                        for (int j = (i == block.getRow() ? block.getCol() + 1 : 0); j < BOARD_SIZE; ++j) {
                            Block bottomWrap = this.getBottomLayerAt(i, j);
                            if (bottomWrap != null && bottomWrap.getType() == wrapType) {
                                wrapBlock(block, i, j);
                                done = true;
                                break;
                            }
                        }
                        if (done)
                            break;
                    }
                    if (!done) {
                        for (int i = 0; i <= block.getRow(); ++i) {
                            for (int j = 0; j < (i == block.getRow() ? block.getCol() : BOARD_SIZE); ++j) {
                                Block bottomWrap = this.getBottomLayerAt(i, j);
                                if (bottomWrap != null && bottomWrap.getType() == wrapType) {
                                    wrapBlock(block, i, j);
                                    done = true;
                                    break;
                                }
                            }
                            if (done)
                                break;
                        }
                    }
                }
            }
        }
    }

    public long makeStep(int dirR, int dirC) {
        anyBlockEliminated = false;
        anyBlockWrapped = false;
        anyBlockCrossedArrow = false;
        anyBlockSticked = false;
        anyBlockShifted = false;

        do {
            int moved = this.moveBlocks(dirR, dirC);
            if (moved <= 0) {
                break;
            }

            this.changeBlocksColor();

            int eliminated = this.eliminateBlocks();
            if (eliminated <= 0) {
                break;
            }

        } while (true);

        this.moveWalls();

        this.wrapBlocks();

        this.eliminateBlocks();

        return 0;
    }

    public boolean checkComplete() {
        for (Block b : blocks) {
            if (BlockType.inArray(b.getType(), BlockType.TARGET_BLOCK_CODES) && !b.isEliminated()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkFailure() {
        return stepRemain <= 0;
    }

    public int getStepLimit() {
        return stepLimit;
    }

    public int getStepRemain() {
        return stepRemain;
    }

    public boolean hasAnyBlockEliminated() {
        return anyBlockEliminated;
    }

    public boolean hasAnyBlockWrapped() {
        return anyBlockWrapped;
    }

    public boolean hasAnyBlockCrossedArrow() {
        return anyBlockCrossedArrow;
    }

    public boolean hasAnyBlockSticked() {
        return anyBlockSticked;
    }

    public boolean hasAnyBlockShifted() {
        return anyBlockShifted;
    }

    public char getLastStep() {
        return lastStep;
    }

    /**
     * Move field up
     * 
     * @return Time needed to animate the board, in milliseconds
     */
    public long moveUp() {
        if (lastStep == UP)
            return 0;
        lastStep = UP;
        stepRemain--;
        return makeStep(-1, 0);
    }

    /**
     * Move field down
     * 
     * @return Time needed to animate the board, in milliseconds
     */
    public long moveDown() {
        if (lastStep == DOWN)
            return 0;
        lastStep = DOWN;
        stepRemain--;
        return makeStep(1, 0);
    }

    /**
     * Move field left
     * 
     * @return Time needed to animate the board, in milliseconds
     */
    public long moveLeft() {
        if (lastStep == LEFT)
            return 0;
        lastStep = LEFT;
        stepRemain--;
        return makeStep(0, -1);
    }

    /**
     * Move field right
     * 
     * @return Time needed to animate the board, in milliseconds
     */
    public long moveRight() {
        if (lastStep == RIGHT)
            return 0;
        lastStep = RIGHT;
        stepRemain--;
        return makeStep(0, 1);
    }

    public boolean isSolutionFound() {
        return this.solutionFound;
    }

    public char[] solve() {
        if (this.solutionFound) {
            return this.solution;
        } else {
            char[] result = (new GameSolver(this)).solve();
            this.solution = result;
            this.solutionFound = true;
            return result;
        }
    }

    public boolean solvableInSteps(int step) {
        return (new GameSolver(this)).solvableInSteps(step);
    }

    public char[] solve(GameSolver.OnProgressListener listener) {
        if (this.solutionFound) {
            return this.solution;
        } else {
            char[] result = (new GameSolver(this, listener)).solve();
            this.solution = result;
            this.solutionFound = true;
            return result;
        }
    }

    public void setSolution(char[] s) {
        this.solution = s;
        this.solutionFound = true;
    }

    public void clearSolution() {
        this.solutionFound = false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
        result = prime * result + level;
        result = prime * result + stepLimit;
        result = prime * result + stepRemain;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof GameField))
            return false;
        GameField other = (GameField) obj;
        if (blocks == null) {
            if (other.blocks != null)
                return false;
        } else if (!blocks.equals(other.blocks))
            return false;
        if (level != other.level)
            return false;
        if (stepLimit != other.stepLimit)
            return false;
        if (stepRemain != other.stepRemain)
            return false;
        return true;
    }

    public boolean hasEqualFieldTo(GameField f) {
        return this.blocks.equals(f.blocks);
    }

    @Override
    public String toString() {
        return "GameField [blocks=" + blocks + ", level=" + level + ", stepLimit=" + stepLimit
                + ", stepRemain=" + stepRemain
                + ", anyBlockEliminated=" + anyBlockEliminated + ", anyBlockWrapped="
                + anyBlockWrapped + "]";
    }

}
