package com.evacipated.cardcrawl.modthespire;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class DownloadAndRestarter
{
    private DownloadAndRestarter() {}

    public static final boolean DO_APPEND = false;

    private static String fileNameFromURL(URL url)
    {
        String uri = url.toString();
        return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
    }

    private static void restartApplication() throws URISyntaxException, IOException
    {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(DownloadAndRestarter.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar")) {
            return;
        }

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());
        command.addAll(Arrays.asList(Loader.ARGS));

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }

    public static void downloadOne(URL download) throws IOException
    {
        ReadableByteChannel rbc = Channels.newChannel(download.openStream());
        String fileName = fileNameFromURL(download);
        FileOutputStream fos = new FileOutputStream(Loader.MOD_DIR + fileName, DO_APPEND);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    public static void downloadAndRestart(URL[] downloads) throws IOException, URISyntaxException
    {
        for (URL download : downloads) {
            downloadOne(download);
        }
        restartApplication();
    }

}
