import { JSX, useState } from "react";
import { Link } from "react-router-dom";
import { Button, ButtonGroup, Table } from "reactstrap";
import tokenService from "../../services/token.service.ts";
import "../../static/css/admin/adminPage.css";
import deleteFromList from "../../util/deleteFromList.ts";
import getErrorModal from "../../util/getErrorModal.tsx";
import useFetchState from "../../util/useFetchState.ts";
import SearchBar from "../../components/SearchBar/SearchBar.tsx"
import User from '../../interfaces/User.ts'

const jwt = tokenService.getLocalAccessToken();

export default function UserListAdmin() {
  const [message, setMessage] = useState<string>("");
  const [visible, setVisible] = useState<boolean>(false);
  const [users, setUsers] = useFetchState<User[]>([],`/api/v1/users`,jwt,setMessage,setVisible);
  const [filtered, setFiltered] = useState<User[]>([]);
  const [alerts, setAlerts] = useState<any[]>([]); // Temporal

  function userList(usersToList:User[]) : JSX.Element[] {
  return usersToList.map((user) => {
    return (
      <tr key={user.id}>
        <td>{user.username}</td>
        <td>{user.authority.authority}</td>
        <td>
          <ButtonGroup>
            <Button
              size="sm"
              color="primary"
              aria-label={"edit-" + user.id}
              tag={Link}
              to={"/users/" + user.id}
            >
              Edit
            </Button>
            <Button
              size="sm"
              color="danger"
              aria-label={"delete-" + user.id}
              onClick={() =>
                deleteFromList(
                  `/api/v1/users/${user.id}`,
                  user.id,
                  [users, setUsers],
                  [alerts, setAlerts],
                  setMessage,
                  setVisible
                )
              }
            >
              Delete
            </Button>
          </ButtonGroup>
        </td>
      </tr>
    );
  });
  }
  const modal = getErrorModal(setVisible, visible, message);

  return (
    <div className="admin-page-container">
    <SearchBar data={users} setFiltered={setFiltered} placeholder={"Search users"} defaultCaseSensitive={true}/>
      <h1 className="text-center">Users</h1>
      {alerts.map((a) => a.alert)}
      {modal}
      <Button color="success" tag={Link} to="/users/new">
        Add User
      </Button>
      <div>
        <Table aria-label="users" className="mt-4">
          <thead>
            <tr>
              <th>Username</th>
              <th>Authority</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>{filtered ? userList(filtered) : userList(users)}</tbody>
        </Table>
      </div>
    </div>
  );
}
