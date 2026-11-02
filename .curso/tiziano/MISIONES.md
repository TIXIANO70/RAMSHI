# 🗺️ Plan de Misiones — Curso de Programación y Defensa Técnica RAMSHI

**Alumno:** tiziano  
**Minijuego asignado:** Galactica 1v1 (`galactica`)  
**Stack:** Java + LibGDX + Gradle  
**Metodología:** Mastery-Based Learning, práctica incremental y preparación para defensa oral.

---

# BLOQUE A — Base Lógica y Arquitectura de RAMSHI

## Misión 1 — Anatomía del Proyecto, Build System y Launcher
- **Objetivo:** Explicar con precisión por qué RAMSHI está partido en dos módulos (`:core` y `:lwjgl3`), cómo funciona Gradle y cómo arranca el juego en la JVM de escritorio.
- **Conceptos nuevos:**
  - Build automation con Gradle (`build.gradle`, subproyectos, dependencias).
  - Desacoplamiento de plataforma: lógica pura en `:core` vs backend de escritorio en `:lwjgl3`.
  - Punto de entrada (`public static void main`) y configuración de ventana en LibGDX (`Lwjgl3ApplicationConfiguration`).
  - Resolución virtual fija ($800 \times 480$) vs resolución real de monitor.
- **Archivos a leer:**
  - `build.gradle` (raíz): líneas 28-56 (configuración de subproyectos y assets).
  - `core/build.gradle`: dependencias de LibGDX (`gdx`).
  - `lwjgl3/build.gradle`: dependencias de backend (`gdx-backend-lwjgl3`, `natives-desktop`).
  - `lwjgl3/src/main/java/io/github/some_example_name/lwjgl3/Lwjgl3Launcher.java`: métodos `main()`, `createApplication()`, `getDefaultConfiguration()`.
- **Preguntas de comprensión:**
  1. ¿Por qué el código de nuestros minijuegos se escribe en `:core` y no directamente en `:lwjgl3`?
  2. ¿Qué pasa si intentás importar clases nativas de escritorio o AWT/Swing dentro del módulo `:core`?
  3. En `Lwjgl3Launcher.java`, ¿para qué se configuran `setWindowedMode(800, 480)` y `useVsync(true)`?
  4. ¿Qué rol cumple la clase `Main` al pasarse como argumento en `new Lwjgl3Application(new Main(), ...)`?
- **Ejercicio práctico:**
  - Inspeccionar `Lwjgl3Launcher.java`, modificar temporalmente el título de la ventana a `"RAMSHI 1v1 - Edicion Tiziano"` y verificar que la ventana inicie con dicho título.
- **Criterio de aprobación:**
  - `./gradlew lwjgl3:build` termina en `BUILD SUCCESSFUL`.
  - `./gradlew lwjgl3:run` abre la ventana con el título modificado.
- **Preguntas de defensa oral:**
  1. *Si el tribunal te pregunta: "¿Por qué el proyecto usa dos carpetas separadas (core y lwjgl3) en vez de poner todo el código junto?", ¿qué respondés?*
  2. *¿Qué es el VSync y por qué está activado en la configuración de arranque?*

---

## Misión 2 — Game Loop, Navegación de Pantallas y Ciclo de Vida
- **Objetivo:** Explicar cómo fluye el ciclo de vida de LibGDX (`create`, `render`, `resize`, `dispose`), cómo `Main extends Game` delega el control en `Screen`, y cómo funciona el menú interactivo con cálculo de coordenadas relativas.
- **Conceptos nuevos:**
  - Clase `Game` vs interfaz `Screen` (patrón State/Strategy para pantallas).
  - El ciclo de vida de una pantalla: `show()`, `render(float delta)`, `resize(width, height)`, `hide()`, `dispose()`.
  - `delta time` ($\Delta t$): tiempo entre fotogramas y su importancia para la física independiente del framerate.
  - Gestión de memoria no gestionada por la JVM: interfaz `Disposable` y liberación explícita de GPU con `dispose()`.
  - Coordenadas virtuales, desproyección del mouse con `viewport.unproject()` y detección de colisión con botones (`Rectangle.contains`).
- **Archivos a leer:**
  - `core/src/main/java/io/github/some_example_name/Main.java`: métodos `create()`, `render()`.
  - `core/src/main/java/io/github/some_example_name/screens/MainMenuScreen.java`: constructor, `render(float delta)`, `drawCenteredText()`, `dispose()`.
