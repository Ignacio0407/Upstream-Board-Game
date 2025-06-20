package es.us.dp1.l4_01_24_25.upstream.model;

import java.util.List;

public interface EntityMapper<T, D> {
    D toDTO(T entity);
    T toEntity(D dto);
    List<D> toDTOList(List<T> entities);
    List<T> toEntityList(List<D> dtos);
}