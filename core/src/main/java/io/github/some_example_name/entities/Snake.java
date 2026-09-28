package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Representa a la serpiente en el juego.
 * Administra sus segmentos corporales, las texturas asociadas y el renderizado en pantalla.
 */
public class Snake implements Disposable {

    public static final float TILE_SIZE = 16f;

    // Texturas de la cabeza en las 4 direcciones (Jugador 1)
    private final Map<Direction, Texture> headTextures = new EnumMap<>(Direction.class);

    // Textura base del cuerpo (Jugador 1)
    private final Map<Direction, Texture> bodyTextures = new EnumMap<>(Direction.class);

    // Texturas de la cola en las 4 direcciones (Jugador 1)
    private final Map<Direction, Texture> tailTextures = new EnumMap<>(Direction.class);

    // Lista ordenada de segmentos que componen la serpiente (desde la cola hasta la cabeza)
    private final List<SnakeSegment> segments = new ArrayList<>();

    /**
     * Constructor de la serpiente.
     * Carga los assets de texturas e inicializa un cuerpo estático de prueba.
     */
    // Número de jugador (1 o 2)
    private final int playerNumber;

    // Segmentos acumulados pendientes por crecer
    private int growPending = 0;

    /**
     * Constructor por defecto para el Jugador 1 en la posición clásica.
     */
    public Snake() {
        this(1, 21, 14, Direction.RIGHT);
    }

    /**
     * Constructor parametrizado para instanciar a la serpiente del Jugador 1 o Jugador 2.
     *
     * @param playerNumber     1 para Jugador 1 (verde), 2 para Jugador 2 (azul/naranja).
     * @param startX           Coordenada X inicial de la cabeza.
     * @param startY           Coordenada Y inicial de la cabeza.
     * @param initialDirection Dirección inicial de avance.
     */
    public Snake(int playerNumber, int startX, int startY, Direction initialDirection) {
        this.playerNumber = playerNumber;
        loadTextures(playerNumber);
        reset(startX, startY, initialDirection);
    }

    /**
     * Carga las texturas correspondientes en memoria según el número de jugador.
     *
     * @param player Identificador del jugador (1 o 2).
     */
    private void loadTextures(int player) {
        String p = "P" + player;

        // Carga de texturas de cabeza
        headTextures.put(Direction.UP, new Texture("snake/head/Cabeza" + p + "_Up.png"));
        headTextures.put(Direction.DOWN, new Texture("snake/head/Cabeza" + p + "_Down.png"));
        headTextures.put(Direction.LEFT, new Texture("snake/head/Cabeza" + p + "_Left.png"));
        headTextures.put(Direction.RIGHT, new Texture("snake/head/Cabeza" + p + "_Right.png"));

        // Carga de texturas del cuerpo (vertical y horizontal)
        bodyTextures.put(Direction.UP, new Texture("snake/Body/Body" + p + ".png"));
        bodyTextures.put(Direction.DOWN, new Texture("snake/Body/Body" + p + ".png"));
        bodyTextures.put(Direction.LEFT, new Texture("snake/Body/Body" + p + "_1.png"));
        bodyTextures.put(Direction.RIGHT, new Texture("snake/Body/Body" + p + "_1.png"));

        // Carga de texturas de cola
        tailTextures.put(Direction.UP, new Texture("snake/tail/Cola" + p + "_Up.png"));
        tailTextures.put(Direction.DOWN, new Texture("snake/tail/Cola" + p + "_Down.png"));
        tailTextures.put(Direction.LEFT, new Texture("snake/tail/Cola" + p + "_Left.png"));
        tailTextures.put(Direction.RIGHT, new Texture("snake/tail/Cola" + p + "_Right.png"));
    }

    /**
     * Reinicia la serpiente a su estado base con 3 segmentos alineados en la dirección inicial.
     *
     * @param startX   Coordenada X de la cabeza.
     * @param startY   Coordenada Y de la cabeza.
     * @param startDir Dirección hacia la que apunta.
     */
    public void reset(int startX, int startY, Direction startDir) {
        segments.clear();
        growPending = 0;

        int dx = startDir.getDx();
        int dy = startDir.getDy();

        // Creamos cola, cuerpo y cabeza de forma consecutiva
        segments.add(new SnakeSegment(startX - 2 * dx, startY - 2 * dy, SnakeSegment.Type.TAIL, startDir));
        segments.add(new SnakeSegment(startX - dx, startY - dy, SnakeSegment.Type.BODY, startDir));
        segments.add(new SnakeSegment(startX, startY, SnakeSegment.Type.HEAD, startDir));
    }

