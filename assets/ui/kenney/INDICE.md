# Índice del pack "Input Prompts Pixel 1-Bit" (Kenney, CC0)

- Cada tile mide **16×16 px**. Están en `white/` (blancos) y `black/` (negros): **el mismo número es el mismo dibujo en los dos colores**.
- Ruta de ejemplo: `ui/kenney/white/tile_0086.png` (tecla W).
- La hoja completa está en `tilemap/` (34 columnas × 24 filas). El número de un tile es `fila * 34 + columna`.
- **Teclas de contorno** (tecla "apagada"): números de abajo. **Teclas rellenas** (tecla "apretada"): **sumar 272**. Ejemplo: W contorno = 86, W rellena = 358.
- Las teclas anchas ocupan 2 o 3 tiles seguidos (por ejemplo ALT = 187 + 188).

## Teclado (contorno; rellena = +272)

| Tecla | Tile | | Tecla | Tile | | Tecla | Tile |
|---|---|---|---|---|---|---|---|
| ESC | 17 | | Q | 85 | | Z | 155 |
| F1 … F12 | 18 … 29 | | W | 86 | | X | 156 |
| ~ | 30 | | E | 87 | | C | 157 |
| ! | 31 | | R | 88 | | V | 158 |
| @ | 32 | | T | 89 | | B | 159 |
| # | 33 | | Y | 90 | | N | 160 |
| 1 … 9 | 51 … 59 | | U | 91 | | M | 161 |
| 0 | 60 | | I | 92 | | < | 162 |
| - | 61 | | O | 93 | | > | 163 |
| + | 62 | | P | 94 | | ? | 164 |
| = | 63 | | [ | 95 | | / | 165 |
| _ | 64 | | ] | 96 | | **Flecha ↑** | **166** |
| Borrar (ancha) | 66 + 67 | | { | 97 | | **Flecha →** | **167** |
| Enter (2 tiles de alto) | 100-101 / 134-135 | | } | 98 | | **Flecha ↓** | **168** |
| Shift (flecha) | 119 | | \ | 99 | | **Flecha ←** | **169** |
| Espacio (chica) | 153 | | A | 120 | | ALT (ancha) | 187 + 188 |
| Windows | 154 | | S | 121 | | TAB (ancha) | 189 + 190 |
| CTRL (ancha) | 221 + 222 | | D | 122 | | DEL (ancha) | 191 + 192 |
| CAPS (ancha) | 223 + 224 | | F | 123 | | END (ancha) | 193 + 194 |
| HOME (ancha) | 225 + 226 | | G | 124 | | NUM (ancha) | 195 + 196 |
| PAGE ↑ (ancha) | 227 + 228 | | H | 125 | | . | 197 |
| PAGE ↓ (ancha) | 229 + 230 | | J | 126 | | $ | 198 |
| Tecla vacía | 233 | | K | 127 | | % | 199 |
| Barra espaciadora | 235 + 236 + 237 | | L | 128 | | ^ | 200 |
| SHIFT (ancha) | 255 + 256 | | ' | 129 | | ( | 202 |
| INSERT (ancha) | 257 + 258 | | " | 130 | | ) | 203 |
| PRINT (ancha) | 259 + 260 | | : | 131 | | Play | 265 |
| SCROLL (ancha) | 261 + 262 | | ; | 132 | | Pausa | 266 |
| PAUSE (ancha) | 263 + 264 | | × | 133 | | Stop | 267 |

## Otros grupos (por rango)

| Qué | Tiles |
|---|---|
| Botones de joystick redondos (A, B, X, Y, vacíos) | 0 – 16, 42 – 50 |
| Cruces direccionales (D-pad) | 34 – 41, 68 – 75, 102 – 109, 136 – 143, 170 – 177, 204 – 211 y siguientes |
| Mouse (clic izquierdo, derecho, rueda) | 76 – 84, 110 – 118, 144 – 152, 178 – 186 |
| Sticks L y R | 212 – 220, 246 – 254, 280 – 288, 314 – 322 |
| Gatillos LT / RT / LB / RB | 551 – 560, 585 – 594 y 619 – 638 |
| Manos (señalar, agarrar) | 578 – 584 |
| Gestos y flechas curvas | 569 – 577, 603 – 611, 640 – 645, 673 – 679 |
| Botones de consola (menú, home, +, −, 1, 2) | 680 – 747 |
