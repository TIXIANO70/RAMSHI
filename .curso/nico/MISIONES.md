# MISIONES — Curso RAMSHI (Nico)

Minijuego propio: **Race** (3 carriles, esquivar, gana quien llega más lejos).
Rama de práctica: `practica/nico`. Rama del minijuego: `feature/Race`.

Reglas fijas:
- Una misión por vez. No se avanza sin confirmación.
- Aprobación = build OK (`./gradlew lwjgl3:build`) + comportamiento observado en `./gradlew lwjgl3:run` + 2-3 preguntas de defensa oral.
- Base lógica (Bloques A y B): se toca código real solo para *reimplementar métodos vaciados*; nunca se mergea a `main`.
- Bloque C: se implementa Race de verdad en `feature/Race` y termina en Pull Request.

> NOTA IMPORTANTE: `BaseMinigameScreen` NO existe en el repo (verificado). El equipo nunca la creó. Decisión tomada: **la creamos nosotros** en el Bloque A, y en el Bloque C la llevamos a `feature/Race` con `git checkout practica/nico -- core/src/main/java/.../BaseMinigameScreen.java` (mismo truco que se usa para traer la carpeta `.curso`).

---

## BLOQUE A — Base lógica de RAMSHI

### ✅ Misión 1 — Cómo arranca RAMSHI (Gradle + Launcher + Main)
- Objetivo: explicar de punta a punta qué pasa desde que ejecutás el juego hasta que ves el menú.
- Conceptos nuevos: proyecto Gradle multi-módulo (`:core` vs `:lwjgl3`), classpath y carpeta `assets/`, `main()` como punto de entrada, `Game` loop (`create/render/dispose`), Vsync/FPS.
- Archivos a leer: `settings.gradle`, `build.gradle`, `lwjgl3/build.gradle`, `lwjgl3/src/main/java/io/github/some_example_name/lwjgl3/Lwjgl3Launcher.java`, `core/src/main/java/io/github/some_example_name/Main.java`.
- Preguntas de comprensión: ¿por qué hay dos módulos? ¿qué hace `setScreen`? ¿quién llama a `create()`? ¿por qué `assets/` es la raíz de los `new Texture("...")`?
- Ejercicio práctico: cambiar título y tamaño de ventana en `Lwjgl3Launcher` y explicar la diferencia entre tamaño real y virtual (`FitViewport`).
- Criterio de aprobación: `./gradlew lwjgl3:build` OK; al correr se ve el menú con el nuevo título.
- Preguntas de defensa oral: ¿qué diferencia hay entre `:core` y `:lwjgl3`? ¿qué pasaría si borrás `Main.create()`?

### ✅ Misión 2 — Screens y navegación (MainMenuScreen)
- Objetivo: explicar cómo una pantalla se dibuja, captura clics y navega a otra.
- Conceptos nuevos: interfaz `Screen`, polimorfismo, `Game.setScreen`, ciclo de vida `show/render/hide/dispose`, `OrthographicCamera` + `FitViewport` + `unproject`, `Rectangle` como zona clickeable.
- Archivos a leer: `core/.../screens/MainMenuScreen.java`.
- Preguntas de comprensión: ¿por qué `render` corre ~60 veces/s y `show` una sola vez? ¿por qué hay que `unproject`? ¿por qué `dispose()` es obligatorio?
- Ejercicio práctico: cambiar textos/colores y agregar el texto de un tercer botón (sin funcionalidad) respetando el layout.
- Criterio de aprobación: build OK; el menú muestra los cambios.
- Preguntas de defensa oral: ¿qué hace `game.setScreen(new SnakeScreen(game))`? ¿por qué el menú llama `dispose()` justo después de cambiar de pantalla?

### ✅ Misión 3 — Template Method: diseñar `BaseMinigameScreen`
- Objetivo: entender el patrón y diseñar la clase base que compartirán todos los minijuegos.
- Conceptos nuevos: clase `abstract`, herencia, métodos abstractos vs hooks, `protected`, Template Method (la base llama a métodos que la subclase define).
- Archivos a leer: `core/.../screens/SnakeScreen.java` (para extraer el contrato: temporizador, HUD, overlay, victorias globales).
- Preguntas de comprensión: ¿qué debe hacer la base y qué debe dejar a la subclase? ¿por qué el temporizador y las victorias globales van en la base?
- Ejercicio práctico: crear `core/src/main/java/io/github/some_example_name/screens/BaseMinigameScreen.java` con el esqueleto (con el andamiaje guiado, no copy-paste).
- Criterio de aprobación: `./gradlew core:compileJava` OK.
- Preguntas de defensa oral: ¿qué es un método abstracto? ¿por qué SnakeScreen podría compartir esta base sin duplicar el HUD?

