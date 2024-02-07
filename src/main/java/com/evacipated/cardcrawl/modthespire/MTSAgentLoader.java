package com.evacipated.cardcrawl.modthespire;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.file.Path;

class MTSAgentLoader
{
    private static final boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static final boolean isLinux = System.getProperty("os.name").contains("Linux");
    private static final boolean isMac = System.getProperty("os.name").contains("Mac");
    private static final boolean is64Bit = (System.getProperty("os.arch").equals("amd64") || System.getProperty("os.arch").equals("x86_64"));

    private static String mapLibraryName(String libraryName)
    {
        if (isWindows) {
            return libraryName + (is64Bit ? "64.dll" : ".dll");
        }
        if (isLinux) {
            return "lib" + libraryName + (is64Bit ? "64.so" : ".so");
        }
        if (isMac) {
            return "lib" + libraryName + (is64Bit ? "64.dylib" : ".dylib");
        }
        return libraryName;
    }

    static void loadAgent()
    {
        if (ModTheSpire.AGENT_ENABLED) {
            System.out.println("Premain agent enabled.");
            return;
        }
        System.out.print("Loading agent...");
        // Unpack attach library
        try {
            Path tmpDir = ModTheSpire.getTmpDir();
            ModTheSpire.unpackAs("/attach/" + mapLibraryName("attach"), tmpDir.resolve(System.mapLibraryName("attach")));
            try {
                String libraryPath = System.getProperty("java.library.path");
                System.setProperty("java.library.path", tmpDir.toString() + File.pathSeparatorChar + libraryPath);
                Field f = ClassLoader.class.getDeclaredField("sys_paths");
                f.setAccessible(true);
                f.set(null, null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.out.println("Failed to change java.library.path");
                System.out.println(e);
            }
        } catch (IOException e) {
            System.out.println("Failed to unpack attach");
            System.out.println(e);
        }

        // Attach the agent
        try {
            String path = ModTheSpire.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "utf-8");
            File agentJar = new File(path);

            com.ea.agentloader.AgentLoader.loadAgent(agentJar.getAbsolutePath(), null);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Failed.");
            throw new RuntimeException(e);
        }
    }
}
