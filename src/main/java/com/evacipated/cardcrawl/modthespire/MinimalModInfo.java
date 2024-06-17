package com.evacipated.cardcrawl.modthespire;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of ModInfo that contains only essential mod data for user to user transfer.
 */
public class MinimalModInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Id of the mod represented by this mod info. */
    @SerializedName("modid")
    private String modId;

    /** Steam id of the mod represented by this mod info. Can be null if the mod was installed manually. */
    @SerializedName("steamId")
    private Long steamId;

    public MinimalModInfo(ModInfo modInfo){
        modId = modInfo.ID;

        if(modInfo.workshopInfo != null){
            steamId = modInfo.workshopInfo.getID();
        }
    }

    /**
     * @return : Mod id.
     */
    public String getModId() {
        return modId;
    }

    /**
     * @return : Mod steam id.
     */
    public Long getSteamId() {
        return steamId;
    }

    /**
     * Converts a list of mod infos to minimal mod infos.
     * @param modInfos : List of mod infos to convert to minimal mod infos.
     * @return : A list of minimal mod infos of the provided full mod infos.
     */
    public static List<MinimalModInfo> fromList(List<ModInfo> modInfos){
        List<MinimalModInfo> minimalModInfos = new ArrayList<MinimalModInfo>();
        for(ModInfo modInfo : modInfos){
            minimalModInfos.add(new MinimalModInfo(modInfo));
        }
        return minimalModInfos;
    }
}