### ✅ Misión 4 — Refactor: `SnakeScreen` como subclase de `BaseMinigameScreen`
- Objetivo: aplicar el patrón y dejar Snake funcionando mejor.
- Conceptos nuevos: `extends`, `@Override`, `super`, refactor controlado (mismo comportamiento, menos duplicación).
- Archivos a leer: `BaseMinigameScreen.java`, `SnakeScreen.java`.
- Preguntas de comprensión: ¿quién maneja ahora el timer y las victorias? ¿cómo sabe la base cuánto dura la ronda?
- Ejercicio práctico: convertir `SnakeScreen` en subclase implementando los hooks; borrar la lógica duplicada.
- Criterio de aprobación: build OK; jugar Snake igual que antes (rondas de 30s, overlay, victorias).
- Preguntas de defensa oral: ¿qué ganás con el refactor? ¿qué hook define la duración de la ronda?

---

## BLOQUE B — Snake

### ✅ Misión 5 — `Direction` y `SnakeSegment`
- Objetivo: entender la grilla y la representación de cada parte de la serpiente.
- Conceptos nuevos: `enum`, campos y constructores, `EnumMap`, inmutabilidad.
- Archivos a leer: `core/.../entities/Direction.java`, `core/.../entities/SnakeSegment.java`.
- Preguntas de comprensión: ¿qué guarda `dx/dy`? ¿para qué sirve `isOpposite`? ¿por qué `TYPE` es un `enum`?
- Ejercicio práctico: extender `Direction` con una dirección diagonal (solo lógica, sin usarla) y explicar el impacto en `isOpposite`.
- Criterio de aprobación: build OK.
- Preguntas de defensa oral: ¿por qué un `enum` y no `int`? ¿cómo afecta una dirección nueva a la orientación visual?

### Misión 6 — `Snake`: movimiento por ticks, crecimiento y colisiones
- Objetivo: explicar el corazón de Snake.
- Conceptos nuevos: `List`/`ArrayList`, `step()` como paso discreto, cola de crecimiento (`growPending`), colisión consigo mismo y con bordes.
- Archivos a leer: `core/.../entities/Snake.java`.
- Preguntas de comprensión: ¿por qué `grow()` solo incrementa y no crece? ¿por qué se remueve `segments.get(0)`? ¿cómo se orienta la cola?
- Ejercicio práctico: modificar la longitud inicial de la serpiente en `reset()` y explicar el patrón `startX - n*dx`.
- Criterio de aprobación: build OK; se ve la serpiente más larga al arrancar.
- Preguntas de defensa oral: ¿qué pasa si comentás el `remove(0)`? ¿por qué la cabeza es el último elemento?

### Misión 7 — `Food`: spawn aleatorio
- Objetivo: explicar la comida y su reaparición sin superposición.
- Conceptos nuevos: `MathUtils.random`, bucle con condición y límite de intentos, `Texture` + `Disposable`.
- Archivos a leer: `core/.../entities/Food.java`, `core/.../entities/Snake.occupies()`.
- Preguntas de comprensión: ¿por qué el `do/while` tiene `attempts < 100`? ¿qué recibe `respawn` sobrecargado?
- Ejercicio práctico: agregar un tipo de comida especial que valga 3 puntos (extensión pequeña, guiada).
- Criterio de aprobación: build OK; al comer la especial suma 3.
- Preguntas de defensa oral: ¿qué hace `occupies`? ¿por qué no puede la comida caer sobre la serpiente?

### Misión 8 — `SnakeArena`: split-screen y `tick()`
- Objetivo: explicar cómo dos tableros independientes simulan y se dibujan.
- Conceptos nuevos: composición (arena *tiene* una serpiente y una comida), origen en píxeles vs coordenadas de grilla, doble `for` de dibujado.
- Archivos a leer: `core/.../entities/SnakeArena.java`, `core/.../screens/SnakeScreen.java` (creación de arenas).
- Preguntas de comprensión: ¿qué hace `tick()` en orden? ¿por qué P1 y P2 usan el mismo `Snake` pero distinto `playerNumber`? ¿qué diferencia hay entre `originPixelX` y `gridX`?
- Ejercicio práctico: modificar la posición/origen de una arena en `SnakeScreen` y observar el corrimiento.
- Criterio de aprobación: build OK; el tablero se mueve en pantalla.
- Preguntas de defensa oral: ¿qué es composición? ¿por qué `render` recibe `originPixelX/Y`?

