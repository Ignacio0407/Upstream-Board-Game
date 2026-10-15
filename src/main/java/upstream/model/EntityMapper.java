package upstream.model;

import java.util.List;

public interface EntityMapper<T, D> {
    D toDTO(T entity);
    T toEntity(D dto);
    List<D> toDTOList(List<T> entities);
    List<T> toEntityList(List<D> dtos);
}