import { useLocation, useNavigate } from "react-router-dom";
import { SubmitHandler } from "react-hook-form";
import tokenService from "../../services/token.service.ts";
import "../../static/css/admin/adminPage.css";
import { EditFormGenerator } from "../../components/Form/EditFormGenerator.tsx";
import { post, put } from "../../util/fetchers.ts";
import { Authority, UserList, emptyUserList } from "../../interfaces/User.ts";

const jwt = tokenService.getLocalAccessToken();

export default function UserEditAdmin() {
  const { state } = useLocation();
  const user = state?.user as UserList || emptyUserList;
  const actualUser = tokenService.getUser();
  const isEdit = Boolean(user?.id);
  const navigate = useNavigate();

  const onSubmit: SubmitHandler<UserList> = async (data) => {
    const request = isEdit ? put(`/api/v1/users/listing/${user.id}`, jwt, data) : post(`/api/v1/users`, jwt, data);
    request.then((response) => response.json())
      .then((updated) => {
        navigate("/users", { state: { updated } });
      })
      .catch((error) => alert(error instanceof Error ? error.message : String(error)));
  };

  return (
    <div className="auth-page-container">
      {<h2 className="text-center">{isEdit ? "Edit User" : "Create User"}</h2>}
      <div className="auth-form-container">
        <EditFormGenerator<UserList> entityToEdit={user} onSubmit={onSubmit} submitText="Save" loadingText="Saving..."
          selectOptions={{ authority: Object.values(Authority) }} actualUser={actualUser} requiredAuth="ADMIN"
        />
      </div>
    </div>
  );
}