- **Preguntas de comprensión:**
  1. ¿Qué diferencia fundamental hay entre el método `render()` de la clase `Main` y el de `MainMenuScreen`?
  2. ¿Qué es el parámetro `delta` que recibe `render(float delta)` y por qué nunca debemos hardcodear velocidades en píxeles fijos por fotograma?
  3. ¿Por qué es obligatorio llamar a `viewport.unproject(mousePos)` antes de preguntar si el mouse está sobre un botón?
  4. ¿Por qué objetos como `SpriteBatch`, `ShapeRenderer` y `BitmapFont` deben llamar a `dispose()` al salir de la pantalla? ¿No se encarga el Garbage Collector de Java?
- **Ejercicio práctico:**
  - En `MainMenuScreen.java`, vaciar el bloque de detección de clic del botón "JUGAR 1v1" para reimplementar la transición a `SnakeScreen(game)` asegurando el correcto llamado de `dispose()` de los recursos del menú.
- **Criterio de aprobación:**
  - Build sin advertencias ni errores.
  - Al cliquear "JUGAR 1v1" con el cursor, el juego pasa a la pantalla de juego fluidamente sin memory leaks ni cuelgues.
- **Preguntas de defensa oral:**
  1. *¿Qué pasaría si cambiás de pantalla 50 veces seguidas con `game.setScreen(...)` sin llamar a `dispose()` en la pantalla saliente?*
  2. *¿Cómo garantiza `FitViewport` que el juego no se deforme si el usuario achica o estira la ventana en su monitor?*

---

## Misión 3 — Arquitectura Base de Minijuegos (`BaseMinigameScreen`) y Patrón Template Method
- **Objetivo:** Dominar el diseño abstracto que gobierna todos los minijuegos de RAMSHI: cronómetro universal de 30s, HUD estandarizado, flujo de rondas, marcador global y el patrón de diseño Template Method.
- **Conceptos nuevos:**
  - Patrón de diseño **Template Method**: algoritmo esqueleto en clase abstracta con pasos diferidos a subclases.
  - Métodos abstractos (`abstract`) vs métodos hook/gancho opcionales con implementación por defecto.
  - Modificadores de acceso: `protected` vs `private` vs `public` en jerarquías de herencia.
  - Separación de responsabilidades: la base resuelve la infraestructura común y la subclase únicamente su jugabilidad.
- **Archivos a leer:**
  - `core/src/main/java/io/github/some_example_name/screens/BaseMinigameScreen.java`: contrato completo, `render(float delta)`, `finishRound()`, `startNewRound()`, `drawHUD()`, hooks abstractos.
- **Preguntas de comprensión:**
  1. ¿Cuáles son los 6 métodos abstractos que `BaseMinigameScreen` obliga a implementar a cualquier minijuego nuevo?
  2. ¿En qué orden exacto invoca `BaseMinigameScreen.render(delta)` la lógica de actualización, el renderizado del juego y el dibujado del HUD?
  3. ¿Cómo resuelve la clase base la detección de si la ronda terminó por tiempo versus si una nave o serpiente murió antes?
  4. Si presionamos la tecla `ESCAPE`, ¿quién gestiona la vuelta al menú principal: la clase base o cada minijuego por su cuenta?
- **Ejercicio práctico:**
  - En `BaseMinigameScreen.java`, analizar el método `finishRound()` y agregar una alerta visual en el HUD cuando el tiempo sea menor o igual a 5 segundos (cambio de color dinámico del reloj a rojo intermitente o destacado).
- **Criterio de aprobación:**
  - Build exitoso.
  - El cronómetro del HUD cambia a rojo en los últimos 5 segundos de juego de forma limpia.
- **Preguntas de defensa oral:**
  1. *¿Por qué el equipo eligió usar una clase abstracta `BaseMinigameScreen` en vez de que cada programador arme su propio temporizador y HUD desde cero en su pantalla?*
  2. *¿Por qué `updateMinigame` y `renderMinigame` son `protected` y no `public`?*

---

# BLOQUE B — Dominio del Minijuego Existente: Snake 1v1

