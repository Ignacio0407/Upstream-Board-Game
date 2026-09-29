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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, I> {
    protected JpaRepository<T, I> repository;
    protected final String entityName;

    protected BaseService(JpaRepository<T, I> repository) {
        this.repository = repository;
        this.entityName = extractEntityName(repository);
    }

    public String extractEntityName(JpaRepository<T, I> repository) {
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
        return this.repository.findAll();
    }

    @Transactional(readOnly = true)
    public T findById(I id) {
        return this.findOrResourceNotFoundException(this.repository.findById(id), "ID", id);
    }

    @Transactional
    public T save(@Valid T entity) {
        return this.repository.save(entity);
    }

    @Transactional
    public List<T> saveAll(List<T> entities) {
        return this.repository.saveAll(entities);
    }

    @Transactional
    public void deleteById(I id) {
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
    public T update(I id, @Valid T updatedEntity) {
        T existingEntity = this.findById(id);
        this.updateEntityFields(existingEntity, updatedEntity); // Protected method to implement in each service
        return this.save(existingEntity);
    }

    @Transactional
    public List<T> updateSome(@Valid List<I> ids, @Valid List<T> updatedEntities) {
        if (ids.size() != updatedEntities.size())
            throw new IllegalArgumentException("Ids and entities size mismatch");
        List<T> toSave = new ArrayList<>();
        Integer index = 0;
        for (T updated : updatedEntities) {
            I id = ids.get(index);
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
