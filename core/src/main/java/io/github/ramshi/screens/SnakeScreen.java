package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SnakeScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final float MOVE_INTERVAL = 0.098f;
    private static final int ARENA_GRID_WIDTH = 14;
    private static final int ARENA_GRID_HEIGHT = 14;
    private static final float ARENA_P1_X = 88f;
    private static final float ARENA_P2_X = 488f;
    private static final float ARENA_Y = 108f;
    private static final float TILE_SIZE = 16f;

    private final InputHandler inputHandler = new InputHandler();
    private final Texture tile1 = new Texture("snake/tiles/Tile1.png");
    private final Texture tile2 = new Texture("snake/tiles/Tile2.png");
    private final SnakeArena arenaP1;
    private final SnakeArena arenaP2;
    private float moveTimer = 0f;

    public SnakeScreen(Game game) {
        super(game);
        arenaP1 = new SnakeArena(1, ARENA_P1_X, ARENA_Y, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
        arenaP2 = new SnakeArena(2, ARENA_P2_X, ARENA_Y, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputHandler) Gdx.input.setInputProcessor(null);
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        Direction d1 = inputHandler.getP1Direction(arenaP1.getCurrentDirection());
        if (d1 != null) arenaP1.setNextDirection(d1);
        Direction d2 = inputHandler.getP2Direction(arenaP2.getCurrentDirection());
        if (d2 != null) arenaP2.setNextDirection(d2);

        moveTimer += delta;
        if (moveTimer >= MOVE_INTERVAL) {
            moveTimer -= MOVE_INTERVAL;
            arenaP1.tick();
            arenaP2.tick();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        arenaP1.render(batch);
        arenaP2.render(batch);
        batch.end();

        float w = ARENA_GRID_WIDTH * TILE_SIZE;
        float h = ARENA_GRID_HEIGHT * TILE_SIZE;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
        shapeRenderer.rect(ARENA_P1_X - 1f, ARENA_Y - 1f, w + 2f, h + 2f);
        shapeRenderer.rect(ARENA_P2_X - 1f, ARENA_Y - 1f, w + 2f, h + 2f);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(VIRTUAL_WIDTH / 2f, ARENA_Y, VIRTUAL_WIDTH / 2f, ARENA_Y + h);
        shapeRenderer.end();
    }

    @Override
    protected String determineRoundWinner() {
        int s1 = arenaP1.getScore();
        int s2 = arenaP2.getScore();
        if (s1 > s2) {
            int pts = (s1 - s2 >= 2 || s1 >= 3) ? 2 : 1;
            p1GlobalWins += pts;
            return "¡JUGADOR 1 GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
        }
        if (s2 > s1) {
            int pts = (s2 - s1 >= 2 || s2 >= 3) ? 2 : 1;
            p2GlobalWins += pts;
            return "¡JUGADOR 2 GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
        }
        return "¡EMPATE EN LA RONDA!";
    }

    @Override
    protected void onRoundReset() {
        moveTimer = 0f;
        inputHandler.reset();
        arenaP1.resetForNewRound();
        arenaP2.resetForNewRound();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + arenaP1.getScore() + " pts (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + arenaP2.getScore() + " pts (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Puntaje de ronda: P1 (" + arenaP1.getScore() + ") vs P2 (" + arenaP2.getScore() + ")";
    }

    @Override
    public void dispose() {
        hide();
        super.dispose();
        tile1.dispose();
        tile2.dispose();
        arenaP1.dispose();
        arenaP2.dispose();
    }

    private enum Direction {
        UP(0, 1),
        DOWN(0, -1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
        public boolean isOpposite(Direction other) {
            return other != null && this.dx + other.dx == 0 && this.dy + other.dy == 0;
        }
    }

    private static class SnakeSegment {
        public enum Type {
            HEAD,
            BODY,
            TAIL
        }

        private int gridX;
        private int gridY;
        private Type type;
        private Direction direction;

        public SnakeSegment(int gridX, int gridY, Type type, Direction direction) {
            this.gridX = gridX;
            this.gridY = gridY;
            this.type = type;
            this.direction = direction;
        }

        public int getGridX() {
            return gridX;
        }

        public void setGridX(int gridX) {
            this.gridX = gridX;
        }

        public int getGridY() {
            return gridY;
        }

        public void setGridY(int gridY) {
            this.gridY = gridY;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }
    }

    private static class Food implements Disposable {
        private int gridX;
        private int gridY;
        private final Texture texture;
        public Food() {
            this.texture = new Texture("snake/egg.png");
        }

        public void respawn(int minX, int minY, int maxX, int maxY, Snake snake) {
            int attempts = 0;
            int newX;
            int newY;

            do {
                newX = MathUtils.random(minX, maxX);
                newY = MathUtils.random(minY, maxY);
                attempts++;
            } while (snake != null && snake.occupies(newX, newY) && attempts < 100);

            this.gridX = newX;
            this.gridY = newY;
        }

        public void respawn(int gridWidth, int gridHeight, Snake snake) {
            respawn(0, 0, gridWidth - 1, gridHeight - 1, snake);
        }

        public void render(SpriteBatch batch, float tileSize, float offsetX, float offsetY) {
            batch.draw(texture, offsetX + gridX * tileSize, offsetY + gridY * tileSize, tileSize, tileSize);
        }

        public void render(SpriteBatch batch, float tileSize) {
            render(batch, tileSize, 0, 0);
        }

        public int getGridX() {
            return gridX;
        }

        public int getGridY() {
            return gridY;
        }

        @Override
        public void dispose() {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    private static class Snake implements Disposable {
        private final Map<Direction, Texture> headTextures = new EnumMap<>(Direction.class);
        private final Map<Direction, Texture> tailTextures = new EnumMap<>(Direction.class);

        private Texture bodyVertical;
        private Texture bodyHorizontal;

        private Texture cornerDownLeft;
        private Texture cornerUpLeft;
        private Texture cornerDownRight;
        private Texture cornerUpRight;

        private final List<SnakeSegment> segments = new ArrayList<>();
        private final int playerNumber;
        private int growPending = 0;

        public Snake() {
            this(1, 21, 14, Direction.RIGHT);
        }

        public Snake(int playerNumber, int startX, int startY, Direction initialDirection) {
            this.playerNumber = playerNumber;
            loadTextures(playerNumber);
            reset(startX, startY, initialDirection);
        }

        private void loadTextures(int player) {
            String p = "P" + player;

            headTextures.put(Direction.UP, new Texture("snake/head/Cabeza" + p + "_Up.png"));
            headTextures.put(Direction.DOWN, new Texture("snake/head/Cabeza" + p + "_Down.png"));
            headTextures.put(Direction.LEFT, new Texture("snake/head/Cabeza" + p + "_Left.png"));
            headTextures.put(Direction.RIGHT, new Texture("snake/head/Cabeza" + p + "_Right.png"));

            tailTextures.put(Direction.UP, new Texture("snake/tail/Cola" + p + "_Up.png"));
            tailTextures.put(Direction.DOWN, new Texture("snake/tail/Cola" + p + "_Down.png"));
            tailTextures.put(Direction.LEFT, new Texture("snake/tail/Cola" + p + "_Left.png"));
            tailTextures.put(Direction.RIGHT, new Texture("snake/tail/Cola" + p + "_Right.png"));

            bodyVertical = new Texture("snake/Body/Body" + p + ".png");
            bodyHorizontal = new Texture("snake/Body/Body" + p + "_1.png");

            cornerDownLeft = new Texture("snake/Body/Body" + p + "_2.png");
            cornerUpLeft = new Texture("snake/Body/Body" + p + "_3.png");
            cornerDownRight = new Texture("snake/Body/Body" + p + "_4.png");
            cornerUpRight = new Texture("snake/Body/Body" + p + "_5.png");
        }

        public void reset(int startX, int startY, Direction startDir) {
            segments.clear();
            growPending = 0;

            int dx = startDir.getDx();
            int dy = startDir.getDy();

            segments.add(new SnakeSegment(startX - 2 * dx, startY - 2 * dy, SnakeSegment.Type.TAIL, startDir));
            segments.add(new SnakeSegment(startX - dx, startY - dy, SnakeSegment.Type.BODY, startDir));
            segments.add(new SnakeSegment(startX, startY, SnakeSegment.Type.HEAD, startDir));
        }

        public void step(Direction direction) {
            SnakeSegment currentHead = getHead();

            Direction effectiveDirection = direction.isOpposite(currentHead.getDirection())
                ? currentHead.getDirection()
                : direction;

            int nextHeadX = currentHead.getGridX() + effectiveDirection.getDx();
            int nextHeadY = currentHead.getGridY() + effectiveDirection.getDy();

            currentHead.setType(SnakeSegment.Type.BODY);
            currentHead.setDirection(effectiveDirection);

            segments.add(new SnakeSegment(nextHeadX, nextHeadY, SnakeSegment.Type.HEAD, effectiveDirection));

            if (growPending > 0) {
                growPending--;
            } else {
                segments.remove(0);
            }

            updateTailOrientation();
        }

        private void updateTailOrientation() {
            if (segments.isEmpty()) return;

            SnakeSegment tail = segments.get(0);
            tail.setType(SnakeSegment.Type.TAIL);

            if (segments.size() > 1) {
                SnakeSegment next = segments.get(1);
                int dx = next.getGridX() - tail.getGridX();
                int dy = next.getGridY() - tail.getGridY();

                if (dx > 0) tail.setDirection(Direction.RIGHT);
                else if (dx < 0) tail.setDirection(Direction.LEFT);
                else if (dy > 0) tail.setDirection(Direction.UP);
                else if (dy < 0) tail.setDirection(Direction.DOWN);
            }
        }

        public void grow() {
            this.growPending++;
        }

        public SnakeSegment getHead() {
            return segments.get(segments.size() - 1);
        }

        public boolean checkSelfCollision() {
            SnakeSegment head = getHead();
            for (int i = 0; i < segments.size() - 1; i++) {
                SnakeSegment segment = segments.get(i);
                if (segment.getGridX() == head.getGridX() && segment.getGridY() == head.getGridY()) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkOutOfBounds(int gridWidth, int gridHeight) {
            SnakeSegment head = getHead();
            return head.getGridX() < 0 || head.getGridX() >= gridWidth ||
                   head.getGridY() < 0 || head.getGridY() >= gridHeight;
        }

        public boolean occupies(int x, int y) {
            for (SnakeSegment segment : segments) {
                if (segment.getGridX() == x && segment.getGridY() == y) {
                    return true;
                }
            }
            return false;
        }

        public void render(SpriteBatch batch, float offsetX, float offsetY) {
            int count = segments.size();
            for (int i = 0; i < count; i++) {
                SnakeSegment segment = segments.get(i);
                Texture textureToDraw = null;

                if (i == count - 1) {
                    textureToDraw = headTextures.get(segment.getDirection());
                } else if (i == 0) {
                    if (count > 1) {
                        SnakeSegment next = segments.get(1);
                        int dx = next.getGridX() - segment.getGridX();
                        int dy = next.getGridY() - segment.getGridY();
                        if (dx > 0) textureToDraw = tailTextures.get(Direction.RIGHT);
                        else if (dx < 0) textureToDraw = tailTextures.get(Direction.LEFT);
                        else if (dy > 0) textureToDraw = tailTextures.get(Direction.UP);
                        else textureToDraw = tailTextures.get(Direction.DOWN);
                    } else {
                        textureToDraw = tailTextures.get(segment.getDirection());
                    }
                } else {
                    SnakeSegment prev = segments.get(i - 1);
                    SnakeSegment next = segments.get(i + 1);

                    int dx1 = prev.getGridX() - segment.getGridX();
                    int dy1 = prev.getGridY() - segment.getGridY();
                    int dx2 = next.getGridX() - segment.getGridX();
                    int dy2 = next.getGridY() - segment.getGridY();

                    boolean hasLeft  = (dx1 == -1 || dx2 == -1);
                    boolean hasRight = (dx1 ==  1 || dx2 ==  1);
                    boolean hasUp    = (dy1 ==  1 || dy2 ==  1);
                    boolean hasDown  = (dy1 == -1 || dy2 == -1);

                    if (hasLeft && hasRight) {
                        textureToDraw = bodyHorizontal;
                    } else if (hasUp && hasDown) {
                        textureToDraw = bodyVertical;
                    } else if (hasLeft && hasDown) {
                        textureToDraw = cornerDownLeft;
                    } else if (hasLeft && hasUp) {
                        textureToDraw = cornerUpLeft;
                    } else if (hasRight && hasDown) {
                        textureToDraw = cornerDownRight;
                    } else if (hasRight && hasUp) {
                        textureToDraw = cornerUpRight;
                    } else {
                        textureToDraw = (segment.getDirection() == Direction.LEFT || segment.getDirection() == Direction.RIGHT)
                            ? bodyHorizontal : bodyVertical;
                    }
                }

                if (textureToDraw != null) {
                    float posX = offsetX + segment.getGridX() * TILE_SIZE;
                    float posY = offsetY + segment.getGridY() * TILE_SIZE;
                    batch.draw(textureToDraw, posX, posY, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        public void render(SpriteBatch batch) {
            render(batch, 0, 0);
        }

        public List<SnakeSegment> getSegments() {
            return segments;
        }

        public int getPlayerNumber() {
            return playerNumber;
        }

        @Override
        public void dispose() {
            for (Texture texture : headTextures.values()) {
                texture.dispose();
            }
            headTextures.clear();

            for (Texture texture : tailTextures.values()) {
                texture.dispose();
            }
            tailTextures.clear();

            if (bodyVertical != null) bodyVertical.dispose();
            if (bodyHorizontal != null) bodyHorizontal.dispose();
            if (cornerDownLeft != null) cornerDownLeft.dispose();
            if (cornerUpLeft != null) cornerUpLeft.dispose();
            if (cornerDownRight != null) cornerDownRight.dispose();
            if (cornerUpRight != null) cornerUpRight.dispose();
        }
    }

    private static class SnakeArena implements Disposable {
        private final int playerNumber;
        private final float originPixelX;
        private final float originPixelY;
        private final int gridWidth;
        private final int gridHeight;

        private final Snake snake;
        private final Food food;
        private final Texture tile1;
        private final Texture tile2;

        private Direction currentDirection;
        private Direction nextDirection;
        private int score = 0;

        private final int initialSpawnX;
        private final int initialSpawnY;
        private final Direction initialDirection;

        public SnakeArena(int playerNumber, float originPixelX, float originPixelY, int gridWidth, int gridHeight, Texture tile1, Texture tile2) {
            this.playerNumber = playerNumber;
            this.originPixelX = originPixelX;
            this.originPixelY = originPixelY;
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
            this.tile1 = tile1;
            this.tile2 = tile2;

            this.initialSpawnY = gridHeight / 2;
            if (playerNumber == 1) {
                this.initialSpawnX = 4;
                this.initialDirection = Direction.RIGHT;
            } else {
                this.initialSpawnX = gridWidth - 5;
                this.initialDirection = Direction.LEFT;
            }

            this.currentDirection = initialDirection;
            this.nextDirection = initialDirection;

            this.snake = new Snake(playerNumber, initialSpawnX, initialSpawnY, initialDirection);
            this.food = new Food();
            this.food.respawn(gridWidth, gridHeight, this.snake);
        }

        public void setNextDirection(Direction dir) {
            if (dir != null && !dir.isOpposite(currentDirection)) {
                this.nextDirection = dir;
            }
        }

        public Direction getCurrentDirection() {
            return currentDirection;
        }

        public Direction getNextDirection() {
            return nextDirection;
        }

        public void tick() {
            currentDirection = nextDirection;
            snake.step(currentDirection);

            if (snake.getHead().getGridX() == food.getGridX() && snake.getHead().getGridY() == food.getGridY()) {
                snake.grow();
                score++;
                food.respawn(gridWidth, gridHeight, snake);
            }

            if (snake.checkOutOfBounds(gridWidth, gridHeight) || snake.checkSelfCollision()) {
                respawnSnake();
            }
        }

        public void respawnSnake() {
            snake.reset(initialSpawnX, initialSpawnY, initialDirection);
            currentDirection = initialDirection;
            nextDirection = initialDirection;
            score = 0;
            food.respawn(gridWidth, gridHeight, snake);
        }

        public void resetForNewRound() {
            respawnSnake();
        }

        public void render(SpriteBatch batch) {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    Texture currentTile = ((x + y) % 2 == 0) ? tile1 : tile2;
                    batch.draw(currentTile, originPixelX + x * TILE_SIZE, originPixelY + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }

            food.render(batch, TILE_SIZE, originPixelX, originPixelY);

            snake.render(batch, originPixelX, originPixelY);
        }

        public int getScore() {
            return score;
        }

        public int getPlayerNumber() {
            return playerNumber;
        }

        public Snake getSnake() {
            return snake;
        }

        public Food getFood() {
            return food;
        }

        public float getOriginPixelX() {
            return originPixelX;
        }

        public float getOriginPixelY() {
            return originPixelY;
        }

        public int getGridWidth() {
            return gridWidth;
        }

        public int getGridHeight() {
            return gridHeight;
        }

        @Override
        public void dispose() {
            snake.dispose();
            food.dispose();
        }
    }

    private static class InputHandler extends InputAdapter {
        private Direction p1BufferedDirection = null;
        private Direction p2BufferedDirection = null;

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.W) {
                p1BufferedDirection = Direction.UP;
                return true;
            }
            if (keycode == Input.Keys.S) {
                p1BufferedDirection = Direction.DOWN;
                return true;
            }
            if (keycode == Input.Keys.A) {
                p1BufferedDirection = Direction.LEFT;
                return true;
            }
            if (keycode == Input.Keys.D) {
                p1BufferedDirection = Direction.RIGHT;
                return true;
            }

            if (keycode == Input.Keys.UP || keycode == Input.Keys.NUMPAD_8) {
                p2BufferedDirection = Direction.UP;
                return true;
            }
            if (keycode == Input.Keys.DOWN || keycode == Input.Keys.NUMPAD_2) {
                p2BufferedDirection = Direction.DOWN;
                return true;
            }
            if (keycode == Input.Keys.LEFT || keycode == Input.Keys.NUMPAD_4) {
                p2BufferedDirection = Direction.LEFT;
                return true;
            }
            if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.NUMPAD_6) {
                p2BufferedDirection = Direction.RIGHT;
                return true;
            }

            return false;
        }

        public Direction getP1Direction(Direction currentDirection) {
            Direction dir = p1BufferedDirection;
            p1BufferedDirection = null;

            if (dir == null) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.W) || (Gdx.input.isKeyPressed(Input.Keys.W) && currentDirection != Direction.UP && currentDirection != Direction.DOWN)) {
                    dir = Direction.UP;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.S) || (Gdx.input.isKeyPressed(Input.Keys.S) && currentDirection != Direction.DOWN && currentDirection != Direction.UP)) {
                    dir = Direction.DOWN;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.A) || (Gdx.input.isKeyPressed(Input.Keys.A) && currentDirection != Direction.LEFT && currentDirection != Direction.RIGHT)) {
                    dir = Direction.LEFT;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.D) || (Gdx.input.isKeyPressed(Input.Keys.D) && currentDirection != Direction.RIGHT && currentDirection != Direction.LEFT)) {
                    dir = Direction.RIGHT;
                }
            }

            if (dir != null && !dir.isOpposite(currentDirection)) {
                return dir;
            }

            return null;
        }

        public Direction getP2Direction(Direction currentDirection) {
            Direction dir = p2BufferedDirection;
            p2BufferedDirection = null;

            if (dir == null) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || (Gdx.input.isKeyPressed(Input.Keys.UP) && currentDirection != Direction.UP && currentDirection != Direction.DOWN)) {
                    dir = Direction.UP;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || (Gdx.input.isKeyPressed(Input.Keys.DOWN) && currentDirection != Direction.DOWN && currentDirection != Direction.UP)) {
                    dir = Direction.DOWN;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || (Gdx.input.isKeyPressed(Input.Keys.LEFT) && currentDirection != Direction.LEFT && currentDirection != Direction.RIGHT)) {
                    dir = Direction.LEFT;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && currentDirection != Direction.RIGHT && currentDirection != Direction.LEFT)) {
                    dir = Direction.RIGHT;
                }
            }

            if (dir != null && !dir.isOpposite(currentDirection)) {
                return dir;
            }

            return null;
        }

        public void reset() {
            p1BufferedDirection = null;
            p2BufferedDirection = null;
        }

        public Direction getNewDirection(Direction currentDirection) {
            Direction dir = getP1Direction(currentDirection);
            return dir != null ? dir : currentDirection;
        }
    }
}
