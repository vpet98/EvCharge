package org.killercarrots.evcharge.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "roles")
public class Role {
    @Id
    private int id;
    private ERole name;
    private String details;
    public Role(){

    }

    public Role(int id, ERole name){
        this.id = id;
        this.name = name;
    }
}