## Misión 4 — Estructura de Datos de la Cuadrícula y Spawn de Comida
- **Objetivo:** Comprender la representación espacial en casillas discretas ($16 \times 16$ px), enumeraciones en Java con atributos y el algoritmo de Rejection Sampling para comida.
- **Conceptos nuevos:**
  - Enumeraciones (`enum`) avanzadas en Java con campos y métodos (`Direction`).
  - Representación de cuadrículas bidimensionales y conversión entre coordenadas de celda $(x, y)$ y píxeles de pantalla.
  - Algoritmo de generación aleatoria por muestreo y rechazo (*Rejection Sampling* con bucle `do-while`) evitando solapamientos.
  - Verificación de límites de arena y colisiones de casillas.
- **Archivos a leer:**
  - `core/src/main/java/io/github/some_example_name/entities/Direction.java`: enum, campos `dx`, `dy`, método `isOpposite()`.
  - `core/src/main/java/io/github/some_example_name/entities/Food.java`: atributos, método `respawn()`, `render()`.
  - `core/src/main/java/io/github/some_example_name/entities/SnakeSegment.java`: atributos de nodo de serpiente.
- **Preguntas de comprensión:**
  1. ¿Por qué `Direction.isOpposite()` comprueba la suma de `dx` y `dy` en lugar de comparar nombres de cadenas con `equals()`?
  2. ¿Cómo funciona la condición de parada del bucle `do-while` en `Food.respawn()` y por qué se limita a 100 intentos?
  3. ¿Cómo se traduce la posición discreta de la comida `gridX = 5` en coordenadas de píxeles reales para `SpriteBatch.draw`?
- **Ejercicio práctico:**
  - En `Direction.java`, vaciar el método `isOpposite(Direction other)` para reimplementarlo desde cero garantizando que reconozca giros de 180° opuestos y devuelva `false` ante valores nulos.
- **Criterio de aprobación:**
  - Build exitoso.
  - La serpiente no puede suicidarse doblando 180° hacia adentro sobre sí misma.
- **Preguntas de defensa oral:**
  1. *¿Qué pasaría si en un tablero lleno la comida busca respawnear sin límite de intentos en el `do-while`?*
  2. *¿Por qué usamos un `enum` para las direcciones en vez de simples números enteros (`1, 2, 3, 4`) o Strings (`"ARRIBA"`, etc.)?*

---

## Misión 5 — Simulación de Movimiento por Ticks y Crecimiento de Serpiente
- **Objetivo:** Explicar el desacoplamiento entre los FPS de renderizado (60 FPS) y la tasa de simulación del juego (Ticks de 0.12s), y cómo una lista de segmentos modela la serpiente.
- **Conceptos nuevos:**
  - Acumulador de tiempo de tick (`moveTimer += delta; while(moveTimer >= MOVE_INTERVAL)`).
  - Colecciones en Java (`List<SnakeSegment>`, `ArrayList`).
  - Dinámica de avance: inserción de nueva cabeza en el frente y remoción condicional de la cola (patrón oruga / FIFO modificado).
  - Orientación dinámica de la textura de la cola según el segmento predecesor.
  - Detección de auto-colisión y choque con bordes de la cuadrícula.
- **Archivos a leer:**
  - `core/src/main/java/io/github/some_example_name/entities/Snake.java`: constructor, `step(Direction)`, `grow()`, `updateTailOrientation()`, `checkSelfCollision()`, `checkOutOfBounds()`.
- **Preguntas de comprensión:**
  1. Si el juego corre a 60 FPS, ¿por qué la serpiente no avanza 60 casillas por segundo? ¿Dónde se controla su ritmo?
  2. Al avanzar un paso en `step()`, ¿qué se hace exactamente con la cabeza actual, la nueva cabeza y la cola si la serpiente NO comió?
  3. ¿Y qué pasa cuando `growPending > 0`? ¿Por qué la serpiente crece de tamaño?
  4. ¿Cómo detecta `checkSelfCollision()` si la cabeza se chocó a sí misma sin confundirse con el segmento de la cabeza?
- **Ejercicio práctico:**
  - En `Snake.java`, vaciar el algoritmo de `checkSelfCollision()` para reimplementar la iteración sobre la lista de segmentos comprobando si las coordenadas de la cabeza coinciden con algún segmento corporal.
