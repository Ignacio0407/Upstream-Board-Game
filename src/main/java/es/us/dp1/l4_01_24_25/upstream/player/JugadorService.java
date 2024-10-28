package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

@Service
public class JugadorService {
        
    JugadorRepository jugadorRepository;

    @Autowired
    public JugadorService(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Jugador> getJugadores() {
        return jugadorRepository.findAll();
    }

    // COMPLETAR MANEJO ERRORES
    @Transactional(readOnly = true)
    public List<Jugador> getSomeJugadoresById(List<Integer> ids) {
        List<Jugador> Jugadores = new LinkedList<>();
        ids.stream().forEach(id -> Jugadores.add(getJugadorById(id)));
        //throw new ResourceNotFoundException("Jugadores no encontradas");
        return new ArrayList<>(Jugadores);
    }

    @Transactional(readOnly = true)
    public List<Jugador> getSomeJugadoresByName(List<String> names) {
        List<Jugador> Jugadores = new LinkedList<>();
        names.stream().forEach(name -> Jugadores.add(getJugadorByName(name)));
        return new ArrayList<>(Jugadores);
    }

    private Jugador optionalToValueWithNotFoundException(Optional<Jugador> op) {
        if (!op.isPresent()) {
            throw new ResourceNotFoundException("No existe la Jugador indicada");
        }
        return op.get();
    }

    @Transactional(readOnly = true)
    public Jugador getJugadorById(Integer id) {
        Optional <Jugador> op = jugadorRepository.findById(id);
        return optionalToValueWithNotFoundException(op);
    }

    @Transactional(readOnly = true)
    public Jugador getJugadorByName(String name) {
        Optional <Jugador> op = Optional.ofNullable(jugadorRepository.findByName(name));
        return optionalToValueWithNotFoundException(op);
    }

    @Transactional
    public void deleteAllJugadores() {
        jugadorRepository.deleteAll();
    }

    @Transactional
    public void deleteSomeJugadoresById(List<Integer> idsToDelete) {
        idsToDelete.stream().forEach( id -> deleteJugadorById(id));
    }

    @Transactional
    public void deleteSomeJugadoresByName(List<String> namesToDelete) {
        namesToDelete.stream().forEach( name -> deleteJugadorByName(name));
    }

    @Transactional
    public void deleteJugadorById(Integer id) {
        getJugadorById(id); // Si no existe p, ya lanza la excepcion.
        jugadorRepository.deleteById(id);

    }

    @Transactional
    public void deleteJugadorByName(String name) {
        Jugador p = getJugadorByName(name); // Si no existe p, ya lanza la excepcion.
        jugadorRepository.delete(p);
    }


    @Transactional
    private Jugador updateJugador(Jugador JugadorNueva, Jugador JugadorToUpdate) {
        BeanUtils.copyProperties(JugadorNueva, JugadorToUpdate, "id");
        return jugadorRepository.save(JugadorToUpdate); // Guarda y retorna la versión actualizada
    }

    @Transactional
    public Jugador updateJugadorById(Jugador JugadorNueva, Integer idtoUpdate) {
        Jugador JugadorToUpdate = getJugadorById(idtoUpdate); // Si no existe p, ya lanza la excepcion.
        return updateJugador(JugadorNueva, JugadorToUpdate);
    }

    @Transactional  
    public Jugador updateJugadorByName(Jugador JugadorNueva, String nameToUpdate) {
        Jugador JugadorToUpdate = getJugadorByName(nameToUpdate); // Si no existe p, ya lanza la excepcion.
        return updateJugador(JugadorNueva, JugadorToUpdate);
    }


    @Transactional
	public Jugador saveJugador(Jugador Jugador) throws DataAccessException {
		jugadorRepository.save(Jugador);
		return Jugador;
	}

    @Transactional
	public List<Jugador> saveJugadores(List<Jugador> Jugadores) throws DataAccessException {
		List<Jugador> jugadoresGuardadas = new LinkedList<>();
        List<Jugador> jugadoresFallidas = new LinkedList<>();
        Jugadores.forEach(jugador -> {
            try {
                Jugador saved = jugadorRepository.save(jugador);
                jugadoresGuardadas.add(saved);
            } catch (DataAccessException e) {
                jugadoresFallidas.add(jugador);
            }
        });
        if (jugadoresFallidas.isEmpty()) {
            System.out.println("Jugadores guardadas correctamente");
        }
        else {
            System.out.println("Algunas Jugadores no se han guardado, serán devueltas");
        }
		return jugadoresFallidas;
	}

}
