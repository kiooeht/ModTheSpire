package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.steam.SteamWorkshopRunner;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * All utility methods regarding importing.
 * Importing is a lengthy process. First, we need to check what mods the client is missing. For each missing mod,
 * we need to download it from steam if possible. After that, we need to reload the mod infos from scratch to account for new mods.
 */
public class ImportUtils {
    /**
     * Reconciles the client's modlist with the one provided as a parameter, downloading what could be downloaded.
     * @param minimalModInfos : Mod infos to reconcile with.
     * @return : List of all mods that were successfully reconciled.
     */
    public static List<File> reconcileMods(Component owner, List<MinimalModInfo> minimalModInfos){
        // Identify mods that are missing and split them into mods we can try and download and mods we can't download.
        List<MinimalModInfo> modsToDownload = new ArrayList<MinimalModInfo>();
        List<MinimalModInfo> missingModsNoDownload = new ArrayList<>();

        for(MinimalModInfo minimalModInfo : minimalModInfos){
            ModInfo foundModInfo = null;

            for(ModInfo modInfo : ModTheSpire.ALLMODINFOS){
                if(modInfo.ID.equals(minimalModInfo.getModId())){
                    foundModInfo = modInfo;
                }
            }

            if(foundModInfo == null){
                if (minimalModInfo.getSteamId() != null) {
                    modsToDownload.add(minimalModInfo);
                } else {
                    missingModsNoDownload.add(minimalModInfo);
                }
            }
        }

        if(!missingModsNoDownload.isEmpty()){
            // For all missing non-downloadable mods notify the user.
            String cantDownloadMsg = "Following mods are missing but can not be downloaded:\n";
            for(MinimalModInfo minimalModInfo : missingModsNoDownload){
                cantDownloadMsg += "• " + minimalModInfo.getModId() + "\n";
            }
            cantDownloadMsg += "\nPlease download those mods manually.";

            JOptionPane.showMessageDialog(owner, cantDownloadMsg, "Warning", JOptionPane.ERROR_MESSAGE);
        }

        if(!modsToDownload.isEmpty()){
            // For all missing downloadable mods, prompt the user to download.
            String downloadMsg = "Following mods are missing:\n";
            for(MinimalModInfo minimalModInfo : modsToDownload){
                downloadMsg += "• " + minimalModInfo.getModId() + "\n";
            }
            downloadMsg += "\nWould you like download them?";

            // Ask the user
            String[] options = new String[]{"Download", "Ignore"};
            int result = JOptionPane.showOptionDialog(
                owner,
                downloadMsg,
                "Success",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]
            );

            if(result == 0){
                //Attempt to download the given mods
                SteamWorkshopRunner.downloadMods(owner, modsToDownload);
            }
        }

        // Now that we've done our best to try and obtain the mods, transfer them to a ModInfo array.
        List<File> reconciledMods = new ArrayList<File>();
        for(MinimalModInfo minimalModInfo : minimalModInfos){
            for(ModInfo modInfo : ModTheSpire.ALLMODINFOS){
                if(modInfo.ID.equals(minimalModInfo.getModId())){
                    try{
                        reconciledMods.add(new File(modInfo.jarURL.toURI()));
                    }catch (Exception e){
                        System.err.println("Failed to failed to get mod file of mod " + minimalModInfo.getModId() + " due to " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        return reconciledMods;
    }
}
