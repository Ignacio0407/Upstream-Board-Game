package es.us.dp1.l4_01_24_25.upstream.model;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseService<T, ID> {
    protected JpaRepository<T, ID> repository;
    protected final String entityName;

    protected BaseService(JpaRepository<T, ID> repository) {
        this.repository = repository;
        this.entityName = extractEntityName(repository);
    }

    public String extractEntityName(JpaRepository<T, ID> repository) {
        try {
            return ((Class<?>) ((java.lang.reflect.ParameterizedType)
                repository.getClass().getInterfaces()[0].getGenericInterfaces()[0])
                .getActualTypeArguments()[0]).getSimpleName();
        } catch (Exception e) {
            return "Resource"; // Fallback elegante si falla la reflexión
        }
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return (List<T>) repository.findAll();
    }

    @Transactional(readOnly = true)
    public T findById(ID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(this.entityName + " not found with ID: " + id));
    }

    @Transactional
    public T save(@Valid T entity) {
        return repository.save(entity);
    }

    @Transactional
    public List<T> saveAll(List<T> entity) {
        return (List<T>) repository.saveAll(entity);
    }

    @Transactional
    public List<T> saveSome(List<T> entities) {
        List<T> failedToSave = new ArrayList<>();
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
        repository.deleteById(id);
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