- **Criterio de aprobación:**
  - Build exitoso.
  - Si la serpiente se choca contra su propio cuerpo, se detecta inmediatamente la colisión y reaparece.
- **Preguntas de defensa oral:**
  1. *¿Por qué se utiliza una lista donde el último elemento es la cabeza y el primero es la cola? ¿Qué beneficio tiene al agregar y quitar segmentos?*
  2. *¿Por qué las texturas de la cabeza y la cola cambian de orientación y cómo lo resuelve la clase?*

---

## Misión 6 — Entrada Desacoplada y Buffering Atómico (`InputHandler`)
- **Objetivo:** Explicar la diferencia entre polling de entrada en cada cuadro y captura dirigida por eventos de hardware (`InputAdapter`), y cómo se evita la pérdida de pulsaciones o sobreescritura accidental.
- **Conceptos nuevos:**
  - Arquitectura de eventos en LibGDX: interfaz `InputProcessor` y clase base `InputAdapter`.
  - Buffer de entrada atómico (*Atomic Input Buffer*): retener la intención del jugador entre ticks de simulación.
  - Doble capa de entrada: evento `keyDown` prioritario respaldado por polling `Gdx.input.isKeyJustPressed / isKeyPressed`.
  - Controles simétricos simultáneos: P1 (WASD) vs P2 (Flechas y teclado numérico).
- **Archivos a leer:**
  - `core/src/main/java/io/github/some_example_name/utils/InputHandler.java`: métodos `keyDown()`, `getP1Direction()`, `getP2Direction()`, `reset()`.
- **Preguntas de comprensión:**
  1. Si un jugador pulsa 'W' y al instante 'D' entre un tick de 0.12s, ¿qué problema ocurriría si solo usamos polling `Gdx.input.isKeyPressed`?
  2. ¿Por qué `InputHandler.getP1Direction()` consume la variable buffered (`p1BufferedDirection = null`) inmediatamente después de leerla?
  3. ¿Qué rol juega el método `reset()` y en qué momento del ciclo de vida de la ronda debe ejecutarse?
- **Ejercicio práctico:**
  - En `InputHandler.java`, vaciar el bloque de captura de teclas del Jugador 2 en `keyDown()` para reimplementarlo aceptando tanto las flechas del cursor como el teclado numérico (`NUMPAD_8, NUMPAD_2, NUMPAD_4, NUMPAD_6`).
- **Criterio de aprobación:**
  - Build exitoso.
  - El Jugador 2 puede controlar con total fluidez a su serpiente con las flechas de dirección.
- **Preguntas de defensa oral:**
  1. *¿Qué es el "input ghosting" o pérdida de teclas en un juego rápido y cómo lo soluciona esta clase?*
  2. *¿Por qué `InputHandler` extiende de `InputAdapter` en vez de implementar directamente `InputProcessor`?*

---

## Misión 7 — Arenas Split-Screen y Pantalla SnakeScreen
- **Objetivo:** Analizar cómo se integran dos arenas completamente independientes dentro de una misma pantalla panorámica y cómo `SnakeScreen` cumple el contrato de `BaseMinigameScreen`.
- **Conceptos nuevos:**
  - Composición de objetos: `SnakeScreen` posee dos instancias de `SnakeArena`.
  - Desplazamiento por offset (`originPixelX`, `originPixelY`) para dibujar tableros separados en la misma cámara.
  - Sobrescribir (`@Override`) los métodos contractuales: `updateMinigame`, `renderMinigame`, `getP1Score`, `getP2Score`, `resetMinigameRound` y `disposeMinigame`.
  - Reinicio simétrico de rondas y preservación de victorias globales.
- **Archivos a leer:**
  - `core/src/main/java/io/github/some_example_name/entities/SnakeArena.java`: constructor, `tick()`, `respawnSnake()`, `render()`.
  - `core/src/main/java/io/github/some_example_name/screens/SnakeScreen.java`: herencia de `BaseMinigameScreen`, métodos sobrescritos.
- **Preguntas de comprensión:**
  1. ¿Cómo sabe la baldosa $(0, 0)$ de la arena de P2 en qué coordenada de pantalla debe dibujarse para no encimarse con la de P1?
  2. ¿Por qué el método `tick()` de la arena incrementa el puntaje pero al morir en `respawnSnake()` lo devuelve a 0?
  3. ¿Cómo se comunican los puntajes de las arenas con el HUD superior de la pantalla base?
