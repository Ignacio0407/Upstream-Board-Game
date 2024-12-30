# Documento de análisis de requisitos del sistema

## Introducción

Upstream es un juego de mesa con una temática basada en el deshielo en el paso del frío invierno a primavera. Este deshielo abre el camino de vuelta a casa para los salmones, banco que cada jugador debe controlar con el objetivo de remontar el río y así dejar sus huevos en el lugar en el que nacieron, tras una larga vida en alta mar. Por el camino, deben evitar obstáculos diversos, además de depredadores como osos, garzas o águilas.

En una partida de Upstream pueden jugar de 2 a 5 jugadores, esta suele terminar o bien cuando todos los salmones perecen o todos llegan a la zona de desove.

Elementos del juego:
    
    · Ficha de salmón (4 para cada jugador): por un lado muestra una pareja de salmones, y por el otro un único salmón.
    · Loseta de agua (7): loseta libre, sin normas especiales. 
    · Losetas de mar y desove.
    · Loseta de salto de agua (4): los obstáculos obligan a los salmones a saltar en lugar de nadar.
    · Loseta de águila (5): si una ficha de salmón nada aquí, se pierde un salmón y se voltea la loseta.
    · Loseta de oso (3): si una ficha de salmon salta hacia o desde aquí, se pierde un salmón.
    · Loseta de garza (5): si al final de tu turno se tiene una ficha aquí, se pierde un salmón.
    · Loseta de roca (5): tiene la capacidad de cualquier loseta - 1. (la capacidad de cualquier loseta es igual al número de jugadores)

Preparación inicial:

Coloca las losetas de Mar en la mesa con una ficha de salmón de cada jugador en cada una de ellas por la cara que muestra la pareja de salmones. Mezcla las losetas restantes y déjalas en una pila boca abajo.

Empezando por el último jugador que comió salmón y en sentido de las agujas del reloj, cada jugador coge una loseta de la pila y la coloca a continuación del río, en cualquiera de los 3 espacios inmediatamente superiores a las losetas de Mar, hasta que haya exactamente 4 losetas en cada columna (además de las losetas de Mar). Se pueden colocar las losetas en cualquier espacio respetando el ancho de 3 y sin superar las 4 losetas por columna, orientadas como el jugador elija. Una vez colocadas las losetas, el jugador al que le tocase colocar loseta es el jugador inicial.

Estructura del juego:

Empezando por el jugador inicial, los jugadores toman turnos en sentido horario para mover sus fichas de Salmón. Disponen de 5 puntos de movimientos que deben gastar obligatoriamente. Una vez que todos los jugadores hayan realizado su turno, se habrá completado una ronda. Al final de cada ronda, el jugador inicial retira las 3 últimas losetas de río y coloca 3 más de la pila en la parte superior del río, formando una nueva línea, y el inicial pasa a ser el siguiente jugador. Se puede elegir la orientación de las losetas si es relevante (si tienen obstáculos). Si se retira una loseta donde hay una o más fichas de Salmón, devuélvelas a la caja.

¡IMPORTANTE! Durante la primera ronda de juego, NO se retiran losetas, sólo se colocan 3 nuevas losetas. Al final de la segunda ronda, retira las 4 losetas de Mar, y coloca 3 nuevas losetas. Tras esto, cada ronda se irán retirando y colocando losetas de 3 en 3 hasta agotar la pila de losetas.

Movimiento:

Cada jugador cuenta con 5 puntos de movimiento durante su turno que debe gastar en mover sus fichas de salmón. Puede invertir los 5 puntos en una sola ficha o dividirlos como desee, siempre y cuando gaste los 5 puntos. Las parejas se mueven juntas, por lo que cuesta igual mover un salmón sólo que una pareja, ambos se consideran una ficha. Nunca se puede mover hacia atrás. El movimiento puede ser nadando o saltando.

        · Nadar: Moverse de una loseta a otra sin obstáculos entre medio se considera nadar. Nadar cuesta 1 punto por loseta a la que se desplace la ficha. Si una loseta está llena (capacidad = tantas fichas como jugadores), las fichas de salmón no pueden cruzarla nadando ni pararse en ella.

        · Saltar: Moverse de una loseta a otra ignorando todos los obstáculos se considera saltar. Cuesta 1 punto de movimiento, más 1 punto por cada loseta saltada. Sólo se puede saltar en línea recta (no se puede cambiar de dirección durante el salto).

