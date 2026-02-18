package es.us.dp1.l4_01_24_25.upstream.user.DTO;

import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserListAdmin {

    Integer id;
    String name;
    Authority authority;

    public UserListAdmin(Integer id, String name, Authority authority) {
        this.id = id;
        this.name = name;
        this.authority = authority;
    }
}