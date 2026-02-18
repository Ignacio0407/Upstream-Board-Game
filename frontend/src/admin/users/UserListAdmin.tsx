import { useState } from "react";
import { Button } from "reactstrap";
import { Link } from "react-router-dom";
import tokenService from "../../services/token.service.ts";
import useFetchState from "../../util/useFetchState.ts";
import deleteFromList from "../../util/deleteFromList.ts";
import getErrorModal from "../../util/getErrorModal.tsx";
import ListGenerator from "../../components/ListGenerator/ListGenerator.tsx";
import { UserList } from "../../interfaces/User.ts";
import "../../static/css/admin/adminPage.css";

const jwt = tokenService.getLocalAccessToken();

export default function UserListAdmin() {

  const [message, setMessage] = useState("");
  const [visible, setVisible] = useState(false);
  const [alerts, setAlerts] = useState<any[]>([]);
  const [users, setUsers] = useFetchState<UserList[]>([], "/api/v1/users/listing", jwt, setMessage, setVisible);
  const actualUser = tokenService.getUser();
  const modal = getErrorModal(setVisible, visible, message);

  function renderUser(user: UserList) {
    return (
      <>
        <td>{user.name}</td>
        <td>{user.authority}</td>
      </>
    );
  }

  return (
    <div className="admin-page-container">
      <h1 className="text-center">Users</h1>

      {alerts.map(a => a.alert)}
      {modal}

      {users && <ListGenerator<UserList>
        data={users}
        actualUser={actualUser}
        idKey="id"
        columns={["name", "authority"]}
        customLabels={{name: "Username", authority: "Authority"}}
        customRender={renderUser}

        /* 🔐 Security */
        requiredAuth="ADMIN"

        /* ➕ Create */
        createButton="/users/new"
        createButtonPosition="top"

        /* ✏️ Edit */
        editCondition={true}
        editButton={(u) => (<Button size="sm" color="primary" tag={Link} to={`/users/${u.id}`} state={{ user: u }}> Edit </Button>)}

        /* 🗑️ Delete */
        deleteCondition={true}
        deleteButton={(u) =>
          deleteFromList(`/api/v1/users`, u.id, [users, setUsers], [alerts, setAlerts], setMessage, setVisible)}

        /* 🔍 SearchBar */
        searchBarPlaceholder="Search users"
      />
      }
    </div>
  );
}
