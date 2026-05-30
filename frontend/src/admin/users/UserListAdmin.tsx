import { useState } from "react";
import { Button } from "reactstrap";
import tokenService from "../../services/token.service.ts";
import useFetchState from "../../util/useFetchState.ts";
import deleteFromList from "../../util/deleteFromList.ts";
import ErrorModal from "../../util/ErrorModal.tsx";
import ListGenerator from "../../components/ListGenerator/ListGenerator.tsx";
import { UserList } from "../../interfaces/User.ts";
import "../../static/css/admin/adminPage.css";
import { patch } from "../../util/fetchers.ts";

export default function UserListAdmin() {

  const jwt = tokenService.getLocalAccessToken();
  const [message, setMessage] = useState("");
  const [visible, setVisible] = useState(false);
  const [alerts, setAlerts] = useState<any[]>([]);
  const [users, setUsers] = useFetchState<UserList[]>([], "/api/v1/users/listing", jwt, setMessage, setVisible);
  const actualUser = tokenService.getUser();

  function renderUser(user: UserList) {
    return (
      <>
        <td>{user.name}</td>
        <td>{user.authority}</td>
      </>
    );
  }

  const updateUserAuthority = async (u: UserList) => {
  try {
    const response = await patch(`/api/v1/users/listing/${u.id}`, jwt);
    if (!response.ok) {
        throw new Error("Error updating authority");
    }
    const updatedUser = await response.json();
    setUsers((prevUsers) =>
      prevUsers.map((user) =>
        user.id === u.id
          ? { ...user, authority: updatedUser.authority }
          : user
      )
    );
  } catch (error) {
    setMessage("Error updating authority");
    setVisible(true);
  }
};

  return (
    <div className="admin-page-container">
      <h1 className="text-center">Users</h1>

      {alerts.map(a => a.alert)}
      <ErrorModal visible={visible} setVisible={setVisible} message={message} />

      {users && <ListGenerator<UserList>
        data={users}
        actualUser={actualUser}
        idKey="id"
        columns={["name", "authority"]}
        customLabels={{name: "Username", authority: "Authority"}}
        customRender={renderUser}

        /* 🔐 Security */
        requiredAuth="ADMIN"

        /* ✏️ Edit */
        editCondition={true}
        editButton={(u) => (<Button size="sm" color="primary" onClick={() => updateUserAuthority(u)}> Change authority </Button>)}

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
