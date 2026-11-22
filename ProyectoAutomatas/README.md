# Simulador de la Jerarquía de Chomsky

Aplicación de escritorio en **Java (Swing)** que permite visualizar, simular y validar el comportamiento de los modelos computacionales de los cuatro niveles de la Jerarquía de Chomsky.

| Nivel | Lenguajes | Modelo | Qué muestra la interfaz |
|---|---|---|---|
| 3 | Regulares | Autómata Finito Determinista | Estados, estado actual y transición usada al leer cada símbolo |
| 2 | Libres de contexto | Autómata de Pila | Animación de la pila LIFO (push / pop) mientras se consume la cadena |
| 1 | Sensibles al contexto | Autómata Linealmente Acotado | Cinta acotada con delimitadores `[` `]` y cabezal bidireccional |
| 0 | Recursivamente enumerables | Máquina de Turing | Cinta extensible, alfabeto, cabezal de lectura/escritura y estado activo |

## Requisitos

- **JDK 17 o superior** (probado con JDK 21). No se necesitan librerías externas: solo se usa Swing, que viene en el JDK.

## Instalación y ejecución

```bash
git clone https://github.com/SMugxa/ProyectoAutomatas.git
cd ProyectoAutomatas
```

**Windows:** doble clic en `ejecutar.bat` (o ejecútalo desde `cmd`).

**Linux / macOS:**
```bash
chmod +x ejecutar.sh
./ejecutar.sh
```

**A mano (cualquier sistema):**
```bash
mkdir out
javac -encoding UTF-8 -d out $(find src -name '*.java')   # en Windows PowerShell: javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out chomsky.Main
```

**Pruebas automáticas** (verifican todas las cadenas de ejemplo de los cuatro niveles, sin interfaz):
```bash
javac -encoding UTF-8 -d out $(find src test -name '*.java')
java -cp out chomsky.SelfTest
```

## Manual de usuario

1. Al abrir el programa aparece el **panel principal** con los cuatro módulos. Pulsa **Abrir módulo** en el que quieras usar; **Volver al menú** regresa al panel.
2. En cada módulo:
   - **Lenguaje / máquina**: elige el lenguaje predeterminado.
   - **Cadena**: elige una cadena de prueba de la lista o escribe la tuya (`ε` o vacío = cadena vacía).
   - **Paso**: avanza una sola transición. **Ejecutar / Pausar**: corre la simulación automáticamente (la barra **Velocidad** controla el ritmo). **Reiniciar** / **Cargar cadena**: vuelve al inicio.
   - Abajo se muestra el veredicto (**CADENA ACEPTADA** / **RECHAZADA**) con la razón, y a la derecha el registro paso a paso y la definición formal.
3. **Nivel 3** permite además definir tu propio AFD: elige «Personalizado (editable)» y edita la definición con el formato
   ```
   alfabeto: a,b
   inicial: s0
   finales: s1
   s0,a,s1        # estado,símbolo,estado_destino
   ```

## Cadenas de prueba (también están en la lista desplegable de cada módulo)

### Nivel 3 — Autómata Finito
| Lenguaje | Aceptadas | Rechazadas |
|---|---|---|
| Termina en `ab` | `ab`, `aab`, `bbab`, `abab` | `ε`, `aba`, `ba`, `abb` |
| Número par de ceros | `1100`, `ε`, `111`, `0101` | `0`, `1000` |
| `(ab)*` | `abab`, `ab`, `ε`, `ababab` | `aba`, `ba`, `abba` |
| Binarios múltiplos de 3 | `110`, `1001`, `1111`, `11` | `101`, `100`, `10` |

### Nivel 2 — Autómata de Pila
| Lenguaje | Aceptadas | Rechazadas |
|---|---|---|
| aⁿbⁿ | `aabb`, `ab`, `ε`, `aaabbb` | `aab`, `abb`, `ba`, `abab` |
| w c wᴿ | `abcba`, `c`, `aacaa`, `abbcbba` | `abcab`, `abc`, `ab` |
| Paréntesis `( ) [ ]` | `(())`, `([])`, `()[]()`, `ε` | `(()`, `([)]`, `)(` |
| Palíndromos pares w wᴿ (no determinista) | `abba`, `aa`, `abbbba`, `ε` | `aba`, `abab`, `ab` |

### Nivel 1 — Autómata Linealmente Acotado
| Lenguaje | Aceptadas | Rechazadas |
|---|---|---|
| aⁿbⁿcⁿ | `aabbcc`, `abc`, `ε`, `aaabbbccc` | `aabbc`, `abcc`, `acb`, `ba`, `abcabc` |
| aⁿbⁿcⁿdⁿ | `aabbccdd`, `abcd`, `ε` | `aabbccd`, `abcdd`, `abdc` |

### Nivel 0 — Máquina de Turing
| Máquina | Entrada → resultado |
|---|---|
| Aceptar aⁿbⁿ | `aabb`, `ab`, `ε` aceptadas; `aab`, `abb`, `ba` rechazadas |
| Suma unaria 1ᵐ+1ⁿ | `11+111` → `11111`; `1+1` → `11`; `+11` → `11`; `111+` → `111` |
| Incremento binario | `1011` → `1100`; `111` → `1000` (la cinta crece a la izquierda); `0` → `1` |

## Capturas

| Nivel 3 | Nivel 2 |
|---|---|
| ![Nivel 3](docs/img/nivel3.png) | ![Nivel 2](docs/img/nivel2.png) |
| **Nivel 1** | **Nivel 0** |
| ![Nivel 1](docs/img/nivel1.png) | ![Nivel 0](docs/img/nivel0.png) |

## Estructura del proyecto

```
src/chomsky/
├── Main.java                 Ventana principal (panel con los 4 módulos)
├── common/                   Tema oscuro, plantilla de simulador, motor de cinta y dibujo de cintas
├── nivel3/                   DFA, catálogo de lenguajes, vista del autómata
├── nivel2/                   PDA (con búsqueda de rutas), catálogo, vista de la pila
├── nivel1/                   Catálogo de ALA (aⁿbⁿcⁿ, aⁿbⁿcⁿdⁿ) y panel
└── nivel0/                   Catálogo de máquinas de Turing y panel
test/chomsky/SelfTest.java    Pruebas automáticas de todas las cadenas de ejemplo
docs/                         Informe de implementación y capturas
```

## Documento de implementación

El informe con los conceptos aplicados y la explicación de la implementación está en [`docs/Informe.docx`](docs/Informe.docx).
