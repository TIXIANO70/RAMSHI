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
    public Snake() {
        // Carga de texturas de cabeza
        headTextures.put(Direction.UP, new Texture("snake/head/CabezaP1_Up.png"));
        headTextures.put(Direction.DOWN, new Texture("snake/head/CabezaP1_Down.png"));
        headTextures.put(Direction.LEFT, new Texture("snake/head/CabezaP1_Left.png"));
        headTextures.put(Direction.RIGHT, new Texture("snake/head/CabezaP1_Right.png"));

        // Carga de textura del cuerpo
        bodyTextures.put(Direction.UP, new Texture("snake/Body/BodyP1.png"));
        bodyTextures.put(Direction.DOWN, new Texture("snake/Body/BodyP1.png"));
        bodyTextures.put(Direction.LEFT, new Texture("snake/Body/BodyP1_1.png"));
        bodyTextures.put(Direction.RIGHT, new Texture("snake/Body/BodyP1_1.png"));

        // Carga de texturas de cola
        tailTextures.put(Direction.UP, new Texture("snake/tail/ColaP1_Up.png"));
        tailTextures.put(Direction.DOWN, new Texture("snake/tail/ColaP1_Down.png"));
        tailTextures.put(Direction.LEFT, new Texture("snake/tail/ColaP1_Left.png"));
        tailTextures.put(Direction.RIGHT, new Texture("snake/tail/ColaP1_Right.png"));

        // Inicialización de una serpiente estática en el centro del tablero (orientada hacia la derecha)
        // Cola en (18, 14), Cuerpos en (19, 14) y (20, 14), Cabeza en (21, 14)
        segments.add(new SnakeSegment(18, 14, SnakeSegment.Type.TAIL, Direction.RIGHT));
        segments.add(new SnakeSegment(19, 14, SnakeSegment.Type.BODY, Direction.RIGHT));
        segments.add(new SnakeSegment(20, 14, SnakeSegment.Type.BODY, Direction.RIGHT));
        segments.add(new SnakeSegment(21, 14, SnakeSegment.Type.HEAD, Direction.RIGHT));
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