- **Ejercicio práctico:**
  - En `SnakeScreen.java`, vaciar los métodos `getP1Score()` y `getP2Score()` para reconectarlos con sus respectivas arenas y verificar que el HUD refleje los puntos en vivo.
- **Criterio de aprobación:**
  - Build exitoso.
  - Ambos jugadores acumulan puntos al comer y el HUD de la base los muestra en tiempo real.
- **Preguntas de defensa oral:**
  1. *Si quisiéramos agregar un tercer o cuarto jugador en la misma pantalla, ¿qué cambios arquitectónicos requeriría `SnakeScreen`?*
  2. *¿Dónde se liberan las texturas del tablero y los sprites al salir de la pantalla?*

---

# BLOQUE C — Desarrollo de Galactica 1v1 (Tu Minijuego)

## Misión 8 — Esqueleto de GalacticaScreen y Acceso desde el Menú Principal
- **Objetivo:** Crear la clase `GalacticaScreen extends BaseMinigameScreen`, registrar sus contratos mínimos y habilitar un botón en `MainMenuScreen` para acceder directamente al juego.
- **Conceptos nuevos:**
  - Creación de subclases de `BaseMinigameScreen` en paquete `io.github.some_example_name.screens`.
  - Llamada obligatoria a constructores padre con `super(game)` y `super(game, p1Wins, p2Wins)`.
  - Actualización de la interfaz del menú: distribución geométrica de botones con `Rectangle`.
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/screens/GalacticaScreen.java` (nuevo archivo).
  - `core/src/main/java/io/github/some_example_name/screens/MainMenuScreen.java`.
- **Ejercicio práctico:**
  - Crear el esqueleto de `GalacticaScreen` con implementaciones stub mínimas que compilen.
  - En `MainMenuScreen`, agregar el botón "GALACTICA 1v1" para poder abrir tu minijuego desde la interfaz principal.
- **Criterio de aprobación:**
  - `./gradlew lwjgl3:build` termina en `BUILD SUCCESSFUL`.
  - Al ejecutar el juego, el menú principal muestra el botón de Galactica y al cliquearlo se abre la pantalla con el HUD de 30s de `BaseMinigameScreen` funcionando.
- **Preguntas de defensa oral:**
  1. *¿Por qué tu constructor en `GalacticaScreen` debe invocar a `super(game)` obligatoriamente?*
  2. *¿Por qué el HUD de 30 segundos y el atajo ESC ya funcionan en tu pantalla sin haber escrito una sola línea de código de tiempo?*

---

## Misión 9 — Arenas Espaciales Split-Screen y Nave del Jugador
- **Objetivo:** Construir la clase `PlayerShip` y `SpaceShipArena` para representar los espacios de combate independientes de P1 y P2 con movimiento horizontal restringido.
- **Conceptos nuevos:**
  - Encapsulamiento de entidades: posición $X, Y$, velocidad horizontal ($V_x$) y dimensiones del sprite.
  - Restricción de bordes de arena: clamping matemático (`MathUtils.clamp`).
  - Renderizado por lotes con `SpriteBatch` y dibujo de marcos delimitadores con `ShapeRenderer`.
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/PlayerShip.java` (nuevo).
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/SpaceShipArena.java` (nuevo).
  - `core/src/main/java/io/github/some_example_name/screens/GalacticaScreen.java`.
- **Ejercicio práctico:**
  - Programar `PlayerShip` con movimiento suave (WASD para P1, Flechas para P2) dentro de sus arenas ($320 \times 380$ px cada una).
- **Criterio de aprobación:**
  - Build exitoso.
  - Ambas naves se mueven fluidamente a izquierda y derecha sin salirse jamás de los límites de sus respectivas mitades de pantalla.
- **Preguntas de defensa oral:**
  1. *¿Cómo evitás que la nave del Jugador 1 invada el carril del Jugador 2?*
  2. *¿Cómo se calcula la velocidad en función de `delta` para que se mueva a la misma velocidad en monitores de 60Hz o 144Hz?*

---

## Misión 10 — Sistema de Disparo Láser y Optimización con Object Pooling
- **Objetivo:** Implementar el disparo láser de alta cadencia aplicando el patrón Object Pooling (`com.badlogic.gdx.utils.Pool`) para garantizar cero pausas de Garbage Collector (*Zero GC*).
- **Conceptos nuevos:**
  - Patrón de diseño **Object Pool** (variante Flyweight para optimización de memoria en Java).
  - Interfaz `Poolable`: métodos `obtain()`, `reset()`, y `free()`.
  - Gestión de colecciones activas reciclables con `com.badlogic.gdx.utils.Array`.
  - Cadencia de tiro (*cooldown timer*) al presionar ESPACIO (P1) y ENTER (P2).
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/LaserBullet.java` (nuevo, implementa `Pool.Poolable`).
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/SpaceShipArena.java`.
- **Ejercicio práctico:**
  - Crear la entidad `LaserBullet` con su `Pool<LaserBullet>` dentro de `SpaceShipArena`. Instanciar disparos con `bulletPool.obtain()` y reciclarlos con `bulletPool.free()` cuando superen el borde superior de la arena.
- **Criterio de aprobación:**
  - Build exitoso.
  - Al mantener o pulsar disparo, salen proyectiles continuos hacia arriba y desaparecen limpiamente arriba sin fugar memoria ni instanciar objetos nuevos en cada frame.
- **Preguntas de defensa oral:**
  1. *¿Por qué en un juego con muchos disparos no hacemos simplemente `new LaserBullet()` en cada pulsación?*
  2. *¿Qué hace el método `reset()` adentro de un objeto `Poolable` y cuándo se ejecuta?*

---

## Misión 11 — Cronología Determinista de Oleadas (`WaveEvent` y `StageSequencer`)
- **Objetivo:** Desacoplar la generación de enemigos del azar mediante una línea de tiempo determinista compartida que garantice estricta paridad competitiva entre ambos jugadores.
- **Conceptos nuevos:**
  - Patrón Command / Secuenciador temporal (*Timeline Scheduler*).
  - Coordenadas normalizadas ($0.0 \le X_{norm} \le 1.0$) proyectadas al ancho específico de cada arena.
  - Clases inmutables de datos (*Data Holders*): `WaveEvent` y tipos de enemigos con `enum EnemyType`.
  - Avance del cursor de eventos (`nextEventIndex`) sobre la cronología de 30 segundos.
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/WaveEvent.java` (nuevo).
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/StageSequencer.java` (nuevo).
  - `core/src/main/java/io/github/some_example_name/screens/GalacticaScreen.java`.
- **Ejercicio práctico:**
  - Implementar `WaveEvent` y `StageSequencer`. Cargar el itinerario de oleadas de 30s de la especificación técnica e inyectar sincrónicamente los avisos de spawn a ambas arenas en el mismo instante temporal.
- **Criterio de aprobación:**
  - Build exitoso.
  - Los eventos se disparan en sincronía milimétrica para P1 y P2 según el tiempo transcurrido de la ronda.
- **Preguntas de defensa oral:**
  1. *¿Por qué un secuenciador de etapas es superior a un generador con `MathUtils.random()` en un juego competitivo 1v1?*
  2. *¿Qué significa que la posición $X$ esté normalizada entre 0.0 y 1.0 y qué ventaja nos da?*

---

## Misión 12 — Entidades Enemigas, Comportamientos y Colisiones
- **Objetivo:** Construir los enemigos espaciales (`EnemyShip`), reciclaje con pool, trayectorias (recta, sinusoidal) y detección de colisiones bounding-box con láseres y naves.
- **Conceptos nuevos:**
  - Trayectorias matemáticas: movimiento lineal uniforme vs oscilación sinusoidal con funciones trigonométricas (`MathUtils.sin`).
  - Detección de colisiones 2D con cajas delimitadoras AABB (`Rectangle.overlaps`).
  - Asignación de puntos y estado de destrucción inmediata de nave.
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/EnemyShip.java` (nuevo).
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/SpaceShipArena.java`.
- **Ejercicio práctico:**
  - Programar la colisión: si un láser colisiona con un enemigo, el enemigo se destruye, el láser se recicla en el pool y el jugador suma puntos. Si un enemigo impacta con la nave del jugador, la nave se marca como destruida.
- **Criterio de aprobación:**
  - Build exitoso.
  - Se puede disparar a los enemigos, suman puntos en el marcador de la arena, y si impactan la nave, la ronda finaliza coronando al rival.
- **Preguntas de defensa oral:**
  1. *¿Cómo resolviste la colisión entre el láser y la nave enemiga en código?*
  2. *¿Por qué no podemos borrar un elemento de un `Array` o `List` con un `for (EnemyShip e : enemies)` tradicional mientras lo estamos recorriendo?*

---

## Misión 13 — Mecánica Competitiva de Sabotaje Cruzado (*Counter-Attack Waves*)
- **Objetivo:** Implementar la interacción competitiva directa entre arenas: al aniquilar una formación enemiga completa, se dispara una amenaza inmediata en la arena del oponente.
- **Conceptos nuevos:**
  - Patrón de diseño **Observer / Event Callback** mediante interfaces funcionales en Java (`Runnable` o custom listener).
  - Desacoplamiento de arenas: `arenaP1` no conoce los detalles internos de `arenaP2`, solo interactúa vía callback.
  - Inyección dinámica de amenazas fuera del cronograma estándar (`injectSabotageHazard()`).
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/minigames/galactica/SpaceShipArena.java`.
  - `core/src/main/java/io/github/some_example_name/screens/GalacticaScreen.java`.
