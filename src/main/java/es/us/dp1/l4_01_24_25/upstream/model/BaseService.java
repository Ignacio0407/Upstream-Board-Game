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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

import java.util.ArrayList;
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
            return "Resource";
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
    public void deleteAll(List<T> entities) {
        this.repository.deleteAll(entities);
    }

    @Transactional
    public T update(ID id, @Valid T updatedEntity) {
        T existingEntity = this.findById(id);
        this.updateEntityFields(existingEntity, updatedEntity); // Protected method to implement in each service
        return this.save(existingEntity);
    }

    @Transactional
    public List<T> updateSome(@Valid List<ID> ids, @Valid List<T> updatedEntities) {
        if (ids.size() != updatedEntities.size())
            throw new IllegalArgumentException("Ids and entities size mismatch");
        List<T> toSave = new ArrayList<>();
        Integer index = 0;
        for (T updated : updatedEntities) {
            ID id = ids.get(index);
            T existing = this.findById(id);
            updateEntityFields(existing, updated);
            toSave.add(existing);
            index++;
        }
        return this.repository.saveAll(toSave);
    }

    protected T updateEntityFields(@Valid T existing, @Valid T updated) {
        throw new UnsupportedOperationException("updateEntityFields no implementado");
    }

}