### Misión 9 — `InputHandler`: entrada P1/P2
- Objetivo: explicar cómo se captura y filtra la entrada de dos jugadores.
- Conceptos nuevos: `InputAdapter`/`keyDown`, buffer de eventos vs polling, por qué se evita el giro de 180°.
- Archivos a leer: `core/.../utils/InputHandler.java`, `core/.../screens/SnakeScreen.java` (uso en `render`).
- Preguntas de comprensión: ¿por qué se guarda `p1BufferedDirection` y luego se pone en `null`? ¿para qué el polling de respaldo?
- Ejercicio práctico: agregar una tecla alternativa (ej. `NUMPAD`/otra) para P1 y explicar el orden de prioridad.
- Criterio de aprobación: build OK; la tecla nueva funciona.
- Preguntas de defensa oral: ¿qué diferencia hay entre `isKeyJustPressed` e `isKeyPressed`? ¿por qué filtrar direcciones opuestas?

### Misión 10 — `SnakeScreen`: HUD, rondas y victorias
- Objetivo: repasar cómo una pantalla concreta usa la base.
- Conceptos nuevos: `GlyphLayout` para centrar texto, estado de ronda (`roundOver`, `timeRemaining`), overlay con `ShapeRenderer`.
- Archivos a leer: `core/.../screens/SnakeScreen.java`.
- Preguntas de comprensión: ¿cómo se decide el ganador de la ronda? ¿cómo suma victorias globales? ¿qué dibuja el overlay?
- Ejercicio práctico: cambiar la duración de la ronda y el umbral de alerta roja (últimos 5s).
- Criterio de aprobación: build OK; el cronómetro refleja el cambio.
- Preguntas de defensa oral: ¿por qué `finishRound` no reinicia las arenas? ¿quién llama `startNewRound`?

---

## BLOQUE C — Race (en `feature/Race`)

### ✅ Misión 11 — Esqueleto de `RaceScreen` + acceso desde el menú
- Objetivo: crear la subclase de `BaseMinigameScreen` y poder entrar desde el menú.
- Conceptos nuevos: trabajar en rama de feature, integrar cambios de `main`, hooks de la base.
- Archivos a leer/tocar: `BaseMinigameScreen.java`, `MainMenuScreen.java`, nuevo `RaceScreen.java`.
- Ejercicio práctico: `RaceScreen` con hooks mínimos (duración 30s; update/draw vacíos que dibujen un placeholder) y botón en el menú.
- Criterio de aprobación: build OK; se entra a Race y con ESC vuelve al menú; overlay de fin de ronda funciona.
- Preguntas de defensa oral: ¿qué hooks implementás y por qué? ¿cómo se conecta el menú?

### Misión 12 — El camino: fondo y 3 carriles
- Objetivo: dibujar la pista con 3 carriles por jugador.
- Conceptos nuevos: `pgista.png` (128×128) como carril repetido 3× horizontal y tileado vertical, filtro `Nearest` para pixel art.
- Archivos a leer/tocar: `RaceScreen.java`, `assets/race/pista.png`.
- Preguntas de comprensión: ¿por qué 128×3 = 384 encaja en media pantalla? ¿por qué `Nearest` y no `Linear`?
- Ejercicio práctico: render del fondo con scroll vertical.
- Criterio de aprobación: build OK; se ven 3 carriles por jugador.
- Preguntas de defensa oral: ¿cómo se evita el difuminado del pixel art? ¿cómo calculás la x de cada carril?

### Misión 13 — Los autos y el cambio de carril
- Objetivo: dibujar los autos y moverlos entre carriles con los controles definidos.
- Conceptos nuevos: estado de carril por jugador, índice de carril, clamp a [0,2], controles P1 `A/D` y P2 `←/→`.
- Archivos a leer/tocar: `RaceScreen.java`, `assets/race/car1.png`, `car2.png`.
- Preguntas de comprensión: ¿por qué modelar el carril con un índice y no con píxeles? ¿qué pasa si el carril se sale de rango?
- Ejercicio práctico: movimiento lateral con límites para ambos jugadores.
- Criterio de aprobación: build OK; cada auto cambia de carril y no se sale.
- Preguntas de defensa oral: ¿por qué clamp y no `if` sueltos? ¿dónde se registra la entrada?