- **Ejercicio práctico:**
  - Configurar en `GalacticaScreen` los callbacks cruzados: `arenaP1.setOnComboClearedListener(() -> arenaP2.injectSabotageHazard())` y viceversa.
- **Criterio de aprobación:**
  - Build exitoso.
  - Al destruir una oleada completa en una de las pantallas, aparece inmediatamente un asteroide o enemigo interceptor en la pantalla rival con alerta visual.
- **Preguntas de defensa oral:**
  1. *¿Cómo se comunican las dos arenas sin estar acopladas directamente entre sí?*
  2. *¿Qué es una expresión lambda en Java y cómo la usaste en el listener de sabotaje?*

---

## Misión 14 — Pulido Final, Gestión de Recursos GPU (`dispose`) y Pull Request
- **Objetivo:** Completar el ciclo de vida de Galactica: reinicio perfecto de rondas con `resetMinigameRound()`, liberación estricta de texturas en `disposeMinigame()`, integración con Git y preparación de PR a `main`.
- **Conceptos nuevos:**
  - Limpieza de memoria GPU sin dejar fugas (texturas de naves, proyectiles, fuentes).
  - Reinicio de estado de juego en nueva ronda preservando victorias globales (`p1GlobalWins`, `p2GlobalWins`).
  - Flujo profesional de Git: `git pull origin main`, resolución de conflictos, push de rama `feature/galactica` y apertura de Pull Request.
