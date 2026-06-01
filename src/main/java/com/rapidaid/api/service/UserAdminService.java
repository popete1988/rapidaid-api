package com.rapidaid.api.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserAdminService {

    public List<Map<String, Object>> listUsers() throws Exception {
        List<Map<String, Object>> users = new ArrayList<>();
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        for (ExportedUserRecord user : page.iterateAll()) {
            Map<String, Object> u = new HashMap<>();
            u.put("uid", user.getUid());
            u.put("email", user.getEmail() != null ? user.getEmail() : "");
            u.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "");
            u.put("disabled", user.isDisabled());
            u.put("createdAt", user.getUserMetadata().getCreationTimestamp());
            u.put("lastSignIn", user.getUserMetadata().getLastSignInTimestamp());
            users.add(u);
        }
        users.sort(Comparator.comparingLong(u -> -(long) u.getOrDefault("createdAt", 0L)));
        return users;
    }

    public void setUserDisabled(String uid, boolean disabled) throws Exception {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(disabled);
        FirebaseAuth.getInstance().updateUser(request);
    }

    public void deleteUser(String uid) throws Exception {
        FirebaseAuth.getInstance().deleteUser(uid);
    }
}
