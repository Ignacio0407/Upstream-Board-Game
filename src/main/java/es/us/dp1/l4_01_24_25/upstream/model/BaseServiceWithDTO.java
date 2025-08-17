package es.us.dp1.l4_01_24_25.upstream.model;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

public abstract class BaseServiceWithDTO<T, D, ID> extends BaseService<T, ID> {

    protected final EntityMapper<T, D> mapper;
    protected final BaseRepository<T, D, ID> repository;

    protected BaseServiceWithDTO(BaseRepository<T, D, ID> repository, EntityMapper<T, D> mapper) {
        super(repository);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<D> findAllAsDTO() {
        return this.repository.findAllAsDTO();
    }

    @Transactional(readOnly = true)
    public D findByIdAsDTO(ID id) {
        return this.repository.findByIdAsDTO(id).orElseThrow(() -> new ResourceNotFoundException(this.entityName + " not found with ID: " + id));
    }

    @Transactional
    public D saveAsDTO(@Valid D dto) {
        T savedEntity = this.save(mapper.toEntity(dto));
        return this.mapper.toDTO(savedEntity);
    }

    @Transactional
    public List<D> saveSomeAsDTO(List<D> dtos) {
        List<T> entities = mapper.toEntityList(dtos);
        List<T> failed = super.saveSome(entities);
        return this.mapper.toDTOList(failed);
    }

    @Transactional
    public D updateAsDTO(ID id, @Valid D dto) {
        T updated = this.update(id, mapper.toEntity(dto));
        return this.mapper.toDTO(updated);
    }
    
}