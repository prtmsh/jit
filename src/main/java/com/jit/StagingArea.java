package com.jit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StagingArea implements Serializable {
    private final Map<String, byte[]> stagedFiles = new HashMap<>();

    public void addFile(String path, byte[] content) {
        stagedFiles.put(path, content);
    }

    public void clear() {
        stagedFiles.clear();
    }

    public boolean isEmpty() {
        return stagedFiles.isEmpty();
    }

    public Map<String, byte[]> getStagedFiles() {
        return new HashMap<>(stagedFiles);
    }
}