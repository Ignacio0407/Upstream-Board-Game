package es.us.dp1.l4_01_24_25.upstream.model;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

import java.util.LinkedList;
import java.util.List;

public abstract class BaseService<T, ID> {
    protected final CrudRepository<T, ID> repository;

    protected BaseService(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return (List<T>) repository.findAll();
    }

    @Transactional(readOnly = true)
    public T findById(ID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public <D> List<D> findList(List<D> list) {
        if(list == null) throw new ResourceNotFoundException("List not found");
        else return list;
    }

    @Transactional
    public T save(@Valid T entity) {
        return repository.save(entity);
    }

    @Transactional
    public List<T> saveSome(List<T> entities) {
        List<T> failedToSave = new LinkedList<>();
        entities.forEach(jugador -> {
            try {
                this.save(jugador);
            } catch (DataAccessException e) {
                failedToSave.add(jugador);
            }
        });
		return failedToSave;
    }

    @Transactional
    public void delete(ID id) {
        T entity = this.findById(id);
        repository.delete(entity);
    }

    @Transactional
    public T update(ID id, @Valid T updatedEntity) {
        T existingEntity = this.findById(id);

        updateEntityFields(existingEntity, updatedEntity); // Protected method to implement in each service

        return this.save(existingEntity);
    }

    protected void updateEntityFields(@Valid T existing, @Valid T updated) {
        throw new UnsupportedOperationException("updateEntityFields no implementado");
    }

}
