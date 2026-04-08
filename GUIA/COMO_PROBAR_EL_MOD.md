# Guía para probar el Combat Plugin

Esta guía es para alguien que nunca ha visto este mod y quiere probarlo desde cero.

---

## ¿Qué hace este mod?

Añade un **sistema de clases y progresión** al servidor de Hytale. Cuando entras al servidor:

- Puedes elegir una **clase** (arquetipo de combate) mediante una **interfaz gráfica**
- Subes de **nivel** ganando XP
- Con cada nivel ganas **puntos de talento**
- Puedes gestionar tus **habilidades pasivas** mediante un **árbol de talentos interactivo**

---

## Paso 1 — Arrancar el servidor

En la carpeta del proyecto, abre una terminal y ejecuta:

```
./gradlew runServer
```

Espera a que aparezca algo como `Server started` en los logs. Luego abre Hytale y conéctate al servidor local.

> Usa un mundo existente (tipo "Exploration") al conectarte. El mundo "Creative Hub" puede provocar un error de Hytale no relacionado con el plugin.

---

## Paso 2 — Elegir una clase (interfaz gráfica)

Una vez dentro del servidor, escribe:

```
/class choose
```

Se abrirá una **interfaz gráfica** con las cuatro clases disponibles. Haz clic en el botón de la clase que quieras:

| Clase | Rol principal |
|---|---|
| Sword Master | Tanque / Bruiser melee |
| Elementalist | Mago / Curandero |
| Technocrat | Soporte / Gadgets |
| Necromancer | Invocador de no-muertos |

> Solo puedes elegir clase una vez. Si quieres cambiarla, usa `/class reset` primero.

---

## Paso 3 — Ver tu estado actual

Para ver tu clase, nivel, XP y puntos de talento disponibles:

```
/level get
```

También puedes ver el detalle de tu clase con:

```
/class info
```

---

## Paso 4 — Ganar experiencia (para testing, como admin)

En condiciones normales, la XP se ganaría jugando. Para probar el sistema rápidamente:

```
/xp add <cantidad>
```

**Ejemplos:**

```
/xp add 100    → sube exactamente 1 nivel (nivel 1 necesita 100 XP)
/xp add 500    → sube varios niveles de golpe
/xp add 9999   → llega al nivel máximo (50)
```

> Cada nivel que subes te da **1 punto de talento**.

---

## Paso 5 — Ver y gestionar talentos (árbol de talentos)

Para abrir el árbol de talentos interactivo:

```
/talents
```

Se abrirá una **interfaz gráfica** con los 16 nodos del árbol de talentos de tu clase.

- **Clic izquierdo** en un nodo → desbloquea / sube de rango
- **Clic derecho** en un nodo → baja de rango / elimina

Los nodos bloqueados (nivel insuficiente o prerequisito no cumplido) aparecen desactivados.

---

## Paso 6 — Comandos opcionales para talentos

Si prefieres usar comandos directamente (por ejemplo, desde consola de admin):

```
/talents --unlock <id_del_talento>    → desbloquea un talento por su ID
/talents --reset                       → devuelve todos los puntos de talento
```

**Ejemplos de IDs de talento:**

### Sword Master
```
sm_iron_will        → +20 vida máxima
sm_shield_mastery   → +15% reducción de daño al bloquear
sm_battle_hardened  → -10% daño recibido
sm_sword_expertise  → +12% daño melee
sm_counterattack    → (requiere battle_hardened) Devuelve el 50% del daño recibido
sm_perfect_block    → (requiere shield_mastery) Cancela un golpe cada 10s al bloquear
```

### Elementalist
```
el_arcane_surge       → +20% daño con hechizos
el_mana_font          → +25 mana máximo
el_efficient_casting  → -20% coste de mana en hechizos
el_lifebind           → +30% curación recibida
el_elemental_mastery  → (requiere arcane_surge) +15% daño adicional multiplicativo
el_mana_regen         → Regeneración de mana pasiva
```

### Technocrat
```
tc_overdrive          → +25% efectividad de consumibles
tc_gadget_cache       → +2 cargas máximas de gadgets
tc_quick_hands        → +10% velocidad al cambiar slot
tc_overclock          → (requiere quick_hands) +15% daño al cambiar slot
tc_field_medic        → +20% curación dada y recibida
tc_cooldown_hacker    → -20% enfriamiento de gadgets
```

### Necromancer
```
nc_death_surge      → Al matar: invoca un esqueleto
nc_undying_army     → +8% daño por cada invocación activa
nc_soul_harvest     → Al matar: recupera 15 de mana
nc_lich_form        → +30 vida máxima
nc_army_of_darkness → (requiere death_surge) +1 invocación máxima
nc_death_pact       → (requiere lich_form) +20% daño con menos del 30% de vida
```

---

## Paso 7 — Comandos de admin para testing

### Fijar un nivel concreto
```
/level set <número>
/level set 10    → Pone directamente el nivel 10
/level set 50    → Nivel máximo
```

### Añadir XP específica
```
/xp add <cantidad>
```

---

## Paso 8 — Resetear y empezar de nuevo

### Resetear solo los talentos (mantener clase y nivel)
```
/talents --reset
```
Devuelve todos tus puntos de talento y elimina los talentos desbloqueados.

### Resetear la clase completa (vuelve a cero)
```
/class reset
```
Borra tu clase, nivel, XP y talentos. Puedes elegir una clase nueva con `/class choose`.

---

## Flujo completo de prueba rápida

```
/class choose              → abre interfaz gráfica de clases → elige Sword Master
/xp add 500
/level get
/talents                   → abre árbol de talentos → haz clic en los nodos
/talents --reset
/class reset
/class choose              → elige Necromancer
/level set 20
/talents                   → árbol de la nueva clase
```

---

## Referencia rápida de comandos

| Comando | Qué hace |
|---|---|
| `/class choose` | Abre la interfaz de selección de clase |
| `/class info` | Muestra información de tu clase actual |
| `/class reset` | Borra tu progresión completa |
| `/talents` | Abre el árbol de talentos interactivo |
| `/talents --unlock <id>` | Desbloquea un talento directamente (admin) |
| `/talents --reset` | Devuelve todos los puntos de talento |
| `/level get` | Muestra clase, nivel, XP y puntos |
| `/level set <n>` | Fija tu nivel (admin) |
| `/xp add <cantidad>` | Añade XP directamente (admin) |

---

## Clases de los IDs de talento

| Prefijo | Clase |
|---|---|
| `sm_` | Sword Master |
| `el_` | Elementalist |
| `tc_` | Technocrat |
| `nc_` | Necromancer |
