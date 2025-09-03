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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseRestControllerWithDTO<T extends BaseEntity, D, ID> {

    protected final BaseServiceWithDTO<T, D, ID> service;

    protected BaseRestControllerWithDTO(BaseServiceWithDTO<T, D, ID> service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<D>> findAll() {
        return ResponseEntity.ok(service.findAllAsDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<D> findById(@PathVariable ID id) {
        return ResponseEntity.ok(service.findByIdAsDTO(id));
    }

    @PostMapping
    public ResponseEntity<D> save(@RequestBody D dto) {
        return new ResponseEntity<>(service.saveAsDTO(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<D> update(@PathVariable ID id, @RequestBody D dto) {
        return ResponseEntity.ok(service.updateAsDTO(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}