    /**
     * Ejecuta un paso de simulación (tick) avanzando la serpiente en la dirección indicada.
     *
     * @param direction Dirección hacia la que debe moverse la cabeza.
     */
    public void step(Direction direction) {
        SnakeSegment currentHead = getHead();

        // Evitar giro suicida directo de 180 grados si llega una dirección opuesta
        Direction effectiveDirection = direction.isOpposite(currentHead.getDirection())
            ? currentHead.getDirection()
            : direction;

        int nextHeadX = currentHead.getGridX() + effectiveDirection.getDx();
        int nextHeadY = currentHead.getGridY() + effectiveDirection.getDy();

        // La cabeza actual pasa a ser un segmento de cuerpo con su orientación de avance
        currentHead.setType(SnakeSegment.Type.BODY);
        currentHead.setDirection(effectiveDirection);

        // Agregamos la nueva cabeza al final de la lista
        segments.add(new SnakeSegment(nextHeadX, nextHeadY, SnakeSegment.Type.HEAD, effectiveDirection));

        // Si hay crecimiento pendiente, lo consumimos sin remover la cola (aumenta el tamaño)
        if (growPending > 0) {
            growPending--;
        } else {
            // Removemos el segmento de la cola anterior
            segments.remove(0);
        }

        // Actualizamos el nuevo primer segmento para que sea la cola y apunte hacia el siguiente
        updateTailOrientation();
    }

    /**
     * Calcula la orientación visual de la cola basándose en la posición del segmento que le sigue.
     */
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

    /**
     * Marca un crecimiento pendiente para el próximo paso.
     */
    public void grow() {
        this.growPending++;
    }

    /**
     * Retorna el segmento de la cabeza (último elemento de la lista).
     */
    public SnakeSegment getHead() {
        return segments.get(segments.size() - 1);
    }

    /**
     * Comprueba si la cabeza ha chocado contra alguna parte de su propio cuerpo o cola.
     *
     * @return true si hay auto-colisión, false si el camino está libre.
     */
    public boolean checkSelfCollision() {
        SnakeSegment head = getHead();
        // Recorremos todos los segmentos excepto la propia cabeza
        for (int i = 0; i < segments.size() - 1; i++) {
            SnakeSegment segment = segments.get(i);
            if (segment.getGridX() == head.getGridX() && segment.getGridY() == head.getGridY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Comprueba si la cabeza se encuentra fuera de los límites de la cuadrícula.
     *
     * @param gridWidth  Ancho total de la cuadrícula en baldosas.
     * @param gridHeight Alto total de la cuadrícula en baldosas.
     * @return true si la cabeza está fuera de los límites, false si está adentro.
     */
    public boolean checkOutOfBounds(int gridWidth, int gridHeight) {
        SnakeSegment head = getHead();
        return head.getGridX() < 0 || head.getGridX() >= gridWidth ||
               head.getGridY() < 0 || head.getGridY() >= gridHeight;
    }

    /**
     * Verifica si alguna casilla de la serpiente ocupa las coordenadas dadas.
     *
     * @param x Coordenada X de la casilla.
     * @param y Coordenada Y de la casilla.
     * @return true si coincide con algún segmento.
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
     * Dibuja todos los segmentos de la serpiente en las posiciones correspondientes de la cuadrícula.
     *
     * @param batch SpriteBatch utilizado para el dibujado 2D.
     */
    public void render(SpriteBatch batch) {
        for (SnakeSegment segment : segments) {
            Texture textureToDraw = getTextureForSegment(segment);
            if (textureToDraw != null) {
                float posX = segment.getGridX() * TILE_SIZE;
                float posY = segment.getGridY() * TILE_SIZE;
                batch.draw(textureToDraw, posX, posY, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    /**
     * Obtiene la textura correcta según el tipo de segmento y su orientación.
     *
     * @param segment Segmento a evaluar.
     * @return Textura correspondiente.
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

