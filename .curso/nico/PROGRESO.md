# PROGRESO — Nico

## Estado actual
- **Misión en curso:** ninguna (Misión 11 completada).
- **Próxima misión:** Misión 12 — El camino: fondo y 3 carriles.
- **Rama activa:** `practica/nico`.
- **Minijuego propio:** Race (se desarrolla en el Bloque C, rama `feature/Race`).

## Tarea pendiente
Arrancar la Misión 12 (fondo y 3 carriles) en `feature/Race`.

## Misiones completadas
| # | Misión | Conceptos dominados | Fecha |
|---|--------|---------------------|-------|
| 1 | Cómo arranca RAMSHI | `main`/entrypoint, Gradle multi-módulo (`:core` vs `:lwjgl3`), classpath de `assets/`, game loop (`create`/`render`/`dispose`), Inversión de Control, `setScreen` | 2026-10-19 |
| 2 | Screens y navegación | Interfaz `Screen`, polimorfismo por interfaz, ciclo de vida `show/render/hide/dispose`, `unproject`, `Rectangle` clickeable, navegación con `setScreen(game)` | 2026-10-19 |
| 3 | Template Method / `BaseMinigameScreen` | Clase `abstract`, herencia, método abstracto (sin cuerpo + obligatorio), Template Method, `protected`, despacho dinámico, firma de método | 2026-10-19 |
| 4 | Refactor de `SnakeScreen` | `extends`, `@Override`, `super`, Template Method aplicado, hooks (`getP1StatusText`/`getP2StatusText`/`getScoreSummary`), `dispose` simétrico (sin doble liberación), refactor sin cambiar comportamiento | 2026-10-23 |
| 5 | `Direction` y `SnakeSegment` | `enum` con `dx/dy`, `isOpposite` (giro de 180°), `SnakeSegment` con `HEAD/BODY/TAIL` (enum anidado), agregar constantes sin uso, fallo en runtime por textura `null` | 2026-10-23 |

## Diagnóstico inicial (FASE 2)
Resultado: base conceptual sólida (clase/objeto, variable/método, if/for, commit/rama).
IDE preferido: IntelliJ. No requiere misiones introductorias de Java: el foco va a LibGDX y al código de RAMSHI.

## Conceptos a reforzar
- **Método vs función:** en Java se dice "método" (función dentro de una clase).
- **Variable:** no siempre cambia (las `final` son constantes). Es un espacio con nombre y tipo.
- **Objeto:** una `Snake` guarda una *lista* de segmentos, no "las partes" sueltas.
- **`dispose()`:** libera memoria de GPU (texturas, batch, fonts); el garbage collector no lo hace.
- **Inversión de Control:** en un framework no llamás vos al motor; el motor te llama. `create` 1 vez, `render` por frame, quien lo decide es el backend (`Lwjgl3Application`), no tu código.
- **POO en frameworks:** no hay un "main que llama a todo"; cada clase conoce solo a la siguiente y se pasan el control.
- **Gradle en modo quiet:** build sin salida = BUILD SUCCESSFUL.
- **`Game` vs `Screen`:** `Game` NO es un `Screen`; `Game` coordina un `Screen`. Las pantallas `implements Screen`.
- **`unproject`:** convierte coordenadas reales de la ventana HACIA coordenadas virtuales del juego (nunca al revés).
- **Abstracto vs concreto:** abstracto = sin cuerpo + obligatorio; concreto = con cuerpo.
- **Llamada a un abstracto:** compila porque está declarado; el cuerpo se elige en runtime según la subclase real (despacho dinámico).
- **`camera.position`:** es el punto del mundo que queda al centro de la pantalla; el tercer parámetro es Z (0 en 2D).

## Reglas de Git del curso
- Bloques A y B: `practica/nico`, NUNCA merge a `main`.
- Bloque C: `feature/Race` desde `main` actualizado; se trae `.curso/nico` (y `BaseMinigameScreen`) desde `practica/nico`; PR a `main` al final.
- Un commit por misión: `course(nico): complete mission N - <topic>`.
| 11 | Esqueleto de `RaceScreen` + acceso al menú | Herencia de `BaseMinigameScreen`, hooks mínimos, navegación en menú, Template Method con segundo minijuego | 2026-10-25 |
