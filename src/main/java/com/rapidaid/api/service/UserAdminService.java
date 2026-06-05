// Servicio de administración de usuarios de Firebase.
// Se comunica con Firebase Admin SDK para listar, deshabilitar, habilitar
// y eliminar usuarios. Solo se puede usar desde AdminController,
// que comprueba la clave de administración antes de llamar a estos métodos.

package com.rapidaid.api.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserAdminService {

    // Devuelve la lista de todos los usuarios registrados en Firebase Auth,
    // ordenada por fecha de creación (más reciente primero)
    public List<Map<String, Object>> listUsers() throws Exception {
        List<Map<String, Object>> users = new ArrayList<>();

        // Obtiene todos los usuarios de Firebase en páginas (paginación automática)
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        for (ExportedUserRecord user : page.iterateAll()) {
            // Convierte cada usuario a un Map con los campos que necesita el dashboard
            Map<String, Object> u = new HashMap<>();
            u.put("uid",         user.getUid());                                                       // ID único del usuario en Firebase
            u.put("email",       user.getEmail() != null ? user.getEmail() : "");                     // Email del usuario
            u.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "");         // Nombre para mostrar
            u.put("disabled",    user.isDisabled());                                                   // Si la cuenta está deshabilitada
            u.put("createdAt",   user.getUserMetadata().getCreationTimestamp());                       // Fecha de creación (timestamp)
            u.put("lastSignIn",  user.getUserMetadata().getLastSignInTimestamp());                     // Fecha del último acceso
            users.add(u);
        }

        // Ordena los usuarios por fecha de creación, el más reciente primero
        users.sort(Comparator.comparingLong(u -> -(long) u.getOrDefault("createdAt", 0L)));
        return users;
    }

    // Habilita o deshabilita la cuenta del usuario con el UID indicado.
    // disabled=true → la cuenta queda bloqueada (no puede iniciar sesión)
    // disabled=false → la cuenta vuelve a estar activa
    public void setUserDisabled(String uid, boolean disabled) throws Exception {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(disabled);
        FirebaseAuth.getInstance().updateUser(request); // Aplica el cambio en Firebase
    }

    // Elimina permanentemente la cuenta del usuario con el UID indicado.
    // Esta operación no se puede deshacer.
    public void deleteUser(String uid) throws Exception {
        FirebaseAuth.getInstance().deleteUser(uid); // Elimina el usuario de Firebase
    }
}
