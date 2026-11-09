# RAMSHI
Juego 1v1 local (pantalla dividida) de microjuegos de 10 segundos, hecho con Java + libGDX.

## Modos
- **Jugar**: se eligen N rondas, los minijuegos salen en orden aleatorio y al final se muestra el recuento de puntos.
- **Endless**: se elige un minijuego y se juega en bucle contando victorias.

## Agregar un minijuego
1. `screens/MiScreen.java` extendiendo `BaseMinigameScreen` (duración 10 s).
2. Sprites en `assets/mijuego/`.
3. Una línea en `session/MinigameType.java`.

Controles: P1 WASD · P2 Flechas.