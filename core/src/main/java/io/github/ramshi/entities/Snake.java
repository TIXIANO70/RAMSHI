package io.github.ramshi.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Snake implements Disposable {

    public static final float TILE_SIZE = 16f;

    // Texturas de cabeza y cola según dirección
    private final Map<Direction, Texture> headTextures = new EnumMap<>(Direction.class);
    private final Map<Direction, Texture> tailTextures = new EnumMap<>(Direction.class);

    // Texturas de cuerpo recto
    private Texture bodyVertical;
    private Texture bodyHorizontal;

    // Texturas de esquinas / curvas (L-bends)
    private Texture cornerDownLeft;  // BodyP#_2.png (conecta Abajo e Izquierda)
    private Texture cornerUpLeft;    // BodyP#_3.png (conecta Arriba e Izquierda)
    private Texture cornerDownRight; // BodyP#_4.png (conecta Abajo y Derecha)
    private Texture cornerUpRight;   // BodyP#_5.png (conecta Arriba y Derecha)

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

        // Cabezas
        headTextures.put(Direction.UP, new Texture("snake/head/Cabeza" + p + "_Up.png"));
        headTextures.put(Direction.DOWN, new Texture("snake/head/Cabeza" + p + "_Down.png"));
        headTextures.put(Direction.LEFT, new Texture("snake/head/Cabeza" + p + "_Left.png"));
        headTextures.put(Direction.RIGHT, new Texture("snake/head/Cabeza" + p + "_Right.png"));

        // Colas
        tailTextures.put(Direction.UP, new Texture("snake/tail/Cola" + p + "_Up.png"));
        tailTextures.put(Direction.DOWN, new Texture("snake/tail/Cola" + p + "_Down.png"));
        tailTextures.put(Direction.LEFT, new Texture("snake/tail/Cola" + p + "_Left.png"));
        tailTextures.put(Direction.RIGHT, new Texture("snake/tail/Cola" + p + "_Right.png"));

        // Cuerpo recto
        bodyVertical = new Texture("snake/Body/Body" + p + ".png");
        bodyHorizontal = new Texture("snake/Body/Body" + p + "_1.png");

        // Esquinas / Curvas
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
                // CABEZA: mira en la dirección de movimiento actual
                textureToDraw = headTextures.get(segment.getDirection());
            } else if (i == 0) {
                // COLA: conecta hacia el segmento siguiente en la cadena
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
                // CUERPO: evaluamos los vecinos previo y siguiente en la grilla
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
                    // Recto horizontal
                    textureToDraw = bodyHorizontal;
                } else if (hasUp && hasDown) {
                    // Recto vertical
                    textureToDraw = bodyVertical;
                } else if (hasLeft && hasDown) {
                    // Esquina Abajo-Izquierda (_2)
                    textureToDraw = cornerDownLeft;
                } else if (hasLeft && hasUp) {
                    // Esquina Arriba-Izquierda (_3)
                    textureToDraw = cornerUpLeft;
                } else if (hasRight && hasDown) {
                    // Esquina Abajo-Derecha (_4)
                    textureToDraw = cornerDownRight;
                } else if (hasRight && hasUp) {
                    // Esquina Arriba-Derecha (_5)
                    textureToDraw = cornerUpRight;
                } else {
                    // Salvaguarda
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
