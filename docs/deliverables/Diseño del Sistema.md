# Documento de diseño del sistema

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


[Enlace al vídeo de explicación de las reglas del juego / partida jugada por el grupo](http://youtube.com)

## Diagrama(s) UML:

### Diagrama de Dominio/Diseño

![](/docs/diagrams/UML%20diagram.png)

### Diagrama de Capas (incluyendo Controladores, Servicios y Repositorios)

![](/docs/diagrams/Layers%20diagrampng.png)

## Descomposición del mockups del tablero de juego en componentes

En esta sección procesaremos el mockup del tablero de juego (o los mockups si el tablero cambia en las distintas fases del juego). Etiquetaremos las zonas de cada una de las pantallas para identificar componentes a implementar. Para cada mockup se especificará el árbol de jerarquía de componentes, así como, para cada componente el estado que necesita mantener, las llamadas a la API que debe realizar y los parámetros de configuración global que consideramos que necesita usar cada componente concreto. 
Por ejemplo, para la pantalla de visualización de métricas del usuario en un hipotético módulo de juego social:

![Descomposición en componentes de la interfaz de estadísticas](https://github.com/gii-is-DP1/react-petclinic/assets/756431/12b36c37-39ed-422e-b8d9-56c94753cbdc)

  - **App** – Componente principal de la aplicación
    - <span style="color: orange;"><b>NavBar</b></span> – Barra de navegación lateral
      - <span style="color: darkred;"><b>[NavButton]</b></span> – Muestra un botón de navegación con un icono asociado.
    - <span style="color: darkblue;"><b>UserNotificationArea</b></span> – Área de notificaciones e identificación del usuario actual
    - <span style="color: blue;"><b>MetricsBar</b></span> – En este componente se muestran las métricas principales del juego. Se mostrarán 4 métricas: partidas jugadas, puntos logrados, tiempo total, y cartas jugadas.
      - <span style="color: darkgreen;"><b>[MetricWell]</b></span> – Proporciona el valor y el incremento semanal de una métrica concreta.
    - <span style="color: purple;"><b>GamesEvolutionChart</b></span> – Muestra la tendencia de evolución en los últimos 4 meses en cuanto a partidas jugadas, ganadas, perdidas y abandonadas.
    - <span style="color: yellow;"><b>PopularCardsChart</b></span> – Muestra la proporción de las N (parámetro de configuración) cartas más jugadas en el juego por el jugador.
    - <span style="color: red;"><b>FrequentCoPlayersTable</b></span> – Muestra los jugadores con los que más se ha jugado (de M en M, donde M es un parámetro definido por la configuración del componente). Concretamente, se mostrarán el nombre, la fecha de la última partida, la localización del jugador, el porcentaje de partidas jugadas por ambos en las que el usuario ha ganado, y si el jugador es amigo o no del usuario.


## Documentación de las APIs
Se considerará parte del documento de diseño del sistema la documentación generada para las APIs, que debe incluir como mínimo, una descripción general de las distintas APIs/tags  proporcionadas. Una descripción de los distintos endpoints y operaciones soportadas. Y la especificación de las políticas de seguridad especificadas para cada endpoint y operación. Por ejemplo: “la operación POST sobre el endpoint /api/v1/game, debe realizarse por parte de un usuario autenticado como Player”.

Si lo desea puede aplicar la aproximación descrita en https://vmaks.github.io/2020/02/09/how-to-export-swagger-specification-as-html-or-word-document/ para generar una versión en formato Word de la especificación de la API generada por OpenAPI, colgarla en el propio repositorio y enlazarla en esta sección del documento.  En caso contrario debe asegurarse de que la interfaz de la documentación open-api de su aplicación está accesible, funciona correctamente, y está especificada conforme a las directrices descritas arriba.

## Patrones de diseño y arquitectónicos aplicados
En esta sección de especificar el conjunto de patrones de diseño y arquitectónicos aplicados durante el proyecto. Para especificar la aplicación de cada patrón puede usar la siguiente plantilla:

### Patrón: < Nombre del patrón >
*Tipo*: Arquitectónico | de Diseño

*Contexto de Aplicación*

Describir las partes de la aplicación donde se ha aplicado el patrón. Si se considera oportuno especificar el paquete donde se han incluido los elementos asociados a la aplicación del patrón.

*Clases o paquetes creados*

Indicar las clases o paquetes creados como resultado de la aplicación del patrón.

*Ventajas alcanzadas al aplicar el patrón*

Describir porqué era interesante aplicar el patrón.

## Decisiones de diseño


### Decisión 1: Elección del color del salmón
#### Descripción del problema:

En las normas del juego no se dice nada sobre como escoger el color de los salmones

#### Alternativas de solución evaluadas:

*Alternativa 1.a*: Que el color se asigne aleatoriamente al jugador.

*Ventajas:*
•	No hay peleas entre jugadores por el color del salmón.

*Inconvenientes:*
•	Si tienes alguna preferencia de color, no puedes elegirlo y dependes de la suerte.

*Alternativa 1.b*: Seleccionar el color del salmón.

*Ventajas:*
•	Puedes elegir tu color favorito para el salmón.

*Inconvenientes:*
•	Dos jugadores pueden querer el mismo color.

*Alternativa 1.c*: Crear un controlador que llame a un servicio de importación de datos, que a su vez invoca a un cliente REST de la API de datos oficiales de XXXX para traerse los datos, procesarlos y poder grabarlos desde el servicio de importación.

*Ventajas:*
•	No necesitamos inventarnos ni buscar nosotros lo datos.
•	Cumple 100% con la división en capas de la aplicación.
•	No afecta al trabajo diario de desarrollo y pruebas de la aplicación
*Inconvenientes:*
•	Supone mucho más trabajo. 
•	Añade cierta complejidad al proyecto

*Justificación de la solución adoptada*
Hemos decidido que deberiamos dejar elegir el color del salmón a los juagadores para que asi puedan ponerle su color favorito a estos.

### Decisión 2: Primer jugador
#### Descripción del problema:

En las normas del juego se dice que empieaza a jugar la persona que comió salmón por última vez.

#### Alternativas de solución evaluadas:

*Alternativa 2.a*: Se siguen las normas del juego.

*Ventajas:*
•	Se está jugando según las normas.

*Inconvenientes:*
•	No hay ningún control de quién esta mintiendo o no con lo la última vez que comió salmón, esto podría resultar en que todos los jugadores digan que comieron salmón hoy.

*Alternativa 2.b*: Aleatoriamente se asigna el orden.

*Ventajas:*
•	No esta el problema de que la gente se invente la fecha de la última vez que comió salmón.

*Inconvenientes:*
•	No seguimos las normas del juego.

*Alternativa 2.c*: El jugador que inicia la partida es el primero en jugar.

•	No esta el problema de que la gente se invente la fecha de la última vez que comió salmón.
•	Es más intuitivo.

*Inconvenientes:*
•	No seguimos las normas del juego.

*Justificación de la solución adoptada*
Hemos decidido que el jugador que inicia la partida es el primero en jugar porque es más intutivo que la opción de que se asigne automaticamente. Además que no tendríamos el problema de que la gente se invente la fecha.


### Decisión 3: Cero jugadores en una partida
#### Descripción del problema:

Teniendo un botón de salir de partida, una partida podría quedarse sin jugadores.

#### Alternativas de solución evaluadas:

*Alternativa 3.a*: La partida sigue en curso.

*Ventajas:*
•	Ninguna.

*Inconvenientes:*
•	No tiene ningún tipo de sentido que una partida tenga 0 jugadores.

*Alternativa 3.b*: Poner un botón de acabar partida.

*Ventajas:*
•	No se quedaría la partida sin ningún jugador, ya que antes le darian al botón de finalizar partida.

*Inconvenientes:*
•	Cualquier jugador en cualquier momento puedo finalizar la partida.

*Alternativa 3.c*: Si la partida tiene 0 jugadores se da por finalizada la partida.

•	Tiene sentido que si una partida se queda sin jugadores se de por terminada.

*Inconvenientes:*
•	No se podría retomar esa partida.

*Justificación de la solución adoptada*
Hemos decidido que si la partida tiene 0 jugadores se da por finalizada la partida, porque no vemos mucha utilidad en dejar una partida vacia para poder retormarla despues.

### Decisión 4: No es obligatorio usar una contraseña para entrar en una partida.
#### Descripción del problema:

Tener contraseñas para entrar en una partida, para que así no entre cualquiera.

#### Alternativas de solución evaluadas:

*Alternativa 4.a*: No hay contraseña.

*Ventajas:*
•	Es más facil entrar en la partida.

*Inconvenientes:*
•	Cualquier persona puede entrar en tu partida.

*Alternativa 4.b*: Poner contraseña obligatoria.

*Ventajas:*
•	Puedes tener control de quién entra en tu partida.

*Inconvenientes:*
•	Puede que quieras jugar una partida con gente que no conoces.

*Alternativa 4.c*: Contraseña no obligatoria.

•	Puedes tener control de quién entra en tu partida.
•	Puedes jugar una partida con gente que no conoces.

*Inconvenientes:*
•	Ninguno.

*Justificación de la solución adoptada*
Hemos decidido que no sea obligatorio usar una contraseña para entrar en una partida, porque asi no le quitas la opción de ponerle contraseña a la gente que quiera ponerla y no obligar a ponerla a quienes no quisieran.

### Decisión 5: Casilla mar
#### Descripción del problema:

La casilla mar es similar al espacio azul en el grid.

#### Alternativas de solución evaluadas:

*Alternativa 5.a*: Cargar la imagen de la casilla mar.

*Ventajas:*
•	Va más fiel al juego original.

*Inconvenientes:*
•	Requiere má tiempo para implementarlo.

*Alternativa 5.b*: No cargar la imagen de la casilla mar.

*Ventajas:*
•	Es más facil de implementar.
•	No hay una difrencia estetica muy grande.

*Inconvenientes:*
•	No es fiel al juego original.

*Justificación de la solución adoptada*
Hemos decidido que la casilla mar no tenga una imagen sino que sea un espacio azul en el grid, ya que
ahorramos código y tiempo.

### Decisión 6: El jugador que creó la partida se sale de la partida
#### Descripción del problema:

Al tener en cuenta quién fue el creador de la partida para asignar el orden en la partida, si este jugaodr sale, no está claro quien pasaría a ser el jugador inicial.

#### Alternativas de solución evaluadas:

*Alternativa 6.a*: Se asigna aleatoriamente.

*Ventajas:*
•	Hay algún tipo de orden en cuanto a quien empieza.

*Inconvenientes:*
•	Es aleatorio.

*Alternativa 6.b*: El primer jugador que entró en la partida tras su creación queda con el rol de creador de la partida.

*Ventajas:*
•	Es más intuitivo.
•	Hay algún tipo de ordén en cuanto a quien empieza.

*Inconvenientes:*
•	Ninguno.

*Justificación de la solución adoptada*
Hemos decidido que una partida no se cancela porque el jugador que la creó se salga de esta, si esto ocurre, el primer jugador que entró en esta tras su creación quedará con el rol de creador de la partida. Esto se ha decidido porque en la pantalla en la que se ven los nombres de los jugadores, estos se ven en ordén de entrada en la partida, asi que al salirse el primero (jugador que creó la partida) el resto sube de posición.

### Decisión 7: Movimiento de los salmones durante la partida
#### Descripción del problema:
Al principio, el movimiento del salmón era de una coordenada a otro, pero esto no permitía saber qué había por medio (para ello tendríamos que haber creado un grafo en base a las coordenadas de las casillas con JGraphT, lo que habría añadido muchísima complejidad innecesaria al juego)

*Alternativa 7.a*: Mover a una coordenada elegida.

*Ventajas:*
•	El juego es mucho más ágil al tener que hacer menos llamadas al backend para moverse.

*Inconvenientes:*
•	Es imposible saber por qué casillas ha pasado el salmón y, si, por tanto, debería haber visto reducido su número de salmones, o sea, si la amenaza de alguna casilla se lo habría comido, por lo que no se puede jugar al juego, al no seguir las reglas.

*Alternativa 7.b*: El salmón se mueve de casilla en casilla, de 1 en 1.

*Ventajas:*

• Podemos saber la casilla a la que salta el salmón, lo que posibilita saber la energía a gastar, las amenzas de la siguiente casilla...  
• Ayuda al jugador a planificar mejor su jugada y a perderse menos


*Inconvenientes:*
•	El juego es más lento al realizar una llamada al backend para avanzar una casilla.

*Justificación de la solución adoptada*
Hemos decidido que los salmones se muevan de uno en uno, ya que facilita mucho la programación para las amenazas y los saltos, y no da sensación de lentitud a la partida.

## Refactorizaciones aplicadas

Si ha hecho refactorizaciones en su código, puede documentarlas usando el siguiente formato:

### Refactorización X: 
En esta refactorización añadimos un mapa de parámetros a la partida para ayudar a personalizar la información precalculada de la que partimos en cada fase del juego.
#### Estado inicial del código
```Java 
class Animal
{
}
``` 
_Puedes añadir información sobre el lenguaje concreto en el que está escrito el código para habilitar el coloreado de sintaxis tal y como se especifica en [este tutorial](https://docs.github.com/es/get-started/writing-on-github/working-with-advanced-formatting/creating-and-highlighting-code-blocks)_

#### Estado del código refactorizado

```
código fuente en java, jsx o javascript
```
#### Problema que nos hizo realizar la refactorización
_Ej: Era difícil añadir información para implementar la lógica de negocio en cada una de las fases del juego (en nuestro caso varía bastante)_
#### Ventajas que presenta la nueva versión del código respecto de la versión original
_Ej: Ahora podemos añadir arbitrariamente los datos que nos hagan falta al contexto de la partida para que sea más sencillo llevar a cabo los turnos y jugadas_

### Refactorización 2 (Ignacio): 
En esta refactorización se cambió una gran parte del backend a inglés para tenerlo todo en el mismo idioma. Se ha realizado lo ejemplificado con cada entidad, y se han renombrado a inglés varios métodos de controladores, servicios y repositorios.
#### Estado inicial del código
```Java 
public class PartidaCasilla extends BaseEntity {

    Integer capacidad;

    Integer orientacion;

    Coordinate coordinate;

    @JsonSerialize(using = CasillaSerializer.class)
    @JsonDeserialize(using = CasillaDeserializer.class)
    @ManyToOne
    Casilla casilla;

    @ManyToOne
    @JoinColumn(name = "partida_id")
    @JsonSerialize(using = PartidaSerializer.class)
    @JsonDeserialize(using = PartidaDeserializer.class)
    Partida Partida;

}
``` 

#### Estado del código refactorizado

```
public class MatchTile extends BaseEntity {

    Integer capacity;

    Integer orientation;

    Integer salmonsNumber;

    @Embedded
    Coordinate coordinate;

    @JsonSerialize(using = TileSerializer.class)
    @JsonDeserialize(using = TileDeserializer.class)
    @ManyToOne
    Tile tile;

    @ManyToOne
    @JoinColumn(name = "match_id")
    @JsonSerialize(using = matchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    Match match;

}
```
#### Problema que nos hizo realizar la refactorización
Era difícil encontrar los nombres de las cosas, ya que muchos estaban en español pero algunos estaban en inglés.
#### Ventajas que presenta la nueva versión del código respecto de la versión original
Ahora está todo mucho más uniforme y se encuentran fácilmente los nombres de propiedades, métodos...

### Refactorización 3 (Ignacio): 
En esta refactorización se cambió la construcción o inicialización del grid en el frontend
#### Estado inicial del código
```jsx 
useEffect(() => {
        // Construir el nuevo estado del grid basado en gridTiles
        const newGrid = Array(18).fill(null).map(() => []); // Crea una cuadrícula vacía de 18 espacios
        const gridWidth = 3; // Ancho de la cuadrícula (número columnas)
        const gridHeight = 6; // Altura de la cuadrícula (número filas)
        // Asignar las tiles con coordenadas al grid
        gridTiles.forEach((tile) => {
            // Convertir las coordenadas (x, y) en un índice del grid
            const index = (gridHeight - 1 - tile[0].coordinate.y) * gridWidth + tile[0].coordinate.x;
            const image = tile[1]; // Obtener la imagen asociada al tile
            newGrid[index].push([tile, image,"tile"]); // Asignar la tile con su imagen al grid 
        });

            gridSalmons.forEach((salmon) => {
                const index = (gridHeight - 1 - salmon[0].coordinate.y) * gridWidth + salmon[0].coordinate.x;
                const image = salmon[1]; // Obtener la imagen asociada al salmon
                newGrid[index].push(salmon,image,"salmon")
            });
            setGrid(newGrid);
            //console.log(grid)
        }, [gridTiles,gridSalmons]);
        

// La renderización, hubo otros 
{gridS[0].length > 0 && 
            <div className='game-container'>
                <div className='grid1'>
                {grid.map((cell, index) => (
                    <div key={index} onClick={() => handleGridClick(index)} className="grid-item"> 
                        {cell.map((element, i) => (

                            <img 
                            key = {i}
                            src = {element[1]}
                            alt=""
                            style={
                        element[2] === "tile"
                        ? { width: '150px', ...getRotationStyle(element[0][0]) }
                        : { width: '50px',position: 'absolute',top:`${9*i}px`, margin:0} // Superponer salmons
                }
/>
                        ))}
                    </div>
                ))}
                </div>
``` 

#### Estado del código refactorizado

```
useEffect(() => {
        const newGrid = Array(18).fill(null).map(() => ({
            tile: null, 
            salmons: [] 
        }));
    
        const gridWidth = 3;
        const gridHeight = 6;
    
        gridTiles.forEach((tile) => {
            const index = (gridHeight - 1 - tile[0].coordinate.y) * gridWidth + tile[0].coordinate.x;
            newGrid[index].tile = {
                data: tile[0],
                image: tile[1]
            };
        });
    
        gridSalmons.forEach((salmon) => {
            const index = (gridHeight - 1 - salmon[0].coordinate.y) * gridWidth + salmon[0].coordinate.x;
            newGrid[index].salmons.push({
                data: salmon[0],
                image: salmon[1]
            });
        });
        setGrid(newGrid);
    }, [gridTiles, gridSalmons]); 
    
    
// La parte de la renderización cambiada
{gridS[0].length > 0 && 
                <div className='game-container'>
                    <div className='grid1'>
                    {grid.map((cell, index) => (
                    <div 
                        key={index} 
                        onClick={() => handleGridClick(index)} 
                        className="grid-item"
                        style={{ position: 'relative' }}
                    >
                        {cell.tile && (
                        <img 
                            src={cell.tile.image}
                            alt=""
                            style={{ 
                            width: '150px', 
                            ...getRotationStyle(cell.tile.data)
                            }}
                        />
                        )}

                        {cell.salmons && cell.salmons.map((salmon, sIndex) => {
                            const position = calculateSalmonPosition(sIndex, cell.salmons.length);
                            return (
                                <img
                                key={`salmon-${index}-${sIndex}`}
                                src={salmon.image}
                                alt=""
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleSalmonClick([salmon.data, salmon.image]);
                                }}
                                style={{
                                    width: '50px',
                                    position: 'absolute',
                                    ...position,
                                    transition: 'all 0.3s ease-in-out',
                                    zIndex: 2,
                                    cursor: 'pointer'
                                }}
                                />
                            );
                            })}
                    </div>
                    ))}

                    </div>
```
#### Problema que nos hizo realizar la refactorización
Era imposible mover los salmones una vez entraban en el tablero, o sea, cuando solo se habían movido una vez
#### Ventajas que presenta la nueva versión del código respecto de la versión original
Ahora se pueden mover los salmones en el tablero

