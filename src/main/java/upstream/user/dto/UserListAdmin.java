package upstream.user.dto;

import upstream.user.Authority;
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