package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class QuickdrawScreen extends BaseMinigameScreen{
    private int healthP1=3;
    private int healthP2=3;
    private float triggerInterval=1;
    private float reactionInterval=0.5f;


    private float P1_X = 0f;
    private float P2_X = 600f;
    private float gunY = 180f;
    private float gunH = 128f;
    private float gunW = gunH;
    private float triggerTimer;
    private boolean canShoot;
    private float reactionTimer;
    private boolean addReactionTimer=false;
    private boolean shooted;
    private int shootOption;
    private int heartY = 300;
    private int heartW = 32;
    private int heartH = heartW;
    private String[] P1_keys = {"W","A","S","D"};
    private String[] P2_keys = {"UP","LEFT","DOWN","RIGHT"};
    private final Texture heart;
    private final Texture empty_heart;
    private final Texture blue_shotgun;
    private final Texture red_shotgun;
    private final Texture desert;
    public QuickdrawScreen(Game game){
        super(game);
        this.heart = new Texture("quickdraw/heart.png");
        this.heart.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.empty_heart = new Texture("quickdraw/empty_heart.png");
        this.empty_heart.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.blue_shotgun = new Texture("quickdraw/blue_shotgun.png");
        this.blue_shotgun.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.red_shotgun = new Texture("quickdraw/red_shotgun.png");
        this.red_shotgun.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.desert = new Texture("quickdraw/desert.png");
        this.desert.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    }

    private Texture cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        for (int p = 0; p < 2; p++) {
            for (int k = 0; k < 4; k++) {
                if (Gdx.input.isKeyJustPressed(TECLAS[p][k])) disparar(p + 1, k + 1);
            }
        }
        tiempoGatillo += delta;
        if (contarReaccion) {
            tiempoReaccion += delta;
        }
        if (tiempoReaccion > TIEMPO_REACCION) {
            contarReaccion = false;
            tiempoReaccion = 0;
            yaDisparo = false;
        }
        if (intervaloGatillo < tiempoGatillo) {
            tiempoGatillo -= intervaloGatillo;
            if (!puedeDisparar) {
                intervaloGatillo = INTERVALO_BASE + MathUtils.random(AZAR_MIN, AZAR_MAX);
                habilitarGatillo();
            }
        }
    }

    private void quitarVida(int jugador) {
        if (jugador == 1 && !yaDisparo) {
            vidaP1--;
            yaDisparo = false;
        } else if (jugador == 2 && !yaDisparo) {
            vidaP2--;
            yaDisparo = false;
        }
    }

    private void disparar(int jugador, int tecla) {
        if (tecla == opcionDisparo && puedeDisparar) {
            puedeDisparar = false;
            quitarVida(jugador == 1 ? 2 : 1);
        } else {
            quitarVida(jugador);
        }
        if (!contarReaccion) {
            contarReaccion = true;
            yaDisparo = true;
        }
        if (vidaP1 == 0 || vidaP2 == 0) {
            terminarRonda();
        }
    }

    private void habilitarGatillo() {
        opcionDisparo = MathUtils.random(1, 4);
        puedeDisparar = true;
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(desert, 0,0,VIRTUAL_WIDTH,VIRTUAL_HEIGHT);
        batch.draw(blue_shotgun, P1_X, gunY, gunW, gunH);
        batch.draw(red_shotgun, P2_X, gunY, gunW, gunH);
        for(int i=0; i<healthP1;i++){
            batch.draw(heart, P1_X+i*48, heartY, heartW, heartH);
        }
        for (int i = 0; i < vidaP2; i++) {
            batch.draw(corazon, P2_X + i * SEPARACION_CORAZON, CORAZON_Y, TAMANO_CORAZON, TAMANO_CORAZON);
        }
        if (puedeDisparar) {
            fuente.draw(batch, NOMBRES_TECLAS_P1[opcionDisparo - 1], 400, 200);
            fuente.draw(batch, NOMBRES_TECLAS_P2[opcionDisparo - 1], 400, 250);
        }
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (vidaP1 == vidaP2) return "¡EMPATE!";
        return (vidaP1 > vidaP2) ? sumarPuntos(1, Math.max(1, vidaP1)) : sumarPuntos(2, Math.max(1, vidaP2));
    }

    @Override
    protected void reiniciarRonda() {
        vidaP1 = VIDA_MAXIMA;
        vidaP2 = VIDA_MAXIMA;
        intervaloGatillo = PRIMER_INTERVALO;
        tiempoGatillo = 0;
    }

    @Override protected String getTextoP1() { return "Vida: " + vidaP1; }
    @Override protected String getTextoP2() { return "Vida: " + vidaP2; }
    @Override protected String getResumen() { return "P1(" + vidaP1 + "hp) vs P2(" + vidaP2 + "hp)"; }

    @Override
    public void dispose() {
        super.dispose();
        if(desert!=null) desert.dispose();
        if(red_shotgun != null) red_shotgun.dispose();
        if(blue_shotgun != null) blue_shotgun.dispose();
        if(heart != null) heart.dispose();
        if(empty_heart != null) empty_heart.dispose();
    }
}
