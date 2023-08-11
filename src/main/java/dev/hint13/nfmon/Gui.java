package dev.hint13.nfmon;

import jcifs.smb.SmbFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Gui {
    private String rootFolder;
    private final TrayIcon trayIcon;

    public Gui(String rootFolder) {
        this.rootFolder = getFolderPath(rootFolder);
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/folder.png"));
            trayIcon = new TrayIcon(image, this.rootFolder);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> {
                openInExplorer(rootFolder);
            });
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void showNotification(String title, String message) {
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    public void setMenu(List<SmbFile> files) {
        PopupMenu menu = new PopupMenu();
        MenuItem openFolderMI = new MenuItem("Открыть...");
        openFolderMI.addActionListener(m -> {
            openInExplorer(rootFolder);
        });

        menu.add(openFolderMI);
        menu.addSeparator();

        Menu filesSM = new Menu("Изменения");
        if (files != null && !files.isEmpty()) {
            filesSM.setEnabled(true);
            for (SmbFile file : files) {
                MenuItem mi = new MenuItem(file.getName());
                mi.addActionListener(e -> {
                    openInExplorer(file.getPath());
                });
                filesSM.add(mi);
            }
        } else {
            filesSM.setEnabled(false);
        }

        menu.add(filesSM);
        MenuItem closeAppMI = new MenuItem("Выход");
        closeAppMI.addActionListener(e -> {
            System.exit(0);
        });
        menu.addSeparator();
        menu.add(closeAppMI);

        trayIcon.setPopupMenu(menu);
    }

    private void openInExplorer(String path) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File(getFolderPath(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFolderPath(String path) {
        return path.replace("smb://", "\\\\").replace("/", "\\");
    }

}
