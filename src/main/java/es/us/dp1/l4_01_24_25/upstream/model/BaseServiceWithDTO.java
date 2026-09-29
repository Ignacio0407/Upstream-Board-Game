/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
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