### Misión 14 — Velocidad, aceleración y distancia
- Objetivo: física del avance y marcador en metros.
- Conceptos nuevos: integración simple (`v += a*dt`, `d += v*dt`), tope de velocidad (100 m/s), `delta` como tiempo.
- Archivos a leer/tocar: `RaceScreen.java`.
- Preguntas de comprensión: ¿por qué se multiplica por `delta`? ¿por qué hace falta el tope de 100?
- Ejercicio práctico: velocidad que sube a 5 m/s² hasta 100 m/s; distancia acumulada; HUD con metros.
- Criterio de aprobación: build OK; el HUD incrementa metros y la velocidad tope se respeta.
- Preguntas de defensa oral: ¿de qué depende `delta`? ¿por qué sin `delta` el juego iría distinto en cada PC?

### Misión 15 — Obstáculos aleatorios idénticos para ambos
- Objetivo: generar obstáculos con el mismo patrón para P1 y P2.
- Conceptos nuevos: semilla / lista compartida de obstáculos, spawn por carril aleatorio, velocidad relativa de acercamiento.
- Archivos a leer/tocar: `RaceScreen.java`, `assets/race/` (sprite de obstáculo).
- Preguntas de comprensión: ¿por qué deben ser idénticos? ¿cómo garantizás el mismo patrón en dos simulaciones separadas?
- Ejercicio práctico: obstáculos que se acercan y reaparecen.
- Criterio de aprobación: build OK; ambos jugadores ven los mismos obstáculos.
- Preguntas de defensa oral: ¿qué es una semilla? ¿por qué no conviene `Math.random()` (no determinista) acá?

### Misión 16 — Colisión: frenar a 0
- Objetivo: detectar choque y aplicar la penalización.
- Conceptos nuevos: detección por comparación de carril/posición, reinicio de velocidad.
- Archivos a leer/tocar: `RaceScreen.java`.
- Preguntas de comprensión: ¿por qué la aceleración sigue siendo 5 m/s²? ¿qué se reinicia y qué no?
- Ejercicio práctico: al chocar, velocidad = 0 pero el auto sigue (no muere).
- Criterio de aprobación: build OK; al chocar la distancia deja de sumar por un momento y luego retoma.
- Preguntas de defensa oral: ¿qué condición exacta es el choque? ¿por qué no se reinicia la distancia?

### Misión 17 — Fin de ronda, ganador y empate
- Objetivo: cerrar la ronda a los 30s y decidir ganador por distancia.
- Conceptos nuevos: comparación de distancia, empate por igualdad, integración con el overlay de la base.
- Archivos a leer/tocar: `RaceScreen.java`, `BaseMinigameScreen.java`.
- Preguntas de comprensión: ¿cómo se calcula el ganador? ¿cuándo hay empate?
- Ejercicio práctico: `getWinner`/hook de resultado según distancia.
- Criterio de aprobación: build OK; a los 30s gana quien más metros hizo; empate si iguales.
- Preguntas de defensa oral: ¿dónde vive la lógica de ganador, base o subclase? ¿por qué?

### Misión 18 — Pulido, `dispose` y Pull Request
- Objetivo: dejar Race listo y abrir el PR.
- Conceptos nuevos: liberar texturas, higiene de recursos, Pull Request en GitHub, revisión.
- Archivos a leer/tocar: `RaceScreen.java`, `MainMenuScreen.java`.
- Preguntas de comprensión: ¿qué recursos creaste y dónde los liberás? ¿qué se revisa en un PR?
- Ejercicio práctico: `dispose()` completo de Race + créditos/controles en HUD.
- Criterio de aprobación: build OK; ciclo entrar/salir de Race sin fugas; PR abierto a `main`.
- Preguntas de defensa oral: ¿por qué `dispose` es simétrico al constructor? ¿qué evita una fuga de GPU?

### Misión 18-B (futura) — Rotación de minijuegos (5s + próximo juego)
- Objetivo: al terminar una ronda, mostrar el resultado, una cuenta regresiva de 5s y pasar automáticamente al próximo minijuego, indicando cuál viene.
- Nota: feature nueva (no existe hoy en el repo); requiere una lista/orden de minijuegos y un coordinador de flujo. Se hará cuando haya al menos Snake y Race jugables.
- Criterio de aprobación: build OK; al terminar la ronda, overlay con ganador + cuenta regresiva + nombre del próximo juego y transición automática.

---

## BLOQUE D — Simulacro de examen oral final

### Misión 19 — Simulacro oral (15-20 preguntas)
- Objetivo: defender TODO el repo (base, Snake y Race) sin mirar código.
- Conceptos nuevos: ninguno; consolidación.
- Archivos a leer: todos.
- Ejercicio práctico: ninguna edición; solo preguntas y feedback.
- Criterio de aprobación: 15+ respuestas correctas con nota por bloque.
- Preguntas de defensa oral: el simulacro completo.
