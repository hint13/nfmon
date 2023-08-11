package dev.hint13.nfmon;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class Repository {
    private final TreeMap<String, SmbFile> filesCache;
    public Repository(SmbFile folder) {
        try {
            if (folder == null || !folder.isDirectory()) {
                throw new RuntimeException("Illegal argument type: need path to exists directory only");
            }
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
        filesCache = new TreeMap<>();
        try {
            scanChanges(folder);
        } catch (SmbException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<SmbFile> scanChanges(SmbFile startFolder) throws SmbException {
        List<SmbFile> list = Arrays.stream(startFolder.listFiles())
                .filter(f -> {
                    boolean isCorrect = false;
                    try {
                        isCorrect = !f.isDirectory() && isFileNewOrModified(f);
                    } catch (SmbException e) {
                        System.out.println(e.getMessage());
                    }
                    addOrUpdateFileOnTree(f);
                    return isCorrect;
                })
                .toList();
        ArrayList<SmbFile> changedFiles = new ArrayList<>(list);
        Arrays.stream(startFolder.listFiles())
                .filter(f -> {
                    try {
                        return f.isDirectory();
                    } catch (SmbException e) {
                        System.out.println(e.getMessage());
                    }
                    return false;
                })
                .forEach(f -> {
                    try {
                        changedFiles.addAll(scanChanges(f));
                    } catch (SmbException e) {
                        System.out.println(e.getMessage());
                    }
                });
        return changedFiles;
    }

    private boolean isFileNewOrModified(SmbFile file) {
        SmbFile inCache = filesCache.get(file.getPath());
        return inCache == null ||
                inCache.getLastModified() < file.getLastModified();
    }

    private void addOrUpdateFileOnTree(SmbFile file) {
        filesCache.put(file.getPath(), file);
    }

    public void printCache() {
        filesCache.keySet().forEach(System.out::println);
    }
}
