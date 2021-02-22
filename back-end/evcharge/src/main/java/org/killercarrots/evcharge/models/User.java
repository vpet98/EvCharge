package org.killercarrots.evcharge.models;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "users")
public class User {
    @MongoId
    private String username;
    private String password;
    //private String FirstName;
    //private String LastName;
    //private String email;
    //User roles ({User, Moderator, Admin, etc})
    private Set<Role> roles = new HashSet<>();

    // Active token (null if no session is active)
    // We assume only one client can have access
    // to a specific user at any time
    // (only one active token pre user allowed)
    // (needed for get user details/admin endpoint)
    private String token;

    public User(){
        /***initialization_is_done_through_setters***/
    }

}
