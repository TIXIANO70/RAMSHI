---
type: course-progress
title: "Progreso — Curso de Programación y Defensa Técnica RAMSHI"
student: "tiziano"
minigame: "galactica"
created: 2026-11-02
updated: 2026-11-02
---

# 📍 Progreso del Curso: Defensa Técnica RAMSHI

## ⏳ Estado Actual

**Misión en curso:** Misión 1 — Anatomía del Proyecto, Build System y Launcher  
**Tarea pendiente:** Repasar la separación modular `:core` vs `:lwjgl3`, ciclo de vida en `Main` / `Lwjgl3Launcher` y resolver el ejercicio práctico de la ventana.

---

## ✅ Misiones Completadas

| # | Misión | Conceptos dominados | Fecha |
|---|--------|---------------------|:-----:|
| 0 | Fase 0 & Diagnóstico Inicial | Entorno verificado, Git snapshots, POO básica (clase vs objeto, estado vs comportamiento) | 2026-11-02 |

---

## 🧠 Conceptos a Reforzar (Historial de Errores / Ajustes)

- *Game Loop vs Bucles Bloqueantes:* En LibGDX nunca usar `while(true)` ni pausas bloqueantes para la lógica de juego; el motor provee `render(delta)` que cicla a 60 FPS.
- *Clase vs Archivo:* Una clase es el molde conceptual en POO; el archivo físico `.java` es el artefacto que la contiene en disco.

---

## 🗺️ Mapa de Misiones del Plan

### BLOQUE A — Base Lógica y Arquitectura de RAMSHI
- [ ] **Misión 1:** Anatomía del Proyecto, Build System y Launcher *(En curso)*
- [ ] **Misión 2:** Game Loop, Navegación de Pantallas y Ciclo de Vida
- [ ] **Misión 3:** Arquitectura Base de Minijuegos (`BaseMinigameScreen`) y Template Method

### BLOQUE B — Dominio de Snake 1v1
- [ ] **Misión 4:** Estructura de Datos de la Cuadrícula y Spawn de Comida
- [ ] **Misión 5:** Simulación de Movimiento por Ticks y Crecimiento de Serpiente
- [ ] **Misión 6:** Entrada Desacoplada y Buffering Atómico (`InputHandler`)
- [ ] **Misión 7:** Arenas Split-Screen y Pantalla `SnakeScreen`

### BLOQUE C — Tu Minijuego: Galactica 1v1 (Cosmic Waves)
- [ ] **Misión 8:** Esqueleto de `GalacticaScreen` y Acceso desde el Menú Principal
- [ ] **Misión 9:** Arenas Espaciales Split-Screen y Nave del Jugador
- [ ] **Misión 10:** Sistema de Disparo Láser y Optimización con Object Pooling (Zero GC)
- [ ] **Misión 11:** Cronología Determinista de Oleadas (`WaveEvent` y `StageSequencer`)
- [ ] **Misión 12:** Entidades Enemigas, Comportamientos y Colisiones
- [ ] **Misión 13:** Mecánica Competitiva de Sabotaje Cruzado (*Counter-Attack Waves*)
- [ ] **Misión 14:** Pulido Final, Gestión de Recursos GPU (`dispose`) y Pull Request

### BLOQUE D — Simulacro de Examen Oral Final
- [ ] **Misión 15:** Defensa Técnica Integral de RAMSHI (15-20 preguntas de tribunal)
