package org.killercarrots.evcharge.models;

import lombok.Getter;

@Getter
public class MessageResponse extends MyAbstractObj {
    private String message;
    private String field;

    public MessageResponse(String message, String field){
        this.message = message;
        this.field = field;
    }

    @Override
    public String toCsv() {
        return field + "\n" + message;
    }

    @Override
    public String toJson() {
        return "{\""+field+"\":\""+message+"\"}";
    }
}
