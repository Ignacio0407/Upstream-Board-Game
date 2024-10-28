package es.us.dp1.l4_01_24_25.upstream.partida;

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
public class PartidaService {
        
    PartidaRepository partidaRepository;

    @Autowired
    public PartidaService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Partida> getPartidas() {
        return partidaRepository.findAll();
    }

    // COMPLETAR MANEJO ERRORES
    @Transactional(readOnly = true)
    public List<Partida> getSomePartidasById(List<Integer> ids) {
        List<Partida> partidas = new LinkedList<>();
        ids.stream().forEach(id -> partidas.add(getPartidaById(id)));
        //throw new ResourceNotFoundException("Partidas no encontradas");
        return new ArrayList<>(partidas);
    }

    @Transactional(readOnly = true)
    public List<Partida> getSomePartidasByName(List<String> names) {
        List<Partida> partidas = new LinkedList<>();
        names.stream().forEach(name -> partidas.add(getPartidaByName(name)));
        return new ArrayList<>(partidas);
    }

    /* Aunque el manejo de errores de operaciones CRUD se realice en el controller, pongo solamente este
       aquí porque simplifica muchísimo la gestión de errores de bastantes de los métodos implementados. */
    private Partida optionalToValueWithNotFoundException(Optional<Partida> op) {
        if (!op.isPresent()) {
            throw new ResourceNotFoundException("No existe la partida indicada");
        }
        return op.get();
    }

    @Transactional(readOnly = true)
    public Partida getPartidaById(Integer id) {
        Optional <Partida> op = partidaRepository.findById(id);
        return optionalToValueWithNotFoundException(op);
    }

    @Transactional(readOnly = true)
    public Partida getPartidaByName(String name) {
        Optional <Partida> op = Optional.ofNullable(partidaRepository.findByName(name));
        return optionalToValueWithNotFoundException(op);
    }

    @Transactional
    public void deleteAllPartidas() {
        partidaRepository.deleteAll();
    }

    @Transactional
    public void deleteSomePartidasById(List<Integer> idsToDelete) {
        idsToDelete.stream().forEach( id -> deletePartidaById(id));
    }

    @Transactional
    public void deleteSomePartidasByName(List<String> namesToDelete) {
        namesToDelete.stream().forEach( name -> deletePartidaByName(name));
    }

    @Transactional
    public void deletePartidaById(Integer id) {
        getPartidaById(id); // Si no existe p, ya lanza la excepcion.
        partidaRepository.deleteById(id);

    }

    @Transactional
    public void deletePartidaByName(String name) {
        Partida p = getPartidaByName(name); // Si no existe p, ya lanza la excepcion.
        partidaRepository.delete(p);
    }


    @Transactional
    private Partida updatePartida(Partida partidaNueva, Partida partidaToUpdate) {
        BeanUtils.copyProperties(partidaNueva, partidaToUpdate, "id");
        return partidaRepository.save(partidaToUpdate); // Guarda y retorna la versión actualizada
    }

    @Transactional
    public Partida updatePartidaById(Partida partidaNueva, Integer idtoUpdate) {
        Partida partidaToUpdate = getPartidaById(idtoUpdate); // Si no existe p, ya lanza la excepcion.
        return updatePartida(partidaNueva, partidaToUpdate);
    }

    @Transactional  
    public Partida updatePartidaByName(Partida partidaNueva, String nameToUpdate) {
        Partida partidaToUpdate = getPartidaByName(nameToUpdate); // Si no existe p, ya lanza la excepcion.
        return updatePartida(partidaNueva, partidaToUpdate);
    }


    @Transactional
	public Partida savePartida(Partida partida) {
		partidaRepository.save(partida);
		return partida;
	}

    @Transactional
	public List<Partida> savePartidas(List<Partida> partidas) throws DataAccessException {
        List<Partida> partidasFallidas = new LinkedList<>();
        partidas.forEach(partida -> {
            try {
                partidaRepository.save(partida);
            } catch (DataAccessException e) {
                partidasFallidas.add(partida);
            }
        });
		return partidasFallidas;
	}

}
