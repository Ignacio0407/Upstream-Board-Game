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