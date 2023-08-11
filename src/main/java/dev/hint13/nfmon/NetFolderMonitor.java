package dev.hint13.nfmon;

import jcifs.CIFSContext;
import jcifs.Credentials;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import javax.swing.*;
import java.io.Closeable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class NetFolderMonitor implements Closeable{
    private final int DELAY = 1000;
    private final String basePath;
    private final SmbFile root;
    private final Gui gui;
    private final Repository repo;
    private Timer timer;

    public NetFolderMonitor(String path) {
        this.basePath = path;
        gui = new Gui(path);

        SingletonContext baseContext = SingletonContext.getInstance();
        Credentials credentials = new NtlmPasswordAuthenticator(null,
                System.getenv("NFMON_USER"), System.getenv("NFMON_PWD"));
        CIFSContext testCtx = baseContext.withCredentials(credentials);

        try {
            root = new SmbFile(path, testCtx);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        repo = new Repository(root);
        run();
    }

    private void run() {
        gui.setMenu(new ArrayList<SmbFile>());
        timer = new Timer(DELAY, t -> {
            List<SmbFile> changes = null;
            try {
                changes = repo.scanChanges(root);
            } catch (SmbException e) {
                System.out.println(e.getMessage());
            }
            if (changes != null && !changes.isEmpty()) {
                gui.setMenu(changes);
                if (changes.size() < 2) {
                    gui.showNotification("Info", formatMessage(changes.get(0)));
                } else {
                    gui.showNotification("Info",formatMessage(changes.get(0)) +
                            ", also " + (changes.size() - 1) + "+ changes in folder.");
                }
            }
        });
        timer.start();
    }

    private String formatMessage(SmbFile file) {
        return file.getPath().substring(basePath.length());
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    @Override
    public void close() {
        timer.stop();
        root.close();
    }
}
