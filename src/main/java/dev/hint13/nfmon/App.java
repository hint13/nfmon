package dev.hint13.nfmon;

import java.io.IOException;

public class App {
    private static final String DEFAULT_FOLDER = "smb://10.38.254.6/portal/!OBMEN!/ФКУ ЦИТОВ/";

    public static void main(String[] args) throws InterruptedException {
        String folder = args.length == 1 ? args[0] : DEFAULT_FOLDER;
        System.out.println("Start monitoring: [" + folder + "]");
        NetFolderMonitor netFolderMonitor = new NetFolderMonitor(folder);
        do {
            Thread.sleep(1000);
        } while (netFolderMonitor.isRunning());
        System.out.println("Good by...");
    }
}
