package org.killercarrots.evcharge.errorHandling;

import org.killercarrots.evcharge.GeneralController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ApiError {
    private String timestamp;
    private int status;
    private String error;
    private String message;

    public ApiError(int status, String error, String message){
        this.timestamp = GeneralController.formatter.format(System.currentTimeMillis());
        this.status = status;
        this.error = error;
        this.message = message;
    }
    public String toJson() {
        String ret = "{\"timestamp\":\""+this.timestamp+"\","+
                        "\"status\":"+this.status+","+
                        "\"error\":\""+this.error+"\","+
                        "\"message\":\""+this.message+"\"}";
        return ret;
    }
    public ResponseEntity<String> buildErrorResponse(HttpStatus status){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>(this.toJson(),headers, status);
    }
}