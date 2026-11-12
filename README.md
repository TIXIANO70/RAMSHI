# RAMSHI

Juego **1 contra 1 local** de **microjuegos de 10 segundos**, al estilo WarioWare, hecho en **Java + libGDX**.
Los dos jugadores comparten el teclado y la pantalla: cada minijuego dura 10 segundos, el que lo gana suma puntos (de **1 a 3** en casi todos) y al final de la partida gana quien juntó más.

- **Jugador 1:** `W` `A` `S` `D` (y `ESPACIO` en los juegos que tienen una acción extra).
- **Jugador 2:** flechas `↑` `←` `↓` `→` (y `ENTER` en los juegos que tienen una acción extra).

---

## Índice

1. [Cómo ejecutarlo](#1-cómo-ejecutarlo)
2. [Menús y modos de juego](#2-menús-y-modos-de-juego) (incluye el [modo en red](#21-modo-en-red))
3. [Qué pasa en cada minijuego](#3-qué-pasa-en-cada-minijuego)
4. [Sistema de puntos](#4-sistema-de-puntos)
5. [Los 12 minijuegos](#5-los-12-minijuegos)
6. [Estructura del proyecto](#6-estructura-del-proyecto)
7. [Cómo funciona por dentro](#7-cómo-funciona-por-dentro)
8. [Cómo agregar un minijuego](#8-cómo-agregar-un-minijuego)
9. [Ajustes rápidos](#9-ajustes-rápidos)
10. [Assets, fuente y créditos](#10-assets-fuente-y-créditos)
11. [Problemas comunes](#11-problemas-comunes)
12. [Equipo y licencia](#12-equipo-y-licencia)

---

## 1. Cómo ejecutarlo

**Requisitos:** un JDK instalado (recomendado 17 o 21). Gradle se descarga solo con el *wrapper* que trae el proyecto; la primera vez tarda un poco porque baja libGDX y FreeType.

Desde la carpeta del proyecto:

| Qué querés hacer | Windows (PowerShell) | Linux / macOS |
|---|---|---|
| Jugar | `.\gradlew.bat lwjgl3:run` | `./gradlew lwjgl3:run` |
| Solo compilar | `.\gradlew.bat core:compileJava` | `./gradlew core:compileJava` |
| Generar el `.jar` ejecutable | `.\gradlew.bat lwjgl3:jar` | `./gradlew lwjgl3:jar` |

El `.jar` queda en `lwjgl3/build/libs/` y se abre con `java -jar <archivo>.jar`.

La ventana es de **800 × 480** (el mundo del juego mide lo mismo; si se agranda la ventana, la imagen se escala manteniendo la proporción).

---

## 2. Menús y modos de juego

Los menús se usan **con el mouse o con el teclado, como en una consola**:

| Tecla | Qué hace |
|---|---|
| `W` `A` `S` `D` o flechas | Mueven la selección al botón que está en esa dirección (el seleccionado se ve más claro y con borde blanco) |
| `ENTER` o `ESPACIO` | Aprietan el botón seleccionado |
| `ESC` | Vuelve al menú anterior |
| Mouse | Pasar por encima selecciona el botón y el clic lo aprieta |

En cualquier submenú, el botón rojo **VOLVER** está abajo a la izquierda. Cada pantalla arranca con el botón principal ya seleccionado (JUGAR, JUGAR RONDAS, REANUDAR…).

```
Menú principal
├── JUGAR
│   ├── LOCAL ──── Partida por rondas (3 a 12)
│   │              └── ENDLESS ── selector de minijuego
│   └── EN RED
│       ├── CREAR SALA ── muestra tu IP y espera ── (se une alguien) ── Rondas / Endless
│       └── UNIRSE ────── buscar salas o escribir la IP ── esperar al anfitrión
├── GUÍA ───────── páginas con la explicación del juego y de cada minijuego
├── AJUSTES ────── pantalla completa (se guarda)
└── SALIR
```

- **Menú principal:** el título "RAMSHI" (si existe `assets/ui/logo.png`, se muestra el logo en su lugar) y los botones **JUGAR**, **GUÍA**, **AJUSTES** y **SALIR**.
- **Jugar:** dos tarjetas, **LOCAL** (los dos en el mismo teclado) a la izquierda y **EN RED** (cada uno en su compu) a la derecha.
- **En red:** cada jugador en su computadora, conectadas a la misma red. Ver la [sección 2.1](#21-modo-en-red).
- **Guía:** como una presentación. Las primeras dos páginas explican qué es el juego, los controles, los modos, los puntos y la pausa; después hay **una página por minijuego** con *cómo se juega*, *meta* y *puntos*. Abajo se ve la página actual (`3/14`) con flechas; se cambia con las flechas en pantalla, `←` `→` o `A` `D`. **Es un bucle:** después de la última vuelve a la primera.
- **Ajustes:** botón **PANTALLA COMPLETA: SÍ / NO**. Queda guardado para la próxima vez que se abra el juego.

### 2.1 Modo en red

Para jugar cada uno en su computadora. Las dos tienen que estar **en la misma red** (el mismo Wi-Fi o router).

**El que crea la sala (anfitrión, Jugador 1):**
1. JUGAR → EN RED → **CREAR SALA**.
2. Aparece **su IP** (por ejemplo `192.168.0.147`) y "Esperando a otro jugador...".
3. Cuando el otro se une, pasa a la **SALA EN RED**: el mismo menú que Local (rondas de 3 a 12 o Endless). **El anfitrión elige el modo** y la partida arranca en las dos computadoras a la vez.

**El que se une (invitado, Jugador 2):**
1. JUGAR → EN RED → **UNIRSE**.
2. El juego **busca salas en la red solo** y las muestra como botones ("Sala de NOMBRE-PC (IP)"). Se toca una y listo.
3. Si no aparece, se escribe la IP del anfitrión con el teclado (números y puntos, `Retroceso` borra) y se toca **CONECTAR**. La última IP usada queda guardada.
4. Queda en "Esperando que el anfitrión elija el modo..." hasta que arranca la partida.

**Durante la partida:**
- Cada uno juega con **WASD o con las flechas** (los dos sirven), y `ESPACIO` o `ENTER` para la acción. En el cartel de inicio dice cuál es tu jugador: "JUGADOR 1 (VOS)".
- **La pausa es compartida:** si cualquiera aprieta `ESC` (o REANUDAR), se pausa o se reanuda en las dos computadoras.
- **SALIR** en la pausa (o **SALA** en los resultados) lleva a los dos de vuelta a la sala, sin desconectarse. **REVANCHA** solo la puede tocar el anfitrión.
- Si la conexión se corta más de 5 segundos (o alguien cierra el juego), aparece el aviso arriba: el anfitrión queda esperando a otro jugador y el invitado vuelve al menú EN RED.
- Si por un momento no llegan datos del otro, el juego se frena y muestra "Esperando al otro jugador...".

**Para probarlo en una sola computadora:** se abren dos ventanas del juego (por ejemplo `java -jar` dos veces, ver la [sección 1](#1-cómo-ejecutarlo)); una crea la sala y la otra se une a `127.0.0.1`.

**Firewall de Windows:** la primera vez que alguien crea una sala, Windows pregunta si deja que Java use la red. Hay que tocar **Permitir acceso** (en redes privadas); si no, la otra computadora no se puede conectar.

### Partida por rondas

1. Con los botones **−** y **+** se eligen las **rondas**: de **3 a 12** (empieza en **8**).
2. Al tocar **JUGAR RONDAS** se arma una lista de minijuegos al azar. **Nunca sale el mismo juego dos veces seguidas**; si hay más rondas que juegos, se vuelve a mezclar la lista.
3. Se juegan uno detrás del otro sin volver al menú.
4. Después de cada minijuego aparece la **tabla de la partida** (ver [sección 3](#3-qué-pasa-en-cada-minijuego)).
5. Al terminar la última ronda se pasa a la **pantalla de resultados**:
   - Ganador y motivo.
   - Lista de todos los juegos con los puntos de cada jugador y quién ganó cada uno.
   - Totales.
   - **Desempate:** 1) más puntos totales; 2) si empatan, más juegos ganados; 3) si también empatan, es empate.
   - Abajo hay dos botones: **REVANCHA** (verde, arranca otra partida con la misma cantidad de rondas) y **MENÚ** (vuelve al menú principal). Durante el primer segundo y medio están apagados, para no saltear el resultado sin querer.

### Endless (práctica)

1. Se elige **un minijuego** en un selector circular:
   - En el centro, un **círculo grande** con la imagen del minijuego, su **nombre** abajo y el número (`5/12`) con flechas.
   - A los costados, **un poco más chicos y sin texto**, el minijuego anterior y el siguiente.
   - Se cambia con las flechas en pantalla, `←` `→`, `A` `D` o tocando un círculo del costado. **Es un bucle.** Al cambiar, los círculos **se deslizan** hacia el costado (0,18 s): el nuevo crece al llegar al centro y el que sale se desvanece en el borde.
   - Se juega con el botón **JUGAR** (arranca seleccionado, así que alcanza con `ENTER` o `ESPACIO`) o tocando el círculo del centro. Acá izquierda y derecha cambian de juego, así que para ir a **VOLVER** se usa `S` / `↓` (y `W` / `↑` para subir).
   - Cada minijuego tiene su **logo**: un collage de sus propios sprites en `assets/menu/minijuegos/<nombre>.png` (por ejemplo `snake.png`, `tug_of_war.png`), de **170 × 170 px** (el tamaño del círculo del centro), recortado en círculo con fondo transparente. Si a un juego le falta el logo, se dibuja un círculo de color con la inicial.
2. Se juega en bucle: al terminar cada ronda aparece el cartel del ganador con la explicación de los puntos y las **partidas ganadas** por cada uno.
3. `ESPACIO` o `ENTER` → siguiente ronda (si no, arranca sola a los 2,5 s).

### Pausa (`ESC` en medio de un minijuego)

- El juego se congela (también el reloj) y aparece la pantalla de **PAUSA** con dos botones: **REANUDAR** (verde) y **SALIR** (rojo). Se eligen con el mouse o con `A` `D` / flechas y `ENTER` / `ESPACIO`. `ESC` también reanuda.
- **En partida por rondas:** muestra en qué juego van (`Juego 3 de 8: Race`), la **tabla de los juegos ya jugados** y el **total** de cada uno. *SALIR* vuelve al menú Local. No se puede pausar durante los 2,5 s de la tabla entre juegos.
- **En Endless:** muestra el **marcador de partidas ganadas** (`P1 3 - 2 P2`). *SALIR* vuelve al selector de Endless con ese mismo minijuego elegido.

---

## 3. Qué pasa en cada minijuego

Todos los minijuegos comparten el mismo "marco", que dibuja la clase base:

### 3.1 Cartel de inicio (1,5 s)

- La pantalla se oscurece y se divide en dos mitades, una por jugador.
- Arriba: el **nombre del juego** y, en partida por rondas, "Juego N de M" con el total de cada jugador.
- En cada mitad: la **consigna** en grande (por ejemplo **¡ESQUIVÁ!**), "JUGADOR 1" / "JUGADOR 2" y **las teclas que va a usar ese jugador, dibujadas en grande**, con lo que hace cada una.
  - Las teclas de movimiento aparecen con la forma del teclado (`W` arriba, `A S D` abajo; lo mismo con las flechas).
  - En RPS, debajo de cada tecla se ve el dibujo del objeto.

### 3.2 Durante el juego

- **Arriba a la izquierda:** estado del jugador 1 (verde).
- **Arriba al centro:** el **reloj**, un círculo con los segundos que quedan (del 10 al 1) en grande y amarillo; se pone **rojo en los últimos 3 segundos**. A los costados, sobre el borde de arriba, dos barras que se van **cortando desde los extremos hacia el reloj** y cambian de **verde a amarillo y a rojo**; cuando llegan al centro se terminó el tiempo.
- **Arriba a la derecha:** estado del jugador 2 (celeste).
- **Corazones** (solo en los juegos con vidas: Quickdraw y Tank Duel): uno por vida, centrados en la mitad de cada jugador. Las vidas perdidas se ven como corazones vacíos.
- **Barra de abajo:** recordatorio de los controles de cada jugador (tecla en chico + acción).
- Arriba y abajo hay **franjas oscuras semitransparentes** detrás de los textos y de los controles, para que se lean sobre cualquier fondo.

### 3.3 Al terminar el minijuego (2,5 s)

**En partida por rondas** aparece la tabla de la partida:

- **Quién ganó y cuántos puntos sumó** (en grande).
- **El motivo** (qué pasó en el juego).
- **La explicación de los puntos:** qué hizo el ganador y qué regla se aplicó.
  Ejemplo: *"El rival se hundió a los 2,4 s (antes de 3,3 s = 3 pts, antes de 6,6 s = 2, después = 1)"*.
- La lista de los juegos jugados hasta el momento, con el último **resaltado en amarillo**, y el **total actualizado** de cada jugador.

**En modo Endless** aparece un cartel con lo mismo (ganador, motivo, explicación) y las partidas ganadas por cada uno.

**Transición:** en partida por rondas, en los últimos 0,3 s de la tabla la pantalla se funde a negro, y el minijuego siguiente (o la pantalla de resultados) aparece aclarándose en otros 0,3 s.

---

## 4. Sistema de puntos

Regla general para **todos** los minijuegos:

| Resultado | Puntos |
|---|---|
| Paliza (ganar rápido, sin daño o por mucha diferencia) | **3** |
| Victoria clara | **2** |
| Victoria ajustada o por tiempo | **1** |
| **Empate** | **0 para los dos** |

Hay dos formas de medir "qué tan buena" fue la victoria:

- **Por rapidez** (juegos que pueden terminar antes de los 10 s): el rival cae o se llega a la meta…
  - entre 0 y 3,3 s → **3 puntos**
  - entre 3,3 y 6,6 s → **2 puntos**
  - después de 6,6 s → **1 punto**
- **Por diferencia** (juegos que se deciden al final): cuanto mayor la ventaja, más puntos (ver cada juego).

**Excepción:** RPS da 1 punto por mano ganada, con un tope de 5.

El detalle de cada juego está en la tabla de la [sección 5](#5-los-12-minijuegos).

---

## 5. Los 12 minijuegos

### Resumen

| # | Minijuego | Consigna | Controles P1 / P2 | Cómo se gana | 3 pts | 2 pts | 1 pt |
|---|---|---|---|---|---|---|---|
| 1 | Snake | ¡COMÉ! | WASD / flechas | Mejor récord de manzanas | Gana por 4+ | por 2-3 | por 1 |
| 2 | Lights | ¡REPETÍ! | WASD / flechas | Más secuencias completadas | 3 de 3 | 2 | 1 |
| 3 | Ships | ¡ESQUIVÁ! | A D / ← → | El rival se hunde primero | antes de 3,3 s | antes de 6,6 s | después |
| 4 | Tug of War | ¡TIRÁ! | A D / ← → (alternando) | Llevar la soga a tu línea | llega antes de 3,3 s | antes de 6,6 s | después, o soga de su lado al final |
| 5 | Quickdraw | ¡DISPARÁ! | WASD / flechas | Dejar al rival sin vidas o tener más | termina con 3 vidas | con 2 | con 1 |
| 6 | Race | ¡ESQUIVÁ! | A D / ← → | Más distancia recorrida | +50% | +20% | menos |
| 7 | RPS | ¡GANÁ! | A S D / ← ↓ → | Más manos ganadas | 1 punto por mano ganada, **hasta 5** | | |
| 8 | Tank Duel | ¡DESTRUÍ! | WASD + ESPACIO / flechas + ENTER | Destruir al rival o más vidas | KO sin perder vidas | KO | más vidas al final |
| 9 | Bomb Tag | ¡PASALA! | WASD + ESPACIO / flechas + ENTER | No tener la bomba a los 10 s | el rival la tuvo +70% | +50% | menos |
| 10 | Ping Pong | ¡DEVOLVÉ! | W S / ↑ ↓ | Meter 2 goles | 2-0 | 2-1 | iba ganando al final |
| 11 | Sumo | ¡EMPUJÁ! | A D + W / ← → + ↑ | Tirar al rival de la plataforma | ring out antes de 3,3 s | antes de 6,6 s | después, o más cerca del centro |
| 12 | Stop It | ¡FRENÁ! | W / ↑ | Más puntos frenando la barra | gana por 6+ | por 3-5 | por 1-2 |

### Detalle de cada minijuego

#### 1. Snake — `SnakeScreen`
- Cada jugador tiene **su propia arena de 14 × 14** y su serpiente (arranca con 3 tramos).
- Comer la manzana hace crecer la serpiente y suma 1 manzana.
- Chocar contra la pared o contra uno mismo hace **reaparecer la serpiente con 0 manzanas**, pero se guarda el **récord**: la mayor cantidad de manzanas que llegó a tener en la ronda. **Los puntos se calculan con el récord**, así que chocar no te hace perder lo que ya lograste.
- **La serpiente acelera** durante la ronda (de 0,098 s a 0,065 s por paso).
- No se puede dar media vuelta.

#### 2. Lights — `LightsScreen`
- **3 secuencias** de **4, 5 y 6 luces** dentro de los 10 s. Cada una se muestra en 1 s (así que la última va más rápido).
- **Los dos reciben la misma secuencia**; gana la secuencia el primero que la repite completa.
- Equivocarse **reinicia el progreso** de esa secuencia.
- Las teclas se ven como teclas reales (pack de Kenney) que se encienden con el color de cada dirección.

#### 3. Ships — `ShipsScreen`
- Pantalla dividida: cada barco se mueve de izquierda a derecha en su zona.
- **Los dos reciben exactamente el mismo patrón de balas** (se genera una sola vez por ronda).
- La caída se acelera fuerte hacia el final (curva de dificultad 2,2: tranquilo al principio, lluvia al final).
- El primero al que le pega una bala pierde. Si les pegan a los dos a la vez, o sobreviven los dos, es empate.

#### 4. Tug of War — `TugOfWarScreen`
- Hay que **alternar** las dos teclas (`A`/`D` o `←`/`→`). Apretar la misma dos veces seguidas **no tira**.
- Cada tirón mueve la soga 5 px; a los 150 px se llega a la línea y se gana en el momento.
- Si nadie llega, gana quien tenga la soga de su lado al final (1 punto).

#### 5. Quickdraw — `QuickdrawScreen`
- Duelo del oeste: cada jugador tiene **3 vidas** (corazones arriba).
- Cada tanto aparece en el centro **la tecla que hay que apretar** (dibujada en grande para cada jugador).
- El primero que aprieta la correcta le saca una vida al rival.
- **Apretar una tecla equivocada o antes de tiempo te saca una vida a vos.**
- Termina cuando alguien se queda sin vidas o se acaba el tiempo.

#### 6. Race — `RaceScreen`
- Cada auto tiene una pista de 3 carriles; se cambia de carril con izquierda/derecha.
- La velocidad sube sola cada segundo y la distancia se acumula.
- Los obstáculos aparecen **cada cierta distancia recorrida** (siempre separados, nunca amontonados).
- **Chocar** deja la velocidad en 0 y rompe el obstáculo. La parte trasera del auto no cuenta como choque (para que no frene si apenas lo rozás al cambiar de carril).

#### 7. RPS (piedra, papel o tijera) — `RpsScreen`
- Manos rápidas: cuenta regresiva **2, 1** (0,4 s por número) y se revela durante 0,5 s, así que entran unas **7 manos** en 10 s.
- Durante la cuenta, cada uno elige en secreto (`A` piedra, `S` papel, `D` tijera / `←` piedra, `↓` papel, `→` tijera). Si no elige, queda papel.
- Gana la ronda quien ganó más manos y suma **1 punto por mano ganada, hasta 5** (`PUNTOS_MAXIMOS`). Es el único juego que puede dar más de 3.

#### 8. Tank Duel — `TankDuelScreen`
- Tanques que se mueven en 4 direcciones y disparan hacia donde miran.
- **3 vidas** cada uno (corazones arriba). Las balas se rompen contra las cajas y los bordes.
- Los tanques no se pueden encimar.

#### 9. Bomb Tag — `BombTagScreen`
- Uno empieza con la bomba (al azar). **Al tocar al rival, se la pasa**, con un momento de inmunidad para que no vuelva enseguida.
- El que tiene la bomba corre un 8% más rápido.
- **Dash** con `ESPACIO` / `ENTER` (recarga de 2 s; se ve abajo de cada lado).
- A los 10 s explota la bomba: pierde quien la tiene. **Nunca hay empate.**

#### 10. Ping Pong — `PingPongScreen`
- Paletas verticales a cada lado, una "bomba" como pelota.
- Cada rebote en una paleta la acelera un 8%.
- El primero en meter **2 goles** gana en el momento.

#### 11. Sumo — `SumoScreen`
- Empujar al rival fuera de la plataforma. **La plataforma se achica** con el tiempo (de 440 a 260 px).
- Si los dos empujan de frente, hay una onda de choque y rebotan.
- `W` / `↑` para saltar (sirve para esquivar).
- Si nadie cae, gana el que terminó más cerca del centro.

#### 12. Stop It — `StopItScreen`
- Un marcador va y viene sobre una barra; hay que **frenarlo dentro de la zona**.
- Perfecto = 3, bien = 2, cerca = 1, afuera = 0. Después de cada intento la zona se achica y el marcador va más rápido.
- Gana quien suma más; los puntos de RAMSHI salen de la diferencia.

---

## 6. Estructura del proyecto

```
RAMSHI/
├── assets/                       Imágenes y fuentes (una carpeta por minijuego)
│   ├── bombtag/  lights/  pingpong/  quickdraw/  race/  rps/
│   ├── ships/  snake/  stopit/  sumo/  tanks/  tugofwar/
│   ├── menu/minijuegos/          Logos circulares del selector de Endless (170 × 170)
│   └── ui/
│       ├── fonts/                Pixelify Sans (+ licencia OFL)
│       ├── kenney/               Pack de teclas de Kenney (ver INDICE.md)
│       └── logo.png              (futuro) Logo del menú principal
├── core/src/main/java/io/github/ramshi/
│   ├── Main.java                 Arranque del juego y fuentes compartidas
│   ├── menus/
│   │   ├── MenuScreen.java           Base de todos los menús (cámara, botones, VOLVER, textos)
│   │   ├── Botonera.java             Los botones (clase Boton adentro) y la lista que los dibuja y maneja
│   │   ├── Paginador.java            "3/14" con flechas, en bucle (Guía y Endless)
│   │   ├── MainMenuScreen.java       Menú principal
│   │   ├── JugarMenuScreen.java      Elegir Local o En red
│   │   ├── ModoMenuScreen.java       Rondas y Endless (el mismo para Local y para la sala en red)
│   │   ├── SalaMenuScreen.java       En red: crear sala o unirse
│   │   ├── CrearSalaScreen.java      El anfitrión ve su IP y espera
│   │   ├── UnirseSalaScreen.java     Buscar salas o escribir la IP
│   │   ├── EsperaSalaScreen.java     El invitado espera que el anfitrión elija
│   │   ├── EndlessMenuScreen.java    Selector circular de minijuego
│   │   ├── GuiaScreen.java           Guía en páginas
│   │   ├── AjustesScreen.java        Pantalla de ajustes y guardado de la pantalla completa
│   │   └── ResultsScreen.java        Resultado final de la partida por rondas
│   ├── screens/
│   │   ├── BaseMinigameScreen.java   Base de todos los minijuegos (HUD, reloj, pausa, tablas)
│   │   ├── Controles.java            Teclas de Kenney: cartel de inicio y barra de abajo
│   │   └── *Screen.java              Un archivo por minijuego (12)
│   ├── red/
│   │   ├── Red.java                  Conexión UDP, salas y mensajes
│   │   ├── HiloRed.java              Hilo que recibe los paquetes
│   │   ├── Sincronizador.java        Lockstep: pasos fijos con las teclas de los dos
│   │   └── EntradaRed.java           Teclado falso que ven los minijuegos en red
│   └── session/
│       ├── GameSession.java          Estado de la partida (modo, cola de juegos, puntos)
│       └── MinigameType.java         Catálogo de minijuegos (nombre, clase y textos de la Guía)
├── lwjgl3/                       Lanzador de escritorio (ventana 800 × 480)
├── CREDITS.md                    Créditos de assets y fuentes
└── README.md
```

Todo el código (variables, métodos, constantes) está **en español** para que sea más fácil de estudiar. Los nombres de las clases y archivos quedaron en inglés.

---

## 7. Cómo funciona por dentro

### `Main`
Extiende `Game` de libGDX. Al arrancar crea **5 fuentes** (Pixelify Sans en 16, 20, 24, 32 y 56 px) que usan todas las pantallas (`Main.fuenteChica`, `fuenteMediana`, `fuenteGrande`, `fuenteCartel`, `fuenteTitulo`), activa la pantalla completa si estaba guardada y abre el menú. Al cerrar, libera las fuentes y las texturas de las teclas.

### Menús (`menus/`)
- **`MenuScreen`** es la base de todos los menús: arma la cámara de 800 × 480, maneja el mouse, dibuja los botones y agrega el botón **VOLVER** (`agregarVolver(...)`, que también responde a `ESC`). Cada menú solo define qué botones tiene y qué dibuja en `dibujar()`. También hereda de ella la pantalla de resultados (`ResultsScreen`), que usa `fundidoEntrada` para aparecer con fundido.
- **`Boton`** (definido dentro de `Botonera`) tiene posición, texto, color (`Boton.VERDE`, `Boton.ROJO`, `Boton.NEUTRO`) y la acción al tocarlo. Con `activo = false` queda gris y no responde (así está REVANCHA para el invitado en red).
- **`Botonera`** es la lista de botones de una pantalla: guarda cuál está **seleccionado**, lo mueve con el teclado buscando el botón más cercano en esa dirección (`mover(dx, dy)`), detecta el mouse, devuelve el botón elegido con clic, `ENTER` o `ESPACIO` (`elegido()`) y los dibuja. Con `navegarHorizontal = false` solo se mueve arriba/abajo (lo usan Endless y la Guía, donde izquierda/derecha cambian de página). Con `enfocar(boton)` se elige cuál arranca seleccionado. **La usa también la pausa de los minijuegos**, así que todos los botones del juego se ven igual.
- **`Paginador`** guarda en qué página estás, la cambia con flechas o teclas y dibuja el `3/14`. Siempre es un bucle. Lo usan la Guía y el selector de Endless.
- **`AjustesScreen`** además de ser la pantalla, guarda la pantalla completa con las `Preferences` de libGDX (en Windows quedan en la carpeta `.prefs` del usuario, no en el repo).

### `BaseMinigameScreen`
Clase abstracta de la que heredan los 12 minijuegos. Se encarga de todo lo común:

- Cámara y mundo virtual de **800 × 480** (`ANCHO_VIRTUAL`, `ALTO_VIRTUAL`).
- **Cronómetro** de la ronda, cartel de inicio, HUD, barra de controles, corazones y pantallas de fin de ronda.
- **Pausa** con `ESC` (tabla de la partida o marcador de Endless, con REANUDAR y SALIR).
- Pasar al siguiente juego (partida por rondas) o reiniciar la ronda (modo Endless).
- Calcular cuántos puntos sumó cada jugador en ese juego.

**Métodos que cada minijuego tiene que implementar (obligatorios):**

| Método | Qué hace |
|---|---|
| `getDuracionRonda()` | Devuelve la duración (10 s). |
| `actualizarJuego(delta)` | Lógica de cada frame. |
| `dibujarJuego(batch)` | Dibuja el juego. |
| `determinarGanador()` | Reparte los puntos y devuelve el mensaje del ganador. |
| `reiniciarRonda()` | Deja todo como al principio. |
| `getTextoP1()` / `getTextoP2()` | Texto de arriba de cada jugador. |
| `getResumen()` | El "motivo" que se muestra al terminar. |

**Métodos opcionales (si no se definen, se usa el valor por defecto):**

| Método | Para qué |
|---|---|
| `getConsigna()` | Palabra del cartel de inicio. Por defecto: "¡PREPARATE!". |
| `getControles()` | Teclas para el cartel de inicio y la barra de abajo. Por defecto: ninguna. |
| `getVidaMaxima()` / `getVida(jugador)` | Si se definen, la base dibuja los corazones. |

**Herramientas que da la base:**

| Herramienta | Para qué |
|---|---|
| `terminarRonda()` | Terminar antes de tiempo (por ejemplo, un KO). Solo cuenta la primera vez. |
| `sumarPuntos(jugador, puntos)` | Suma los puntos y arma el mensaje "¡JUGADOR N GANA (+X PUNTOS)!". |
| `puntosPorTiempo()` | 3, 2 o 1 según en qué tercio de la ronda terminó. |
| `explicacion` | Texto que explica los puntos (se muestra al terminar). |
| `segundosJugados()` / `reglaTiempo()` | Para armar la explicación de los juegos por rapidez. |
| `usarFuente(Main.fuenteX)` | Cambiar de tamaño de letra conservando el color. |
| `dibujarDerecha(texto, y)` | Escribir pegado al borde derecho. |

### `Controles`
Tiene las teclas del pack de Kenney como constantes (`W`, `A`, `S`, `D`, `ARRIBA`, `IZQUIERDA`, `ABAJO`, `DERECHA`, `ESPACIO`, `ENTER`) y tres formas de declarar un control:

```java
Controles.cruz("mover")                                              // WASD / flechas en forma de cruz
Controles.dos("mover", Controles.A, Controles.D, Controles.IZQUIERDA, Controles.DERECHA)
Controles.una("saltar", Controles.W, Controles.ARRIBA)
Controles.una("piedra", Controles.A, Controles.IZQUIERDA).conIcono("rps/piedra.png")  // dibujo en vez de texto
```

### `GameSession` y `MinigameType`
- `MinigameType` es la **lista de minijuegos**: por cada juego tiene el nombre que se ve (igual al de su clase: `Snake`, `Lights`, `Ships`, `Tug of War`, `Quickdraw`, `Race`, `RPS`, `Tank Duel`, `Bomb Tag`, `Ping Pong`, `Sumo`, `Stop It`), la clase y los textos de la Guía (*cómo se juega*, *meta* y *puntos*). **La Guía y el selector de Endless se arman solos a partir de esta lista**, sin límite de cantidad.
- `GameSession` guarda el modo (rondas o Endless), la cola de juegos y los resultados. La cola se mezcla con una **semilla** (`iniciarJugar(rondas, semilla)`): con la misma semilla sale siempre el mismo orden.

### Red (`red/`)

Se usa **UDP** con `DatagramSocket`, igual que en los ejemplos de clase: un hilo (`HiloRed`, que extiende `Thread`) se queda esperando paquetes con `receive()` y cada mensaje es un texto separado por `;`. El anfitrión escucha en el **puerto 9999** y el invitado usa un puerto que elige el sistema.

**La idea principal (*lockstep*): no se manda el juego, se mandan las teclas.**
Las dos computadoras ejecutan el mismo minijuego al mismo tiempo. Lo único que viaja por la red es qué botones aprieta cada jugador, 60 veces por segundo. Si las dos arrancan igual y reciben las mismas teclas en el mismo momento, el juego queda idéntico en las dos. Para que eso se cumpla:
- **Pasos fijos:** en red, el juego avanza en pasos de exactamente 1/60 s (`Sincronizador.TICK`). Cada paso se ejecuta solo cuando ya se tienen las teclas de los dos jugadores para ese paso.
- **Mismo azar:** al empezar cada minijuego, `MathUtils.random` arranca con la **misma semilla** en las dos (`GameSession`), así los obstáculos, balas y manzanas salen iguales.
- **Retraso de 4 pasos:** lo que apretás se aplica 4 pasos después (unos 67 ms), para darle tiempo a llegar a la otra computadora sin que el juego se frene.

**Los minijuegos no saben que están en red.** Siguen preguntando `Gdx.input.isKeyPressed(Input.Keys.W)` como siempre. Durante cada paso en red, `Gdx.input` se reemplaza por un "teclado falso" (`EntradaRed`) que responde con las teclas sincronizadas de los dos jugadores: el anfitrión maneja las teclas del Jugador 1 (WASD + ESPACIO) y el invitado las del Jugador 2 (flechas + ENTER).

| Clase | Qué hace |
|---|---|
| `Red` | La conexión: crear sala, buscar salas (difusión a `255.255.255.255`), conectar, mensajes, saludos para detectar cortes y avisos. |
| `HiloRed` | El hilo que recibe los paquetes (como `HiloServidor`/`HiloCliente` de los ejemplos). |
| `Sincronizador` | El *lockstep*: junta las teclas propias, las manda, espera las del otro y ejecuta los pasos. |
| `EntradaRed` | El "teclado falso" que ven los minijuegos durante cada paso. |

**Mensajes:**

| Mensaje | De quién | Para qué |
|---|---|---|
| `BUSCAR` / `ANUNCIO;nombre` | invitado / anfitrión | Encontrar salas en la red sin escribir la IP |
| `CONECTAR` / `BIENVENIDO;nombre` / `LLENA` | invitado / anfitrión | Unirse a la sala (solo entra un invitado) |
| `E;sesión;juego;desde;teclas` | los dos | Las teclas de los últimos 30 pasos (se repiten por si se pierde algún paquete) |
| `C;id;texto` / `OK;id` | los dos | Mensajes importantes que **no se pueden perder**: se reenvían hasta que llega el `OK` |
| `INICIAR;RONDAS;n;semilla;sesión` | anfitrión | Arrancar una partida (también `INICIAR;ENDLESS;JUEGO;...`) |
| `SALA` | los dos | Volver a la sala |
| `HOLA` | los dos | Saludo cada medio segundo; 5 segundos sin noticias = conexión perdida |
| `SALIR` | los dos | Me voy |

UDP no asegura que los paquetes lleguen, por eso las teclas se mandan repetidas y los mensajes importantes (`C;...`) se reenvían hasta recibir confirmación.

---

## 8. Cómo agregar un minijuego

1. **Sprites:** crear `assets/mijuego/` y poner ahí los PNG. Pixel art con filtro `Nearest`; fondos grandes con `Linear`.
2. **Clase:** crear `core/src/main/java/io/github/ramshi/screens/MiJuegoScreen.java`:

```java
public class MiJuegoScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    // ... todos los ajustes del juego acá arriba, como private static final

    public MiJuegoScreen(Game juego) {
        super(juego);
        reiniciarRonda();
    }

    @Override protected String getConsigna() { return "¡HACÉ ALGO!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] { Controles.cruz("mover") };
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }
    @Override protected void actualizarJuego(float delta) { /* lógica, usando delta */ }
    @Override protected void dibujarJuego(SpriteBatch batch) { /* dibujo */ }

    @Override
    protected String determinarGanador() {
        // empate: return "¡EMPATE!";
        explicacion = "Qué hizo el ganador y qué regla se aplicó.";
        return sumarPuntos(1, 2);
    }

    @Override protected void reiniciarRonda() { /* estado inicial */ }
    @Override protected String getTextoP1() { return "P1 (Victorias: " + victoriasP1 + ")"; }
    @Override protected String getTextoP2() { return "P2 (Victorias: " + victoriasP2 + ")"; }
    @Override protected String getResumen() { return "Qué pasó en el juego"; }

    @Override
    public void dispose() {
        super.dispose();
        // liberar las Texture propias
    }
}
```

3. **Registrar:** agregar una entrada en `session/MinigameType.java`. Con eso ya aparece en la partida por rondas, en el selector de Endless y en la Guía:

```java
MI_JUEGO("Mi Juego", MiJuegoScreen::new,
    "Cómo se juega, en una o dos oraciones.",
    "Cuál es la meta.",
    "Cómo se reparten los puntos."),
```

4. **(Opcional) Logo del selector:** `assets/menu/minijuegos/mi_juego.png` (el nombre de la constante en minúsculas), de 170 × 170 px, recortado en círculo con fondo transparente.

**Reglas para que funcione bien en la partida por rondas:**
- La duración tiene que ser una constante `static final` (la base la lee en su constructor, antes de que existan los campos del juego; si fuera un campo normal valdría 0 y el juego terminaría al instante).
- El ganador suma entre 1 y 3 puntos con `sumarPuntos(...)`; el empate no suma.
- `dispose()` siempre llama a `super.dispose()`.
- No cambiar de pantalla ni manejar `ESC` desde el juego: lo hace la base (la pausa).

**Reglas para que funcione en red** (si no se cumplen, las dos computadoras se desincronizan):
- Toda la lógica va en `actualizarJuego(delta)` y usa **solo ese `delta`** (nada de `Gdx.graphics.getDeltaTime()` ni la hora del sistema).
- `dibujarJuego` **solo dibuja**: no cambia variables del juego.
- El azar sale **solo de `MathUtils.random`** (nada de `Math.random()` ni `new Random()`).
- Para potencias, raíces raras o trigonometría usar `MathUtils` o `StrictMath`, que dan el mismo resultado en cualquier procesador.
- Las teclas se leen con `Gdx.input.isKeyPressed` / `isKeyJustPressed` y solo estas: `W A S D ESPACIO` (Jugador 1) y flechas + `ENTER` (Jugador 2).

---

## 9. Ajustes rápidos

Todos los valores que se pueden ajustar están como **constantes al principio de cada clase**. Los más usados:

| Dónde | Constante | Valor | Qué cambia |
|---|---|---|---|
| `BaseMinigameScreen` | `SEGUNDOS_INTRO` | 1.5 | Duración del cartel de inicio |
| `BaseMinigameScreen` | `SEGUNDOS_RESULTADO` | 2.5 | Duración de la pantalla de fin de ronda |
| `BaseMinigameScreen` | `DURACION_FUNDIDO` | 0.3 | Duración del fundido a negro entre pantallas |
| `BaseMinigameScreen` | `SEGUNDOS_ALERTA` | 3 | Desde qué segundo el número del reloj se pone rojo |
| `BaseMinigameScreen` | `RELOJ_RADIO` / `ALTO_BARRA` | 24 / 8 | Tamaño del reloj y grosor de las barras de tiempo |
| `ModoMenuScreen` | `MIN_RONDAS` / `MAX_RONDAS` / `RONDAS_INICIALES` | 3 / 12 / 8 | Rondas de la partida |
| `EndlessMenuScreen` | `DURACION_DESLIZAR` | 0.18 | Velocidad de la animación del selector |
| `EndlessMenuScreen` | `RADIO_CENTRO` / `RADIO_LADO` | 85 / 62 | Tamaño del círculo del centro y de los costados |
| Cada minijuego | `DURACION_RONDA` | 10 | Duración del juego |
| Snake | `INTERVALO_INICIAL` / `INTERVALO_FINAL` | 0.098 / 0.065 | Velocidad de la serpiente |
| Lights | `LARGOS_SECUENCIA` | {4, 5, 6} | Cantidad de luces (y de secuencias) |
| Lights | `TIEMPO_MUESTRA` | 1 | Tiempo en que se muestra cada secuencia |
| Lights | `REINICIAR_AL_FALLAR` | true | Si equivocarse reinicia el progreso |
| Ships | `VELOCIDAD_CAIDA_FIN` / `CURVA_DIFICULTAD` | 1150 / 2.2 | Qué tan brusca es la lluvia al final |
| Tug of War | `PASO_TIRON` / `DESPLAZAMIENTO_MAX` | 5 / 150 | Cuántos tirones hacen falta para ganar |
| Quickdraw | `TIEMPO_REACCION` | 0.35 | Ventana para disparar |
| Race | `SEPARACION_INICIAL` / `SEPARACION_FINAL` | 320 / 260 | Distancia entre obstáculos |
| Race | `PARTE_TRASERA_LIBRE` / `MARGEN_GOLPE` | 0.35 / 10 | Qué tan permisivo es el choque |
| RPS | `PASOS_CUENTA` / `TIEMPO_PASO` / `TIEMPO_REVELAR` | 2 / 0.4 / 0.5 | Cuenta regresiva y velocidad de cada mano |
| RPS | `PUNTOS_MAXIMOS` | 5 | Tope de puntos por ronda |
| Tank Duel | `TIEMPO_RECARGA` / `VIDA_MAXIMA` | 0.30 / 3 | Cadencia de disparo y vidas |
| Bomb Tag | `RECARGA_DASH` / `BONUS_PORTADOR` | 2.0 / 1.08 | Dash y ventaja del que tiene la bomba |
| Ping Pong | `GOLES_PARA_GANAR` / `ACELERACION_REBOTE` | 2 / 1.08 | Goles y aceleración de la bomba |
| Sumo | `PLAT_ANCHO_INICIAL` / `PLAT_ANCHO_FINAL` | 440 / 260 | Cuánto se achica la plataforma |
| Stop It | `VELOCIDAD_INICIAL` / `MEDIA_ZONA_INICIAL` | 0.9 / 0.09 | Velocidad del marcador y tamaño de la zona |

Los cortes de puntos también son constantes: `DIFERENCIA_PARA_3` / `DIFERENCIA_PARA_2` (Snake, Stop It), `VENTAJA_PARA_3` / `VENTAJA_PARA_2` (Race), `PROPORCION_PARA_3` / `PROPORCION_PARA_2` (Bomb Tag), `PUNTOS_KO_PERFECTO` / `PUNTOS_KO` / `PUNTOS_VICTORIA` (Tank Duel). Los cortes por rapidez (3,3 s y 6,6 s) están en `puntosPorTiempo()` de la base.

---

## 10. Assets, fuente y créditos

- **Fuente:** [Pixelify Sans](https://github.com/eifetx/Pixelify-Sans) (licencia SIL OFL 1.1), cargada con la extensión `gdx-freetype`.
- **Teclas:** pack *Input Prompts Pixel 1-Bit* de [Kenney](https://kenney.nl) (CC0). Está completo en `assets/ui/kenney/`; el archivo `INDICE.md` de esa carpeta dice qué tecla es cada `tile_XXXX.png` (las teclas rellenas son el número + 272).
- **Fondos y sprites de los minijuegos:** hechos o conseguidos por el equipo.

Todo lo que no sea propio tiene que anotarse en [`CREDITS.md`](CREDITS.md) con autor, licencia y origen.

---

## 11. Problemas comunes

| Qué pasa | Por qué | Cómo se arregla |
|---|---|---|
| `gradlew` no se reconoce en PowerShell | PowerShell no ejecuta archivos de la carpeta actual sin `.\` | Usar `.\gradlew.bat lwjgl3:run` |
| Error de Java / versión no soportada | No hay JDK o es muy viejo | Instalar un JDK 17 o 21 y reabrir la terminal |
| La primera ejecución tarda mucho | Gradle descarga libGDX y FreeType | Esperar; las siguientes son rápidas |
| Un minijuego termina apenas empieza | `DURACION_RONDA` no es `static final` | Ver las reglas de la [sección 8](#8-cómo-agregar-un-minijuego) |
| Falta una imagen (`File not found`) | La ruta no coincide con la carpeta de `assets/` | Revisar mayúsculas y nombre exacto del PNG |
| Dos teclas a la vez no responden | Algunos teclados no registran ciertas combinaciones | Es del hardware (*ghosting*); probar con otro teclado |
| En red, "No hubo respuesta" | El firewall de Windows bloquea Java en la PC del anfitrión, o no están en la misma red | En el anfitrión, permitir Java en el aviso del firewall (o en *Firewall de Windows → Permitir una aplicación*); revisar que estén en el mismo Wi-Fi |
| "No se pudo abrir la sala en el puerto 9999" | Ya hay una sala abierta en esa computadora (u otro programa usa el puerto) | Cerrar la otra ventana del juego |
| La sala no aparece en BUSCAR SALAS | Algunas redes (por ejemplo la del colegio) bloquean la difusión | Escribir la IP del anfitrión a mano |

---

## 12. Equipo y licencia

Proyecto académico grupal. Integrantes (usuarios de Git): **TIXIANO70**, **Hung Nicolás**, **puroh**, **JuaDES** y **Adrián Escalante**.

Licencia: **MIT** (ver [`LICENSE`](LICENSE)).

**Forma de trabajo con Git:** cada cambio va en una rama propia (`feature/...`, `fix/...`, `refactor/...`) → Pull Request → merge a `main`. Antes de empezar algo nuevo, `git pull` en `main`.