Final de la partida:

Cuando se vayan a colocar las últimas dos losetas de río, éstas deben colocarse a los lados, para poner las de Desove en el centro, al final del río. Cuando una ficha llegue a la primera loseta de Desove debe detenerse y ya no podrán gastarse puntos de movimiento con esa ficha. Al final de cada ronda se sigue eliminando la fila inferior del río, pero no se colocan nuevas losetas. En lugar de eso se mueven todas las fichas en el lago del Desove a la casilla siguiente. IMPORTANTE: En estas casillas no hay máximo de fichas por casilla.

Puntuaciones:

Cuando todos los salmones estén en las losetas de Desove o en la caja, se acaba la partida automáticamente y cada jugador recibe sus puntos:

    • Por cada salmón (que NO ficha de salmón) que tenga, 1 punto.
    • Por cada ficha de salmón que tenga, 1 punto por cada huevo que haya en la casilla de Desove donde se encuentra.

El jugador con mayor puntuación será el ganador. En caso de empate ganará el que tenga más salmones. Si persiste el empate ganará quien tenga más fichas o en su defecto, quien tenga sus fichas más adelantadas.

## Tipos de Usuarios / Roles

Jugador : Una vez registrados y logueados, los jugadores podrán unirse, crear, jugar y observar partidas dentro del juego. Además, podrán listar tanto sus logros como los globales (los de todos los jugadores).

Administradores: Los administradores, tras registrarse y loguearse, podrán realizar las mismas acciones que los jugadores además de tener la capacidad de crear logros, listar jugadores, listar las partidas jugadas y visualizar los desarrolladores del proyecto. 

## Historias de Usuario -> Visual, lo que puede hacer el usuario.

 ### HU-(ISSUE#65): Iniciar sesión ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/65])
Como usuario quiero que el sistema me permita iniciar sesión para que guarde mis datos y me permita poder jugar partidas. (HU implementada con el proyecto base).

![](/docs/mockups/Mockup%20Inicio%20de%20Sesion.png)

 ### HU-(ISSUE#70): Registrarse ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/70])
Como usuario quiero que el sistema me permita registrarme, guardando mis datos para permitirme iniciar sesión posteriormente. (HU implementada con el proyecto base).

![](/docs/mockups/Mockup%20Registrarse.png)

 ### HU-(ISSUE#62): Jugar partidas ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/62])
Como jugador quiero que el sistema me permita jugar partidas, bien sea creándolas o uniéndome a ellas.
![](/docs/mockups/Mockup%20Pantalla%20Inicial.png)

 ### HU-(ISSUE#66): Listar logros ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/66])
