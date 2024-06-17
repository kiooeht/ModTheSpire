package com.evacipated.cardcrawl.modthespire.steam;

import com.codedisaster.steamworks.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Scanner;

public class SteamWorkshop
{
    private static final int appId = 646570;

    public static void main(String[] args){
        // Load all needed API libraries and initialize the API
        try {
            try {
                SteamAPI.loadLibraries();
            } catch (NoSuchMethodError ignored) {
                // Running an older version of the game, before steamworks4j 1.9.0
            }
            if (!SteamAPI.init()) {
                System.err.println("Could not connect to Steam. Is it running?");
                System.exit(1);
            }
        } catch (SteamException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        // When program closes, shutdown the Steam API link as well
        Runtime.getRuntime().addShutdownHook(new Thread(SteamAPI::shutdown));

        // If steam isn't running, shutdown
        if(!SteamAPI.isSteamRunning(true)){
            return;
        }

        // Set steam rich presence
        SteamFriends friends = new SteamFriends(new FriendsCallback());
        friends.setRichPresence("status", "ModTheSpire");
        friends.setRichPresence("steam_display", "#Status");
        friends.dispose();

        //As long as steam is running, listen to requests from MtS
        Scanner scanner = new Scanner(System.in);
        while (SteamAPI.isSteamRunning()) {
            SteamAPI.runCallbacks();

            try {
                Thread.sleep(66L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                if(System.in.available() > 0){
                    String command = scanner.nextLine();
                    switch (command){
                        case "workshop_infos":
                            new UGCQueryRequest();
                            break;
                        case "subscribe_and_download":
                            String toDownload = scanner.nextLine();
                            List<Long> toDownloadIds = new Gson().fromJson(toDownload, new TypeToken<List<Long>>(){}.getType());
                            if(!toDownloadIds.isEmpty()){
                                new SubscribeAndDownloadRequest(toDownloadIds);
                            }
                            break;
                        case "quit":
                            System.exit(0);
                            break;
                    }
                }
            }catch (Exception e) {
                System.err.println("Failed to ..." + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

    private static class FriendsCallback implements SteamFriendsCallback {
        @Override
        public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result)
        {

        }

        @Override
        public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change)
        {

        }

        @Override
        public void onGameOverlayActivated(boolean active)
        {

        }

        @Override
        public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend)
        {

        }

        @Override
        public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height)
        {

        }

        @Override
        public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID)
        {

        }

        @Override
        public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect)
        {

        }

        @Override
        public void onGameServerChangeRequested(String server, String password)
        {

        }
    }
}
