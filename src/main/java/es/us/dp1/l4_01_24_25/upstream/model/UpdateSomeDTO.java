package es.us.dp1.l4_01_24_25.upstream.model;

import java.util.List;

public class UpdateSomeDTO<ID, T> {

    private List<ID> ids;
    private List<T> entities;

    public List<ID> getIds() { return ids; }
    public void setIds(List<ID> ids) { this.ids = ids; }

    public List<T> getEntities() { return entities; }
    public void setEntities(List<T> entities) { this.entities = entities; }

}