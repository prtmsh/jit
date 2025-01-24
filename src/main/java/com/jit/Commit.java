package com.jit;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Commit implements Serializable {
    private final String id;
    private final String message;
    private final Date timestamp;
    private final Commit parent;
    private final Map<String, byte[]> files;

    public Commit(String message, Commit parent, Map<String, byte[]> files) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.message = message;
        this.timestamp = new Date();
        this.parent = parent;
        this.files = new HashMap<>(files);
    }

    public String getId() { return id; }
    public String getMessage() { return message; }
    public Date getTimestamp() { return timestamp; }
    public Commit getParent() { return parent; }
    public Map<String, byte[]> getFiles() { return new HashMap<>(files); }
}