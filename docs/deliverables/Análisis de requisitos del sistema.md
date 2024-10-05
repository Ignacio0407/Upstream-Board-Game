# Documento de análisis de requisitos del sistema

## Introducción

¿Qué valor puede aportar? ¿Qué objetivos pretendemos alcanzar con su implementación?

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

Desarrolladores: Los desarrolladores, tras registrarse y loguearse, podrán realizar las mismas acciones que los jugadores además de tener la capacidad de crear logros, listar jugadores, listar las partidas jugadas y visualizar los desarrolladores del proyecto. 

## Historias de Usuario -> Visual, lo que puede hacer el usuario.

 ### HU-(ISSUE#65): Iniciar sesión ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/65])
Como jugador quiero que el sistema me permita iniciar sesión para que guarde mis datos y me permita poder jugar partidas.

![](/docs/mockups/Mockup%20Inicio%20de%20Sesion.png)

 ### HU-(ISSUE#70): Registrarse ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/70])
Como jugador quiero que el sistema me permita registrarme, guardando mis datos para permitirme iniciar sesión
posteriormente.

![](/docs/mockups/Mockup%20Registrarse.png)

 ### HU-(ISSUE#ID): Jugar partidas ([Enlace a la Issue asociada a la historia de usuario]()
Como jugador quiero que el sistema me permita jugar partidas, bien sea creándolas o uniéndome a ellas.
|-----|
|Mockups (prototipos en formato imagen de baja fidelidad) de la interfaz de usuario del sistema|
Interacciones con sistema: en la pantalla inicial hay un botón Jugar partida que te permite escoger entre crear una partida o unirte a una.

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
sus nombres y sus roles.
![](/docs/mockups/Mockup%20Listar%20Usuarios.png)

 ### HU-(ISSUE#68): Crear un usuario ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/68])
Como administrador quiero que el sistema me permita crear un nuevo usuario, especificando su nombre, contraseña
y rol, eligiendo entre jugador o administrador.
![](/docs/mockups/Mockup%20Crear%20Usuarios.png)

 ### HU-(ISSUE#68): Modificar y eliminar un usuario ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/68])
Como administrador quiero que el sistema me permita modificar usuarios, ya sea su nombre, contraseña o rol, y eliminar usuarios, borrándolos de la base de datos.
![](/docs/mockups/Mockup%20Listar%20Usuarios.png)

 ### HU-(ISSUE#69): Listar desarrolladores ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/69])
Como administrador y jugador quiero que el sistema me permita listar todas las personas que han participado en el desarrollo
del juego, junto con información básica de cada uno de ellos.
![](/docs/mockups/Mockup%20Listar%20Desarrolladores.png)

 ### HU-(ISSUE#71): Crear una partida ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/71])
Como jugador y administrador quiero que el sistema me permita crear una partida para que otros jugadores se unan, asignandole un nombre y una contraseña, añadiendose a la lista de partidas disponibles.
![]()

 ### HU-(ISSUE#72): Listar y unirse a partidas ([https://github.com/gii-is-DP1/DP1-2024-2025--l4-01/issues/72])
Como jugador y administrador quiero que el sistema me permita crear una partida para que otros jugadores se unan, asignandole un nombre y una contraseña, añadiendose a la lista de partidas disponibles.
![]()

## Diagrama conceptual del sistema
_En esta sección debe proporcionar un diagrama UML de clases que describa el modelo de datos a implementar en la aplicación. Este diagrama estará anotado con las restricciones simples (de formato/patrón, unicidad, obligatoriedad, o valores máximos y mínimos) de los datos a gestionar por la aplicación. _

_Recuerde que este es un diagrama conceptual, y por tanto no se incluyen los tipos de los atributos, ni clases específicas de librerías o frameworks, solamente los conceptos del dominio/juego que pretendemos implementar_
Ej:

![Descomposición en componentes](https://raw.githubusercontent.com/CarlosCerdaMorales/Examen-Online-Y-Offline/a6fc7acb74c1e66318c3459183d2988c6d55ed9f/image.png)

_Si vuestro diagrama se vuelve demasiado complejo, siempre podéis crear varios diagramas para ilustrar todos los conceptos del dominio. Por ejemplo podríais crear un diagrama para cada uno de los módulos que quereis abordar. La única limitación es que hay que ser coherente entre unos diagramas y otros si nos referimos a las mismas clases_

_Puede usar la herramienta de modelado que desee para generar sus diagramas de clases. Para crear el diagrama anterior nosotros hemos usado un lenguaje textual y librería para la generación de diagramas llamada Mermaid_

_Si deseais usar esta herramienta para generar vuestro(s) diagramas con esta herramienta os proporcionamos un [enlace a la documentación oficial de la sintaxis de diagramas de clases de _ermaid](https://mermaid.js.org/syntax/classDiagram.html)_

## Reglas de Negocio -> Normas del juego
### R-< X > < Nombre Regla de negocio >
_< Descripción de la restricción a imponer >_

_Ej:_ 
### R1 – Diagnósticos imposibles
El diagnóstico debe estar asociado a una enfermedad que es compatible con el tipo de mascota de su visita relacionada. Por ejemplo, no podemos establecer como enfermedad diagnosticada una otitis cuando la visita está asociada a una mascota que es un pez, porque éstos no tienen orejas ni oídos (y por tanto no será uno de los tipos de mascota asociados a la enfermedad otitis en el vademecum).

…

_Muchas de las reglas del juego se transformarán en nuestro caso en reglas de negocio, por ejemplo, “la carta X solo podrá jugarse en la ronda Y si en la ronda anterior se jugó la carta Z”, o “en caso de que un jugador quede eliminado el turno cambia de sentido”_


