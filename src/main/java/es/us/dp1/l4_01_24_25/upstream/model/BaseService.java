package es.us.dp1.l4_01_24_25.upstream.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

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

    public T findOrResourceNotFoundException (Optional<T> entity, String variable, Object id) {
        if (entity.isPresent()) return entity.get(); 
        else throw new ResourceNotFoundException(this.entityName + " not found with " + variable + ": " + id);
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return (List<T>) this.repository.findAll();
    }

    @Transactional(readOnly = true)
    public T findById(ID id) {
        return this.findOrResourceNotFoundException(this.repository.findById(id), "ID", id);
    }

    @Transactional
    public T save(@Valid T entity) {
        return this.repository.save(entity);
    }

    @Transactional
    public List<T> saveAll(List<T> entities) {
        return (List<T>) this.repository.saveAll(entities);
    }

    @Transactional
    public void deleteById(ID id) {
        this.repository.deleteById(id);
    }

    @Transactional
    public void delete(@Valid T entity) {
        this.repository.delete(entity);
    }

    @Transactional
    public void deleteAllInBatch(List<T> entities) {
        this.repository.deleteAllInBatch(entities);
    }

    @Transactional
    public T update(ID id, @Valid T updatedEntity) {
        T existingEntity = this.findById(id);
        this.updateEntityFields(existingEntity, updatedEntity); // Protected method to implement in each service
        return this.save(existingEntity);
    }

    protected void updateEntityFields(@Valid T existing, @Valid T updated) {
        throw new UnsupportedOperationException("updateEntityFields no implementado");
    }

}
