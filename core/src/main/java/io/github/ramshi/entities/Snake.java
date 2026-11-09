package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class Snake implements Disposable {

    public static final float TILE_SIZE = 16f;

    private final Map<Direction, Texture> headTextures = new EnumMap<>(Direction.class);

    private final Map<Direction, Texture> bodyTextures = new EnumMap<>(Direction.class);

    private final Map<Direction, Texture> tailTextures = new EnumMap<>(Direction.class);

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

        bodyTextures.put(Direction.UP, new Texture("snake/Body/Body" + p + ".png"));
        bodyTextures.put(Direction.DOWN, new Texture("snake/Body/Body" + p + ".png"));
        bodyTextures.put(Direction.LEFT, new Texture("snake/Body/Body" + p + "_1.png"));
        bodyTextures.put(Direction.RIGHT, new Texture("snake/Body/Body" + p + "_1.png"));

        tailTextures.put(Direction.UP, new Texture("snake/tail/Cola" + p + "_Up.png"));
        tailTextures.put(Direction.DOWN, new Texture("snake/tail/Cola" + p + "_Down.png"));
        tailTextures.put(Direction.LEFT, new Texture("snake/tail/Cola" + p + "_Left.png"));
        tailTextures.put(Direction.RIGHT, new Texture("snake/tail/Cola" + p + "_Right.png"));
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

    /**
     * Metodo checkOutOfBounds (gridWidth, gridHeight)
     * Comprueba si la cabeza se encuentra fuera de los límites de la cuadrícula.
     * Parámetro int gridWidth Ancho total de la cuadrícula en baldosas.
     * Parámetro int gridHeight Alto total de la cuadrícula en baldosas.
     * devolvera isOutOfBounds true si la cabeza está fuera de los límites, false si está adentro.
     */
    public boolean checkOutOfBounds(int gridWidth, int gridHeight) {
        SnakeSegment head = getHead();
        return head.getGridX() < 0 || head.getGridX() >= gridWidth ||
               head.getGridY() < 0 || head.getGridY() >= gridHeight;
    }

    /**
     * Metodo occupies (x, y)
     * Verifica si alguna casilla de la serpiente ocupa las coordenadas dadas.
     * Parámetro int x Coordenada X de la casilla.
     * Parámetro int y Coordenada Y de la casilla.
     * devolvera isOccupied true si coincide con algún segmento.
     */
    public boolean occupies(int x, int y) {
        for (SnakeSegment segment : segments) {
            if (segment.getGridX() == x && segment.getGridY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo render (batch, offsetX, offsetY)
     * Dibuja todos los segmentos de la serpiente aplicando un desplazamiento en píxeles.
     * Parámetro SpriteBatch batch SpriteBatch utilizado para el dibujado 2D.
     * Parámetro float offsetX Desplazamiento horizontal en píxeles para la arena correspondiente.
     * Parámetro float offsetY Desplazamiento vertical en píxeles para la arena correspondiente.
     */
    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        for (SnakeSegment segment : segments) {
            Texture textureToDraw = getTextureForSegment(segment);
            if (textureToDraw != null) {
                float posX = offsetX + segment.getGridX() * TILE_SIZE;
                float posY = offsetY + segment.getGridY() * TILE_SIZE;
                batch.draw(textureToDraw, posX, posY, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    /**
     * Metodo render (batch)
     * Dibuja todos los segmentos de la serpiente en las posiciones correspondientes de la cuadrícula base.
     * Parámetro SpriteBatch batch SpriteBatch utilizado para el dibujado 2D.
     */
    public void render(SpriteBatch batch) {
        render(batch, 0, 0);
    }


    /**
     * Metodo getTextureForSegment (segment)
     * Obtiene la textura correcta según el tipo de segmento y su orientación.
     * Parámetro SnakeSegment segment Segmento a evaluar.
     * devolvera texture Textura correspondiente según el tipo y dirección.
     */
    private Texture getTextureForSegment(SnakeSegment segment) {
        switch (segment.getType()) {
            case HEAD:
                return headTextures.get(segment.getDirection());
            case TAIL:
                return tailTextures.get(segment.getDirection());
            default:
                return bodyTextures.get(segment.getDirection());
        }
    }

    public List<SnakeSegment> getSegments() {
        return segments;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * Metodo dispose ()
     * Libera de la memoria de la GPU todas las texturas cargadas.
     */
    @Override
    public void dispose() {
        for (Texture texture : headTextures.values()) {
            texture.dispose();
        }
        headTextures.clear();

        for (Texture texture : bodyTextures.values()) {
            texture.dispose();
        }
        bodyTextures.clear();

        for (Texture texture : tailTextures.values()) {
            texture.dispose();
        }
        tailTextures.clear();
    }
}

