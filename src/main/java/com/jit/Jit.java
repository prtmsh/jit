package com.jit;

import org.apache.commons.io.FileUtils;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class Jit {
    private static final String JIT_DIR = ".jit";
    private static final String OBJECTS_DIR = JIT_DIR + "/objects";
    private static final String HEAD_FILE = JIT_DIR + "/HEAD";
    private static final String STAGING_FILE = JIT_DIR + "/STAGING";
    
    private final Path rootPath;
    private StagingArea stagingArea;
    private Commit headCommit;

    public Jit() throws IOException, ClassNotFoundException {
        this.rootPath = Paths.get(System.getProperty("user.dir"));
        initialize();
    }

    private void initialize() throws IOException, ClassNotFoundException {
        if (Files.exists(rootPath.resolve(JIT_DIR))) {
            loadStagingArea();
            loadHeadCommit();
        } else {
            stagingArea = new StagingArea();
        }
    }

    public void init() throws IOException {
        if (Files.exists(rootPath.resolve(JIT_DIR))) {
            System.out.println("Jit repository already exists");
            return;
        }
        
        Files.createDirectories(rootPath.resolve(OBJECTS_DIR));
        saveState();
        System.out.println("Initialized empty Jit repository in " + rootPath.resolve(JIT_DIR));
    }

    public void add(String pattern) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> !p.startsWith(rootPath.resolve(JIT_DIR)))
                 .filter(p -> matcher.matches(rootPath.relativize(p)))
                 .forEach(this::stageFile);
        }
        
        saveState();
    }

    private void stageFile(Path file) {
        try {
            byte[] content = Files.readAllBytes(file);
            String relativePath = rootPath.relativize(file).toString();
            stagingArea.addFile(relativePath, content);
            System.out.println("Added " + relativePath);
        } catch (IOException e) {
            System.err.println("Error staging file: " + file + " - " + e.getMessage());
        }
    }

    public void commit(String message) throws IOException {
        if (stagingArea.isEmpty()) {
            System.out.println("No changes staged to commit");
            return;
        }

        Map<String, byte[]> newFiles = new HashMap<>();
        if (headCommit != null) {
            newFiles.putAll(headCommit.getFiles());
        }
        newFiles.putAll(stagingArea.getStagedFiles());

        Commit newCommit = new Commit(message, headCommit, newFiles);
        saveCommit(newCommit);
        headCommit = newCommit;
        stagingArea.clear();
        saveState();
        System.out.println("[" + newCommit.getId() + "] " + message);
    }

    public void checkout(String commitId) throws IOException, ClassNotFoundException {
        Commit target = loadCommit(commitId);
        if (target == null) {
            System.out.println("Commit does not exist: " + commitId);
            return;
        }

        // Clear working directory
        FileUtils.cleanDirectory(rootPath.toFile());
        
        // Restore files from commit
        for (Map.Entry<String, byte[]> entry : target.getFiles().entrySet()) {
            Path filePath = rootPath.resolve(entry.getKey());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, entry.getValue());
        }

        headCommit = target;
        stagingArea.clear();
        saveState();
        System.out.println("Checked out to commit: " + commitId);
    }

    public void status() {
        System.out.println("Current branch: main");
        System.out.println("\nStaged changes:");
        stagingArea.getStagedFiles().keySet().forEach(f -> System.out.println("  " + f));
        
        System.out.println("\nLatest commit: " + 
            (headCommit != null ? "[" + headCommit.getId() + "] " + headCommit.getMessage() : "None"));
    }

    private void saveCommit(Commit commit) throws IOException {
        Path commitFile = rootPath.resolve(OBJECTS_DIR).resolve(commit.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(commitFile))) {
            oos.writeObject(commit);
        }
    }

    private Commit loadCommit(String commitId) throws IOException, ClassNotFoundException {
        Path commitFile = rootPath.resolve(OBJECTS_DIR).resolve(commitId);
        if (!Files.exists(commitFile)) return null;
        
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(commitFile))) {
            return (Commit) ois.readObject();
        }
    }

    private void saveState() throws IOException {
        saveStagingArea();
        saveHeadCommit();
    }

    private void saveStagingArea() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
            Files.newOutputStream(rootPath.resolve(STAGING_FILE)))) {
            oos.writeObject(stagingArea);
        }
    }

    private void loadStagingArea() throws IOException, ClassNotFoundException {
        if (!Files.exists(rootPath.resolve(STAGING_FILE))) {
            stagingArea = new StagingArea();
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
            Files.newInputStream(rootPath.resolve(STAGING_FILE)))) {
            stagingArea = (StagingArea) ois.readObject();
        }
    }

    private void saveHeadCommit() throws IOException {
        if (headCommit == null) return;
        
        Files.write(rootPath.resolve(HEAD_FILE), 
            headCommit.getId().getBytes(), 
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void loadHeadCommit() throws IOException, ClassNotFoundException {
        if (!Files.exists(rootPath.resolve(HEAD_FILE))) return;
        
        String headId = new String(Files.readAllBytes(rootPath.resolve(HEAD_FILE)));
        headCommit = loadCommit(headId.trim());
    }
}