- **Archivos a tocar:**
  - `core/src/main/java/io/github/some_example_name/screens/GalacticaScreen.java`.
  - Todos los recursos y documentación de Galactica.
- **Ejercicio práctico:**
  - Implementar `resetMinigameRound()` y `disposeMinigame()`. Verificar con múltiples rondas seguidas que no haya fugas de memoria ni desincronización de oleadas.
- **Criterio de aprobación:**
  - El juego juega 3 rondas seguidas sin tirones ni anomalías. Al presionar `ESC` vuelve al menú y al cerrar la aplicación no quedan procesos colgados ni warnings en consola.
- **Preguntas de defensa oral:**
  1. *Demostrá en el código dónde y cómo se liberan las texturas de Galactica al cerrar el minijuego.*
  2. *¿Cómo se garantiza que al jugar la ronda 2, el `StageSequencer` no empiece en el segundo 30 sino en el segundo 0?*

---

# BLOQUE D — Simulacro de Examen Oral Final

## Misión 15 — Defensa Técnica Integral de RAMSHI
- **Objetivo:** Superar un simulacro de examen oral exhaustivo de 15 a 20 preguntas sin mirar el código, defendiendo la arquitectura general, el minijuego Snake y Galactica.
- **Dinámica:**
  - Preguntas formuladas de a una en formato tribunal de evaluación.
  - Evaluación rigurosa: precisión conceptual, vocabulario técnico (sin chamuyo), justificación de decisiones y patrones.
  - Entrega de puntaje numérico final por bloque (A, B, C) y Cheat Sheet de conceptos clave.
- **Criterio de aprobación:**
  - Mínimo 85% de respuestas correctas y sólidas en el primer intento.
