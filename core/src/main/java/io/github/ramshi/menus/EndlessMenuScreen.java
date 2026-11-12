package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.ramshi.Main;
import io.github.ramshi.session.GameSession;
import io.github.ramshi.session.MinigameType;

import java.util.EnumMap;
import java.util.Map;

public class EndlessMenuScreen extends MenuScreen {

    private static final float CENTRO_X = ANCHO / 2f;
    private static final float CIRCULOS_Y = 275f;
    private static final float RADIO_CENTRO = 85f;
    private static final float RADIO_LADO = 62f;
    private static final float DISTANCIA_LADO = 230f;
    private static final float BORDE = 5f;
    private static final int SEGMENTOS = 64;
    private static final float NOMBRE_Y = 162f;
    private static final float PAGINADOR_Y = 115f;
    private static final float JUGAR_ANCHO = 160f;
    private static final float JUGAR_ALTO = 48f;
    private static final float JUGAR_Y = 22f;
    private static final float SATURACION = 0.55f;
    private static final float VALOR = 0.7f;
    private static final float BRILLO_LADO = 0.55f;
    private static final Color COLOR_BORDE_CENTRO = new Color(1f, 0.85f, 0.2f, 1f);
    private static final Color COLOR_BORDE_LADO = new Color(0.35f, 0.35f, 0.4f, 1f);
    private static final float DURACION_DESLIZAR = 0.18f;
    private static final float DESPLAZAMIENTO_MAX = 2f;
    private static final int VISIBLES = 2;

    private static int seleccion = 0;

    private final MinigameType[] tipos = MinigameType.values();
    private final Paginador paginador;
    private final Map<MinigameType, Texture> imagenes = new EnumMap<>(MinigameType.class);
    private final Color relleno = new Color(1f, 1f, 1f, 1f);
    private final Color borde = new Color();
    private float desplazamiento;

    public EndlessMenuScreen(Game juego) {
        super(juego);
        paginador = new Paginador(tipos.length, seleccion, CENTRO_X, PAGINADOR_Y);
        for (MinigameType t : tipos) {
            if (Gdx.files.internal(t.getImagen()).exists()) imagenes.put(t, new Texture(t.getImagen()));
        }
        botonera.agregar(new Boton(CENTRO_X - JUGAR_ANCHO / 2f, JUGAR_Y, JUGAR_ANCHO, JUGAR_ALTO, "JUGAR", Boton.VERDE,
            this::jugar));
        agregarVolver(() -> abrir(new ModoMenuScreen(juego, false)));
        botonera.navegarHorizontal = false;
    }

    public static void seleccionar(MinigameType tipo) {
        seleccion = tipo.ordinal();
    }

    private void jugar() {
        abrir(GameSession.iniciarEndless(tipos[paginador.actual()]).primeraPantalla(juego));
    }

    @Override
    protected void actualizar() {
        boolean click = Gdx.input.justTouched();
        paginador.actualizar(mouseX, mouseY, click);
        if (click && Vector2.dst(mouseX, mouseY, CENTRO_X - DISTANCIA_LADO, CIRCULOS_Y) < RADIO_LADO) paginador.anterior();
        if (click && Vector2.dst(mouseX, mouseY, CENTRO_X + DISTANCIA_LADO, CIRCULOS_Y) < RADIO_LADO) paginador.siguiente();

        int cambio = paginador.actual() - seleccion;
        if (cambio > tipos.length / 2) cambio -= tipos.length;
        if (cambio < -tipos.length / 2) cambio += tipos.length;
        desplazamiento = MathUtils.clamp(desplazamiento + cambio, -DESPLAZAMIENTO_MAX, DESPLAZAMIENTO_MAX);
        float paso = Gdx.graphics.getDeltaTime() / DURACION_DESLIZAR;
        desplazamiento = desplazamiento > 0f ? Math.max(0f, desplazamiento - paso) : Math.min(0f, desplazamiento + paso);
        seleccion = paginador.actual();

        if (click && Vector2.dst(mouseX, mouseY, CENTRO_X, CIRCULOS_Y) < RADIO_CENTRO) jugar();
    }

    @Override
    protected void dibujar() {
        dibujarTitulo("ENDLESS");
        MinigameType actual = tipos[paginador.actual()];

        formas.begin(ShapeRenderer.ShapeType.Filled);
        for (int k = -VISIBLES; k <= VISIBLES; k++) dibujarCirculo(tipos[paginador.vecino(k)], k + desplazamiento);
        formas.end();

        batch.begin();
        for (int k = -VISIBLES; k <= VISIBLES; k++) dibujarImagen(tipos[paginador.vecino(k)], k + desplazamiento);
        if (!imagenes.containsKey(actual)) {
            escribir(Main.fuenteTitulo, Color.WHITE, actual.getNombre().substring(0, 1),
                CENTRO_X + desplazamiento * DISTANCIA_LADO, CIRCULOS_Y + 22f);
        }
        escribir(Main.fuenteCartel, Color.WHITE, actual.getNombre(), CENTRO_X, NOMBRE_Y);
        batch.end();

        paginador.dibujar(formas, batch);
    }

    private float lejania(float posicion) {
        return Math.min(1f, Math.abs(posicion));
    }

    private float opacidad(float posicion) {
        return MathUtils.clamp(VISIBLES - Math.abs(posicion), 0f, 1f);
    }

    private void dibujarCirculo(MinigameType tipo, float posicion) {
        float alfa = opacidad(posicion);
        if (alfa <= 0f) return;
        float x = CENTRO_X + posicion * DISTANCIA_LADO;
        float radio = MathUtils.lerp(RADIO_CENTRO, RADIO_LADO, lejania(posicion));
        float brillo = MathUtils.lerp(1f, BRILLO_LADO, lejania(posicion));
        formas.setColor(borde.set(COLOR_BORDE_CENTRO).lerp(COLOR_BORDE_LADO, lejania(posicion)));
        formas.getColor().a = alfa;
        formas.circle(x, CIRCULOS_Y, radio + BORDE, SEGMENTOS);
        relleno.fromHsv(360f * tipo.ordinal() / tipos.length, SATURACION, VALOR * brillo);
        relleno.a = alfa;
        formas.setColor(relleno);
        formas.circle(x, CIRCULOS_Y, radio, SEGMENTOS);
    }

    private void dibujarImagen(MinigameType tipo, float posicion) {
        Texture imagen = imagenes.get(tipo);
        float alfa = opacidad(posicion);
        if (imagen == null || alfa <= 0f) return;
        float x = CENTRO_X + posicion * DISTANCIA_LADO;
        float radio = MathUtils.lerp(RADIO_CENTRO, RADIO_LADO, lejania(posicion));
        float brillo = MathUtils.lerp(1f, BRILLO_LADO, lejania(posicion));
        batch.setColor(brillo, brillo, brillo, alfa);
        batch.draw(imagen, x - radio, CIRCULOS_Y - radio, radio * 2f, radio * 2f);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : imagenes.values()) t.dispose();
    }
}
