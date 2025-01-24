package com.jit;

import org.apache.commons.io.FileUtils;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Jit {
    private static final String JIT_DIR = ".jit";
    private static final String OBJECTS_DIR = JIT_DIR + "/objects";
    private static final String HEAD_FILE = JIT_DIR + "/HEAD";
    
    private final Path rootPath;
    private StagingArea stagingArea;

    public Jit() {
        this.rootPath = Paths.get(System.getProperty("user.dir"));
        loadState();
    }

    private void loadState() {
        try {
            if (Files.exists(rootPath.resolve(JIT_DIR))) {
                loadStagingArea();
            } else {
                stagingArea = new StagingArea();
            }
        } catch (Exception e) {
            System.err.println("Error loading Jit state: " + e.getMessage());
        }
    }

    public void init() throws IOException {
        Files.createDirectories(rootPath.resolve(OBJECTS_DIR));
        saveState();
        System.out.println("Initialized empty Jit repository in " + rootPath.resolve(JIT_DIR));
    }

    public void add(String pattern) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        Files.walk(rootPath)
            .filter(Files::isRegularFile)
            .filter(p -> !p.startsWith(rootPath.resolve(JIT_DIR)))
            .filter(p -> matcher.matches(rootPath.relativize(p)))
            .forEach(this::stageFile);
        saveState();
    }

    private void stageFile(Path file) {
        try {
            byte[] content = Files.readAllBytes(file);
            String relativePath = rootPath.relativize(file).toString();
            stagingArea.addFile(relativePath, content);
            System.out.println("Added " + relativePath);
        } catch (IOException e) {
            System.err.println("Error staging file: " + e.getMessage());
        }
    }

    public void commit(String message) throws IOException {
        if (stagingArea.isEmpty()) {
            System.out.println("No changes staged to commit");
            return;
        }

        Commit newCommit = createNewCommit(message);
        saveCommit(newCommit);
        updateHead(newCommit);
        stagingArea.clear();
        saveState();
        System.out.println("[" + newCommit.getId() + "] " + message);
    }

    private Commit createNewCommit(String message) throws IOException {
        Map<String, byte[]> commitFiles = new HashMap<>();
        Commit headCommit = getHeadCommit();
        if (headCommit != null) {
            commitFiles.putAll(headCommit.getFiles());
        }
        commitFiles.putAll(stagingArea.getStagedFiles());
        return new Commit(message, headCommit, commitFiles);
    }
}