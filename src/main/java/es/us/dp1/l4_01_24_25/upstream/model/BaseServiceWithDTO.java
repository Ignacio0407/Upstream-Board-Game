/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

package es.us.dp1.l4_01_24_25.upstream.model;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

public abstract class BaseServiceWithDTO<T extends BaseEntity, D, I> extends BaseService<T, I> {

    protected final EntityMapper<T, D> mapper;
    protected final BaseRepository<T, D, I> dtoRepository;

    protected BaseServiceWithDTO(BaseRepository<T, D, I> repository, EntityMapper<T, D> mapper) {
        super(repository);
        this.dtoRepository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<D> findAllAsDTO() {
        return this.dtoRepository.findAllAsDTO();
    }

    public D findDTOOrResourceNotFoundException (Optional<D> entity, I id) {
        if (entity.isPresent()) return entity.get(); 
        else throw new ResourceNotFoundException(this.entityName + " not found with ID: " + id);
    }

    @Transactional(readOnly = true)
    public D findByIdAsDTO(I id) {
        return this.findDTOOrResourceNotFoundException(this.dtoRepository.findByIdAsDTO(id), id);
    }

    @Transactional
    public D saveAsDTO(@Valid D dto) {
        T savedEntity = this.save(mapper.toEntity(dto));
        return this.mapper.toDTO(savedEntity);
    }

    @Transactional
    public D updateAsDTO(I id, @Valid D dto) {
        T updated = this.update(id, mapper.toEntity(dto));
        return this.mapper.toDTO(updated);
    }
    
}