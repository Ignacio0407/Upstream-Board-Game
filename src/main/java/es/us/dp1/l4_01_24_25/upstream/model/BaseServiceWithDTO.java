package es.us.dp1.l4_01_24_25.upstream.model;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

public abstract class BaseServiceWithDTO<T, D, ID> extends BaseService<T, ID> {

    protected final EntityMapper<T, D> mapper;

    protected BaseServiceWithDTO(CrudRepository<T, ID> repository, EntityMapper<T, D> mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<D> findAllAsDTO() {
        return mapper.toDTOList(this.findAll());
    }

    @Transactional(readOnly = true)
    public D findByIdAsDTO(ID id) {
        return mapper.toDTO(this.findById(id));
    }

    @Transactional
    public D saveAsDTO(@Valid D dto) {
        T savedEntity = this.save(mapper.toEntity(dto));
        return mapper.toDTO(savedEntity);
    }

    @Transactional
    public List<D> saveSomeAsDTO(List<D> dtos) {
        List<T> entities = mapper.toEntityList(dtos);
        List<T> failed = super.saveSome(entities);
        return mapper.toDTOList(failed);
    }

    @Transactional
    public D updateAsDTO(ID id, @Valid D dto) {
        T updated = this.update(id, mapper.toEntity(dto));
        return mapper.toDTO(updated);
    }

    
}