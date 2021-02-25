package org.killercarrots.evcharge.models;

import java.util.HashSet;
import java.util.Set;

public class UserStatusDetailsResponse extends MyAbstractObj {

    private String username, status, token;
    private Set<String> roles;

    public UserStatusDetailsResponse(User user) {
        this.username = user.getUsername();
        this.token = user.getToken();
        if(this.token == null) {
            this.status = "offline";
        } else {
            this.status = "online";
        }
        this.roles = new HashSet<String>();
        for(Role r : user.getRoles()) {
            switch (r.getName()) {
                case ROLE_USER: {
                    this.roles.add("User");
                    break;
                }
                case ROLE_OPERATOR: {
                    this.roles.add("Operator");
                    break;
                }
                case ROLE_ADMIN: {
                    this.roles.add("Admin");
                }
            }
        }
    }

	@Override
	public String toCsv() {
		String ret = "Username,CurrentStatus,Token,Roles\n"+
                        this.username+","+this.status+","+this.token+",\"[";
        for(String role : this.roles) {
            ret = ret+role+",";
        }
        ret = ret.substring(0,ret.length()-1)+"]\"";
		return ret;
	}

	@Override
	public String toJson() {
		String ret = "{\"Username\":\""+this.username+"\","+
                        "\"CurrentStatus\":\""+this.status+"\","+
                        "\"Token\":\""+this.token+"\","+
                        "\"Roles\":[";
            for(String role : this.roles) {
                ret = ret+"\""+role+"\",";
            }
            ret = ret.substring(0,ret.length()-1);
            ret = ret +"]}";
		return ret;
	}
}
