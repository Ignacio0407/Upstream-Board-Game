package es.us.dp1.l4_01_24_25.upstream.partida;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.player.Jugador;

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

    // Buscar varias partidas a la vez
    @Transactional(readOnly = true)
    public List<Partida> getSomePartidasByName(List<String> names) {
        List<Partida> partidas = new LinkedList<>();
        names.stream().forEach(name -> partidas.add(getPartidaByName(name)));
        return new ArrayList<>(partidas);
    }

    @Transactional(readOnly = true)
    public List<Jugador> getPlayersFromGame(Integer id) throws ResourceNotFoundException{
        List<Jugador> p = partidaRepository.findPlayersFromGame(id);
        if(!p.isEmpty()) return p;
        else throw new ResourceNotFoundException("No players in game " + id);
    }

    @Transactional(readOnly = true)
    public Integer getNumjugadores(Integer id) throws ResourceNotFoundException{
        List<Jugador> players = getPlayersFromGame(id);
        return players.size();
    }

    /* Aunque el manejo de errores de operaciones CRUD se realice en el controller, pongo solamente este
       aquí porque simplifica muchísimo la gestión de errores de bastantes de los métodos implementados. */
    private Partida optionalToValueWithNotFoundException(Optional<Partida> op) {
        if (!op.isPresent()) {
            return null;
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
    public void deletePartidaById(Integer id) {
        getPartidaById(id); // Si no existe p, ya lanza la excepcion.
        partidaRepository.deleteById(id);

    }


    @Transactional
    private Partida updatePartida(Partida partidaNueva, Partida partidaToUpdate) {
        BeanUtils.copyProperties(partidaNueva, partidaToUpdate, "id");
        return partidaRepository.save(partidaToUpdate); // Guarda y retorna la versión actualizada
    }

    @Transactional
    public Partida updatePartidaById(Partida partidaNueva, Integer idtoUpdate) {
        Partida partidaToUpdate = getPartidaById(idtoUpdate);
        if (partidaToUpdate == null){
            return null;
        }
        System.out.println("ñññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññññ");
        System.out.println("Partida a actualizar: " + partidaToUpdate.getNumjugadores());
        System.out.println("##########################################################################################################################################################");
        if (partidaToUpdate.getNumjugadores() != null && partidaToUpdate.getNumjugadores().equals(0)){ 
            partidaToUpdate.setEstado(Estado.FINALIZADA);
        }
        return updatePartida(partidaNueva, partidaToUpdate);
    }


    @Transactional
	public Partida savePartida(Partida partida) {
		partidaRepository.save(partida);
		return partida;
	}

    // FALTA VER EL ID
    @Transactional
    public Partida copyPartida(Partida partidaOriginal) {
        Partida partidaCopia = new Partida();
        BeanUtils.copyProperties(partidaOriginal, partidaCopia, "id");
        return partidaCopia;
    }
    
}