Como jugador quiero que el sistema me permita listar logros, independientemente de si se han conseguido o no, ya sean personales (obtenidos por mí) o globales.
![](/docs/mockups//Mockup%20Listar%20Logros.png)

 ### HU-(ISSUE#37): Listar y crear logros ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/37])
Como administrador quiero que el sistema me permita listar logros, independientemente de si se han conseguido o no, ya sean personales (obtenidos por mí) o globales, y crear logros, añadiendo los valores que quiera a cada campo, estando así 
disponible para todos los jugadores y administradores.
![](/docs/mockups/Mockup%20Admin%20Logros.png)

 ### HU-(ISSUE#37): Modificar y eliminar logros ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/37])
Como administrador quiero que el sistema me permita modificar logros, ajustando cada parámetro del logro a mi gusto, y 
eliminar logros, borrándolos de la base de datos, haciendo así que el logro deje de estar disponible para el resto de 
jugadores y administradores.
![](/docs/mockups/Mockup%20Admin%20Logros.png)

 ### HU-(ISSUE#67): Listar usuarios ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/67])
Como administrador quiero que el sistema me permita listar todos los usuarios del juego registrados además de ver 
sus nombres y sus roles. (HU implementada con el proyecto base).
![](/docs/mockups/Mockup%20Listar%20Usuarios.png)

 ### HU-(ISSUE#68): Crear un usuario ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/68])
Como administrador quiero que el sistema me permita crear un nuevo usuario, especificando su nombre, contraseña
y rol, eligiendo entre jugador o administrador. (HU implementada con el proyecto base).
![](/docs/mockups/Mockup%20Crear%20Usuarios.png)

 ### HU-(ISSUE#68): Modificar y eliminar un usuario ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/68])
Como administrador quiero que el sistema me permita modificar usuarios, ya sea su nombre, contraseña o rol, y eliminar usuarios, borrándolos de la base de datos. (HU implementada con el proyecto base).
![](/docs/mockups/Mockup%20Listar%20Usuarios.png)

 ### HU-(ISSUE#69): Listar desarrolladores ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/69])
Como administrador y usuario quiero que el sistema me permita listar todas las personas que han participado en el desarrollo
del juego, junto con información básica de cada uno de ellos. (HU implementada con el proyecto base).
![](/docs/mockups/Mockup%20Listar%20Desarrolladores.png)

 ### HU-(ISSUE#71): Crear una partida ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/71])
Como usuario y administrador quiero que el sistema me permita crear una partida para que otros jugadores se unan, asignandole un nombre y una contraseña, añadiendose a la lista de partidas disponibles.
![](/docs/mockups/Mockup%20Crear%20Partida.png)

 ### HU-(ISSUE#72): Listar y unirse a partidas ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/72])
Como usuario y administrador quiero que el sistema me permita listar las diferentes partidas disponibles para unirme a ellas.
![](/docs/mockups/Mockup%20LIstar%20Partidas.png)

 ### HU-(ISSUE#160): Buscar partida por nombre ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/160])
 Como usuario quiero buscar partidas disponibles por su nombre.
 ![](/docs/mockups/Mockup%20Buscar%20Partida%20Por%20Nombre.png)

 ### HU-(ISSUE#152): Colocar casilla ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/152])
 Como jugador dentro de una partida quiero poder colocar una casilla en el tablero.
 ![](/docs/mockups/Mockup%20Colocar%20Casilla.png)

 ### HU-(ISSUE#162): Elegir color ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/162])
 Como jugador dentro de una lobby, quiero poder elegir el color de mi salmón.
 ![](/docs/mockups/Mockup%20Elegir%20Color.png)

 ### HU-(ISSUE#163): Ver jugadores ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/163])
 Como jugador dentro de una lobby, quiero poder ver quién está en la partida conmigo.
 ![](/docs/mockups/Mockup%20Listar%20Jugadores%20e%20Iniciar%20o%20cancelar%20partida..png)
 
 ### HU-(ISSUE#164): Iniciar partida ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/164])
 Como jugador dentro de una lobby, quiero poder iniciar la partida.
 ![](/docs/mockups/Mockup%20Listar%20Jugadores%20e%20Iniciar%20o%20cancelar%20partida..png)

 ### HU-(ISSUE#165): Abandonar partida ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/165])
 Como jugador dentro de una lobby, quiero poder abandonar la lobby.
 ![](/docs/mockups/Mockup%20Listar%20Jugadores%20e%20Iniciar%20o%20cancelar%20partida..png)

 ### HU-(ISSUE#166): Ver logros desbloqueados ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/166])
 Como usuario, quiero poder diferenciar los logros que tengo desbloqueados del resto de logros.
 ![](/docs/mockups/Mockup%20Listar%20Logros%20Desbloqueados.png)

 ### HU-(ISSUE#167): Ver las reglas ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/167])
 Como usuario, quiero consultar las reglas del juego. 
 ![](/docs/mockups/Mockup%20Ver%20Reglas.png)

 ### HU-(ISSUE#168): Borrar una partida ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/168])
 Como administrador, quiero poder borrar partidas de la lista. (HU implementada con el proyecto base).
 ![](/docs/mockups/Mockup%20Borrar%20Partida.png)

## Diagrama conceptual del sistema

![Descomposición en componentes](/docs/diagrams/Modelo-DP1-v3.png)

# Reglas de Negocio -> Normas del juego

## Preparación de la Partida.

### R1 - Orden de partida.
El jugador inicial será el último en haber comido salmón. Desde ahí, se rotará en el sentido de las agujas del reloj.

### R2 - Número de losetas.
Se deberá jugar con el número exacto de losetas especificadas en las normas (29 en total, sin contar Mar ni Desove): 7 losetas de agua, 4 losetas de salto de agua, 5 losetas de águila, 3 losetas de oso, 5 losetas de garza azul y 5 losetas de roca.

### R3 - Inicio de partida.
La colocación inicial de las losetas deberá respetar el orden de turno y no exceder tres espacios de ancho ni cuatro de largo.

### R4 - Fichas de salmón.
Cada jugador comenzará con cuatro fichas de salmón, colocadas en las losetas de Mar, con su cara inicial mostrando dos salmones.

## Mecánicas de Juego.

### R5 - Movimiento obligatorio.
Cada jugador dispone de 5 puntos de movimiento por turno, los cuales deben gastarse completamente. No se permite moverse hacia atrás.

### R6 - Tipos de movimiento.

· Nadar: Desplazarse a una loseta adyacente cuesta 1 punto por loseta, siempre que no haya obstáculos en la intersección.

· Saltar: Saltar por encima de una o más losetas cuesta 1 punto por cada loseta cruzada, más 1 adicional por cada obstáculo saltado.

### R7 - Capacidad de losetas.
Una loseta puede contener tantas fichas de salmón como jugadores haya en la partida, excepto en las losetas de roca, cuya capacidad es igual al número de jugadores menos uno.

### R8 - Loseta llena.
Si una loseta está llena, un salmón deberá saltarla, gastando los puntos de movimiento correspondientes. Si no puede hacerlo, no podrá avanzar.

### R9 - Amenazas.

· Águilas: Se comen un salmón si una ficha termina su salto o entra nadando en su loseta. Al hacerlo, la loseta se voltea y pasa a ser de agua normal.

· Osos: Se comen un salmón si una ficha salta sobre, desde o hacia su loseta. Si se cruza nadando, no afecta.

· Garzas azules: Al final del turno, las garzas devoran un salmón de cualquier ficha que se encuentre en su loseta.

## Condiciones del Tablero.

### R10 - Retirada de losetas.
Al final de cada ronda, se retiran las tres losetas más bajas del tablero. Excepciones: en la primera ronda no se retira nada; en la segunda ronda, se retiran las losetas de Mar.

### R11 - Adición de losetas.
Al final de cada ronda, se colocan tres nuevas losetas en la parte superior del río, excepto en la última ronda, donde solo se añaden dos losetas para dejar el centro libre para el Desove.

### R12 - Colocación final.
Las dos últimas losetas de la pila deben colocarse a los lados del río, dejando el espacio central libre para el Desove.

### R13 - Unión del Desove al río.
Cuando el Desove se une al río, ya no se añaden más losetas, pero las filas inferiores siguen eliminándose al final de cada ronda.

### R14 - Capacidad del Desove.
Las losetas de Desove no tienen un límite de capacidad para fichas de salmón.

## Finalización y puntuación.

### R15 - Final de partida.
La partida termina cuando todos los salmones están en el Desove o han sido eliminados.

### R16 - Puntuación.

· Cada salmón cuenta como 1 punto.

· Cada ficha de salmón en el Desove otorga puntos adicionales según el número de huevos en su casilla.

### R17 - Ganador.
El jugador con más puntos será el ganador. En caso de empate, gana quien tenga más salmones; si persiste, el jugador con fichas más adelantadas.

## Gestión de Partidas.

### R18 - Creación de partida.
Solo el creador de la partida puede iniciar la partida desde el lobby.

### R19 - Abandonar partida.
Los jugadores pueden abandonar la partida antes de que esta comience.

### R20 - Eliminar partida.
Un administrador puede eliminar partidas de la lista.

### R21 - Partidas simultáneas.
Un usuario puede participar en varias partidas simultáneamente, representando diferentes jugadores.

### R22 - Nombres de partidas.

· No pueden repetirse entre partidas activas.

· Deben tener entre 3 y 50 caracteres.

## Interacciones adicionales.

### R23 - Colocación y rotación de losetas.
Un jugador puede colocar y rotar una loseta cuya orientación sea relevante para el juego, únicamente durante su turno.

### R24 - Movimiento de salmones.
Cada jugador puede mover únicamente sus propios salmones y solo en su turno.

### R25 - Color de jugador.
El color asignado a cada jugador será único dentro de la partida.