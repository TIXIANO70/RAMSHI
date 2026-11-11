package io.github.ramshi;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import io.github.ramshi.screens.Controles;
import io.github.ramshi.screens.MainMenuScreen;

public class Main extends Game {

    private static final String FUENTE_NORMAL = "ui/fonts/PixelifySans-Regular.ttf";
    private static final String FUENTE_NEGRITA = "ui/fonts/PixelifySans-Bold.ttf";
    private static final int TAMANO_CHICA = 16;
    private static final int TAMANO_MEDIANA = 20;
    private static final int TAMANO_GRANDE = 24;
    private static final int TAMANO_CARTEL = 32;
    private static final int TAMANO_TITULO = 56;
    private static final String CARACTERES_EXTRA = "¡¿áéíóúÁÉÍÓÚñÑü";

    public static BitmapFont fuenteChica;
    public static BitmapFont fuenteMediana;
    public static BitmapFont fuenteGrande;
    public static BitmapFont fuenteCartel;
    public static BitmapFont fuenteTitulo;

    @Override
    public void create() {
        fuenteChica = crearFuente(FUENTE_NORMAL, TAMANO_CHICA);
        fuenteMediana = crearFuente(FUENTE_NORMAL, TAMANO_MEDIANA);
        fuenteGrande = crearFuente(FUENTE_NEGRITA, TAMANO_GRANDE);
        fuenteCartel = crearFuente(FUENTE_NEGRITA, TAMANO_CARTEL);
        fuenteTitulo = crearFuente(FUENTE_NEGRITA, TAMANO_TITULO);
        setScreen(new MainMenuScreen(this));
    }

    private BitmapFont crearFuente(String ruta, int tamano) {
        FreeTypeFontGenerator generador = new FreeTypeFontGenerator(Gdx.files.internal(ruta));
        FreeTypeFontParameter parametros = new FreeTypeFontParameter();
        parametros.size = tamano;
        parametros.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CARACTERES_EXTRA;
        parametros.minFilter = TextureFilter.Nearest;
        parametros.magFilter = TextureFilter.Nearest;
        BitmapFont fuente = generador.generateFont(parametros);
        generador.dispose();
        return fuente;
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        fuenteChica.dispose();
        fuenteMediana.dispose();
        fuenteGrande.dispose();
        fuenteCartel.dispose();
        fuenteTitulo.dispose();
        Controles.liberar();
    }
}
