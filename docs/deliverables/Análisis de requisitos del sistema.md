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

## Historias de Usuario

 ### HU-001: Iniciar sesión
Como usuario quiero que el sistema me permita iniciar sesión para que guarde mis datos y me permita poder jugar partidas.

![](/docs/mockups/Mockup%20Inicio%20de%20Sesion.png)

 ### HU-002: Registrarse
Como usuario quiero que el sistema me permita registrarme, guardando mis datos para permitirme iniciar sesión posteriormente.

![](/docs/mockups/Mockup%20Registrarse.png)

 ### HU-003: Crear partidas como usuario
Como usuario quiero que el sistema me permita crear partidas para que otros jugadores se unan, asignándole un nombre y una contraseña, añadiéndose a la lista de partidas disponibles.

![](/docs/mockups/Mockup%20Crear%20Partida.png)

 ### HU-004: Crear partidas como administrador
Como administrador quiero que el sistema me permita crear una partida para que otros jugadores se unan, asignándole un nombre y una contraseña, añadiéndose a la lista de partidas disponibles.

 ### HU-005: Listar partidas como usuario
Como usuario quiero que el sistema me permita listar las diferentes partidas disponibles para unirme a ellas.

 ### HU-006: Listar partidas como administrador
Como administrador quiero que el sistema me permita listar las diferentes partidas disponibles para unirme a ellas.

 ### HU-007: Unirse a partidas como usuario
Como usuario quiero que el sistema me permita unirme partidas para jugar con otros jugadores.

 ### HU-008: Unirse a partidas como administrador
Como administrador quiero que el sistema me permita unirme partidas para jugar con otros jugadores.

 ### HU-009: Buscar partida por nombre
Como usuario quiero buscar partidas disponibles por su nombre.

 ### HU-010: Elegir color
Como jugador dentro de una lobby, quiero poder elegir el color de mi salmón.

 ### HU-011: Ver jugadores
Como jugador dentro de una lobby, quiero poder ver quién está en la partida conmigo.

 ### HU-012: Iniciar partida
Como jugador dentro de una lobby, quiero poder iniciar la partida.

 ### HU-013: Abandonar partida
Como jugador dentro de una lobby, quiero poder abandonar la lobby.

 ### HU-014: Borrar una partida
Como administrador, quiero poder borrar partidas de la lista.

 ### HU-015: Listar logros
Como usuario quiero que el sistema me permita listar logros, independientemente de si se han conseguido o no, ya sean personales (obtenidos por mí) o globales.

 ### HU-016: Listar logros
Como administrador quiero que el sistema me permita listar logros, independientemente de si se han conseguido o no, ya sean personales (obtenidos por mí) o globales.

 ### HU-017: Crear logros
Como administrador quiero que el sistema me permita crear logros, añadiendo valores a cada campo.

 ### HU-018: Modificar logros
Como administrador quiero que el sistema me permita modificar logros, ajustando cada parámetro del logro a mi gusto.

 ### HU-019: Eliminar logros
Como administrador quiero que el sistema me permita modificar eliminar logros, borrándolos de la base de datos.

 ### HU-020: Ver logros desbloqueados
Como usuario, quiero poder diferenciar los logros que tengo desbloqueados del resto de logros.

 ### HU-021: Ver las reglas
Como usuario, quiero consultar las reglas del juego.

 ### HU-022: Listar usuarios
Como administrador quiero que el sistema me permita listar todos los usuarios del juego registrados además de ver sus nombres y roles.

 ### HU-023: Crear un usuario
Como administrador quiero que el sistema me permita crear un nuevo usuario, especificando su nombre, contraseña y rol, eligiendo entre jugador o administrador.

 ### HU-024: Modificar perfil como usuario
Como usuario quiero que el sistema me permita modificar mis datos de usuario, ya sea mi nombre o contraseña.

 ### HU-025: Modificar un usuario como administrador
Como administrador quiero que el sistema me permita modificar usuarios, ya sea su nombre, contraseña o rol.

 ### HU-026: Eliminar un usuario
Como administrador quiero que el sistema me permita eliminar usuarios, borrándolos de la base de datos.

 ### HU-027: Listar desarrolladores
Como administrador y usuario quiero que el sistema me permita listar todas las personas que han participado en el desarrollo del juego, junto con información básica de cada uno.

 ### HU-028: Elegir color
Como jugador dentro de una lobby, quiero poder elegir el color de mi salmón.

 ### HU-029: Colocar losetas
Como jugador dentro de una partida iniciada, quiero poder poner losetas en el tablero cuando sea mi turno.

 ### HU-030: Mover fichas de salmón
Como jugador dentro de una partida iniciada, quiero poder mover mis salmones, ya sea saltando o nadando.

 ### HU-031: Recuento de puntos
Como jugador en una partida iniciada quiero que se recuenten los puntos de todos los jugadores al terminar para designar un ganador.

 ### HU-032: Gestión de turnos
Como jugador quiero que termine mi turno cuando gaste mis 5 puntos de movimiento.

 ### HU-033: Finalizar partida
Como jugador quiero que la partida que estoy jugando finalice cuando todos los salmones supervivientes estén en la loseta de desove.

 ### HU-034: Modificar salmones de una ficha
Como jugador quiero que cuando, en una partida, una amenaza se coma uno de mis salmones de una ficha de dos, mi ficha de dos salmones se convierta en una de uno.

 ### HU-035: Eliminar ficha de salmón
Como jugador quiero que cuando, en una partida, una amenaza se coma el salmón de una de mis fichas de un salmón, la ficha quede eliminada de la partida.

 ### HU-036: Rotar losetas
Como jugador quiero poder rotar las losetas de juego antes de colocarlas en el tablero para poder planear mi estrategia.

 ### HU-037: Puntuación final
Como jugador quiero poder ver mi puntuación final para saber quién ha resultado ganador de la partida.

 ### HU-038: Entrar al desove
Como jugador quiero poder mover mis fichas de salmón a las losetas de desove para poder conseguir puntos al final de la partida.

 ### HU-039: Escribir por chat
Como jugador quiero poder escribir y enviar mensajes a través del chat para comunicarme con otros jugadores.

 ### HU-040: Número de partidas por usuario
Como usuario quiero poder ver el número de partidas que he jugado para poderme comparar con mis amigos.

 ### HU-041: Número de jugadores por partida
Como jugador quiero poder ver cuántos jugadores hay conmigo en una partida para saber contra quién compito.

 ### HU-042: Estadísticas de usuario
Como jugador quiero poder ver mis estadísticas personales para ver mi estilo de juego.


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
