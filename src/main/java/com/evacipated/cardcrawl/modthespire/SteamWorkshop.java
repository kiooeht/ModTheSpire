package com.evacipated.cardcrawl.modthespire;

import com.codedisaster.steamworks.*;
import com.evacipated.cardcrawl.modthespire.steam.FriendsCallback;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SteamWorkshop
{
    private static final Logger logger = Logger.getLogger(SteamWorkshop.class.getName());

    private static final int appId = 646570;

    private SteamUGC workshop;

    public static class SteamTicker implements Runnable
    {
        @Override
        public void run()
        {
            while (SteamAPI.isSteamRunning()) {
                try {
                    Thread.sleep(66L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SteamAPI.runCallbacks();
            }
            System.out.println("[ERROR] SteamAPI stopped running.");
        }
    }

    SteamWorkshop()
    {
        System.out.println("TRYING TO CONNECT TO STEAM");
        connectToSteam();

        if (SteamAPI.isSteamRunning(true)) {
            workshop = new SteamUGC(new Callback());
            int items = workshop.getNumSubscribedItems();

            SteamPublishedFileID[] publishedFileIDS = new SteamPublishedFileID[items];
            items = workshop.getSubscribedItems(publishedFileIDS);

            SteamUGCQuery query = workshop.createQueryUGCDetailsRequest(Arrays.asList(publishedFileIDS));
            workshop.sendQueryUGCRequest(query);
        }
    }

    private void connectToSteam() {
        Thread thread;
        try {
            if (SteamAPI.init()) {
                SteamApps steamApps = new SteamApps();
                SteamID id = steamApps.getAppOwner();
                int accountId = id.getAccountID();
                System.out.println("ACCOUNT ID: " + accountId);

                SteamFriends friends = new SteamFriends(new FriendsCallback());
                String name = friends.getPersonaName();
                System.out.println("NAME: " + name);

                thread = new Thread(new SteamTicker());
                thread.setName("SteamTicker");
                thread.start();
            } else {
                logger.severe("Could not connect to Steam. Is it running?");
                //exit(1);
            }
        } catch (SteamException e) {
            logger.log(Level.SEVERE, e.toString(), e);
            //exit(1);
        }
    }

    private class Callback implements SteamUGCCallback {

        @Override
        public void onUGCQueryCompleted(SteamUGCQuery query, int numResultsReturned, int totalMatchingResults, boolean isCachedData, SteamResult result) {
            System.out.println("QUERY COMPLETE");

            System.out.println("results: " + numResultsReturned);

            if (query.isValid()) {
                for (int i=0; i<numResultsReturned; ++i) {
                    SteamUGCDetails details = new SteamUGCDetails();
                    if (workshop.getQueryUGCResult(query, i, details)) {
                        System.out.println(details.getPublishedFileID());
                        System.out.println(details.getTitle());
                        System.out.println(details.getDescription());

                        SteamUGC.ItemInstallInfo info = new SteamUGC.ItemInstallInfo();
                        if (workshop.getItemInstallInfo(details.getPublishedFileID(), info)) {
                            System.out.println(info.getFolder());
                        } else {
                            System.out.println("Not installed");
                        }
                    } else {
                        System.out.println("query valid? " + query.isValid());
                        System.out.println("index: " + i);
                        System.out.println("Query result failed");
                    }
                }
            } else {
                System.out.println("Not a valid query?");
            }
        }

        @Override
        public void onSubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) {

        }

        @Override
        public void onUnsubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) {

        }

        @Override
        public void onRequestUGCDetails(SteamUGCDetails details, SteamResult result) {

        }

        @Override
        public void onCreateItem(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {

        }

        @Override
        public void onSubmitItemUpdate(boolean needsToAcceptWLA, SteamResult result) {

        }

        @Override
        public void onDownloadItemResult(int appID, SteamPublishedFileID publishedFileID, SteamResult result) {

        }

        @Override
        public void onUserFavoriteItemsListChanged(SteamPublishedFileID publishedFileID, boolean wasAddRequest, SteamResult result) {

        }

        @Override
        public void onSetUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp, SteamResult result) {

        }

        @Override
        public void onGetUserItemVote(SteamPublishedFileID publishedFileID, boolean votedUp, boolean votedDown, boolean voteSkipped, SteamResult result) {

        }

        @Override
        public void onStartPlaytimeTracking(SteamResult result) {

        }

        @Override
        public void onStopPlaytimeTracking(SteamResult result) {

        }

        @Override
        public void onStopPlaytimeTrackingForAllItems(SteamResult result) {

        }
    }
}
