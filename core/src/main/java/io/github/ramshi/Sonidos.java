package io.github.ramshi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import io.github.ramshi.menus.AjustesScreen;

import java.util.HashMap;
import java.util.Map;

public class Sonidos {

    public static final String CARPETA_SFX = "sonidos/sfx/";
    public static final String CARPETA_MUSICA = "sonidos/musica/";
    public static final float VOLUMEN_MUSICA = 0.45f;
    public static final float VOLUMEN_SFX = 0.75f;

    private static final Map<String, Sound> efectos = new HashMap<>();
    private static Music musicaActual;
    private static String musicaActualNombre;

    public static void sonar(String nombre) {
        sonar(nombre, 1f);
    }

    public static void sonar(String nombre, float volumen) {
        if (nombre == null || !AjustesScreen.sonidosActivos()) return;
        Sound sfx = obtenerEfecto(nombre);
        if (sfx == null) return;
        float vol = Math.max(0f, Math.min(1f, VOLUMEN_SFX * volumen));
        sfx.play(vol);
    }

    public static void reproducirMusica(String nombre) {
        if (nombre == null) {
            detenerMusica();
            return;
        }
        if (nombre.equals(musicaActualNombre) && musicaActual != null) {
            if (AjustesScreen.musicaActiva()) {
                musicaActual.setVolume(VOLUMEN_MUSICA);
                if (!musicaActual.isPlaying()) musicaActual.play();
            } else {
                pausarMusica();
            }
            return;
        }
        if (musicaActual != null) {
            musicaActual.stop();
            musicaActual.dispose();
            musicaActual = null;
        }
        musicaActualNombre = nombre;
        FileHandle archivo = buscarArchivo(CARPETA_MUSICA, nombre, ".ogg", ".wav");
        if (archivo == null) return;
        musicaActual = Gdx.audio.newMusic(archivo);
        musicaActual.setLooping(true);
        musicaActual.setVolume(VOLUMEN_MUSICA);
        if (AjustesScreen.musicaActiva()) {
            musicaActual.play();
        }
    }

    public static void pausarMusica() {
        if (musicaActual != null && musicaActual.isPlaying()) {
            musicaActual.pause();
        }
    }

    public static void reanudarMusica() {
        if (musicaActual != null && AjustesScreen.musicaActiva()) {
            musicaActual.setVolume(VOLUMEN_MUSICA);
            musicaActual.play();
        }
    }

    public static void detenerMusica() {
        if (musicaActual != null) {
            musicaActual.stop();
            musicaActual.dispose();
            musicaActual = null;
        }
        musicaActualNombre = null;
    }

    public static void actualizarAjustes() {
        if (AjustesScreen.musicaActiva()) {
            if (musicaActual != null) {
                reanudarMusica();
            } else if (musicaActualNombre != null) {
                reproducirMusica(musicaActualNombre);
            }
        } else {
            pausarMusica();
        }
    }

    public static void liberar() {
        for (Sound sfx : efectos.values()) {
            if (sfx != null) sfx.dispose();
        }
        efectos.clear();
        detenerMusica();
    }

    private static Sound obtenerEfecto(String nombre) {
        Sound sfx = efectos.get(nombre);
        if (sfx != null) return sfx;
        FileHandle archivo = buscarArchivo(CARPETA_SFX, nombre, ".wav", ".ogg");
        if (archivo == null) return null;
        sfx = Gdx.audio.newSound(archivo);
        efectos.put(nombre, sfx);
        return sfx;
    }

    private static FileHandle buscarArchivo(String carpeta, String nombre, String extPrimaria, String extSecundaria) {
        if (Gdx.files == null || Gdx.audio == null) return null;
        FileHandle primario = Gdx.files.internal(carpeta + nombre + extPrimaria);
        if (primario.exists()) return primario;
        FileHandle secundario = Gdx.files.internal(carpeta + nombre + extSecundaria);
        if (secundario.exists()) return secundario;
        return null;
    }
}
