package org.killercarrots.evcharge.models;

import lombok.Getter;

@Getter
public class SessionsUploadResponse extends MyAbstractObj {

    private int uploaded, imported;
    private long total;

    public SessionsUploadResponse(int uploaded, int imported, long total) {
        this.uploaded = uploaded;
        this.imported = imported;
        this.total = total;
    }

    @Override
    public String toCsv() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toJson() {
        String ret = "{\"SessionsInUploadedFile\":"+String.valueOf(this.uploaded)+","+
                        "\"SessionsImported\":"+String.valueOf(this.imported)+","+
                        "\"TotalSessionsInDatabase\":"+String.valueOf(this.total)+"}";
        return ret;
    }
    
}
