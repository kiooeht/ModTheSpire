package com.evacipated.cardcrawl.modthespire.steam;

import com.evacipated.cardcrawl.modthespire.MinimalModInfo;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.ui.ModDownloadWindow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SteamWorkshopRunner
{
    private static Process apiProcess;

    // Output to process
    private static PrintWriter processWriter;

    // Input from process
    private static Scanner scanner;

    private static void startAPI() throws IOException
    {
        if (apiProcess != null) {
            // API is already active
            return;
        }

        String apiPath = SteamWorkshop.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        apiPath = URLDecoder.decode(apiPath,  "utf-8");
        apiPath = new File(apiPath).getPath();

        ProcessBuilder pb = new ProcessBuilder(
            SteamSearch.findJRE(),
            "-cp", apiPath + File.pathSeparatorChar + ModTheSpire.STS_JAR,
            "com.evacipated.cardcrawl.modthespire.steam.SteamWorkshop"
        ).redirectError(ProcessBuilder.Redirect.INHERIT);

        apiProcess = pb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(apiProcess::destroy));

        OutputStream outputStreamWriter = apiProcess.getOutputStream();
        processWriter = new PrintWriter(outputStreamWriter, true);

        InputStream inputStreamReader = apiProcess.getInputStream();
        scanner = new Scanner(inputStreamReader);
    }

    public static List<SteamSearch.WorkshopInfo> findWorkshopInfos()
    {
        List<SteamSearch.WorkshopInfo> workshopInfos = new ArrayList<>();
        try {
            System.out.println("Searching for Workshop items...");
            startAPI();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            processWriter.println("workshop_infos");

            String result = scanner.nextLine();
            SteamData steamData = gson.fromJson(result, SteamData.class);

            System.out.println(gson.toJson(steamData));

            ModTheSpire.LWJGL3_ENABLED = ModTheSpire.LWJGL3_ENABLED || steamData.steamDeck;
            for (SteamSearch.WorkshopInfo info : steamData.workshopInfos) {
                if (!info.hasTag("tool") && !info.hasTag("tools")) {
                    workshopInfos.add(info);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workshopInfos;
    }

    public static Pair<List<Long>, List<Long>> downloadMods(Component owner, List<MinimalModInfo> toDownload){
        try{
            System.out.println("Attempting to download following items:");
            for(MinimalModInfo item : toDownload){
                System.out.println(item.getModId());
            }

            //Initialize
            startAPI();
            Gson gson = new Gson();

            //Request download
            List<Long> modIdsToDownload = new ArrayList<>();
            for(MinimalModInfo item : toDownload){
                modIdsToDownload.add(item.getSteamId());
            }

            processWriter.println("subscribe_and_download");
            processWriter.println(gson.toJson(modIdsToDownload));

            //Create a new download window
            ModDownloadWindow downloadWindow = new ModDownloadWindow(owner);

            //Start the update listener in the background so that UI can update;
            final Pair<List<Long>, List<Long>>[] resultVal = new Pair[1];
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    //Wait for result
                    while(resultVal[0] == null){
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String result = scanner.nextLine();
                        if(result.startsWith("downloading_")){
                            String downloadingItem = result.split("downloading_")[1];
                            Optional<MinimalModInfo> downloadingMod = toDownload.stream().filter(item -> item.getSteamId().toString().equals(downloadingItem)).findFirst();
                            downloadingMod.ifPresent(minimalModInfo -> downloadWindow.setDownloadingMod(minimalModInfo.getModId()));
                        }
                        else if(result.startsWith("download_perc_")){
                            String perc = result.split("download_perc_")[1];
                            downloadWindow.setDownloadPercentage(Integer.parseInt(perc));
                        }
                        else{
                            try{
                                resultVal[0] = gson.fromJson(result, new TypeToken<Pair<List<Long>, List<Long>>>(){}.getType());
                            }catch (Exception e){
                                System.err.println("Encountered an error msg when downloading an item: " + result);
                                e.printStackTrace();
                            }
                        }
                    }

                    return null;
                }

                @Override
                protected void done() {
                    downloadWindow.dispose();
                }
            }.execute();

            //Hang until disposed
            downloadWindow.setVisible(true);

            ModTheSpire.refreshMods();

            return resultVal[0];
        }catch (IOException e){
            e.printStackTrace();
        }

        return new Pair<>(new ArrayList<>(), new ArrayList<>());
    }

    public static void stopAPI()
    {
        if (apiProcess == null) {
            // API not active
            return;
        }

        if (apiProcess.isAlive()) {
            try {
                processWriter.println("quit");
                if (!apiProcess.waitFor(100, TimeUnit.MILLISECONDS)) {
                    apiProcess.destroy();
                }
            } catch (Exception ignored) {
                apiProcess.destroy();
            }
        }
        apiProcess = null;

        if(processWriter != null) {
            try{
                processWriter.close();
            }catch (Exception ignored){}
        }
        processWriter = null;

        if(scanner != null) {
            try{
                scanner.close();
            }catch (Exception ignored){}
        }
        scanner = null;
    }
}
