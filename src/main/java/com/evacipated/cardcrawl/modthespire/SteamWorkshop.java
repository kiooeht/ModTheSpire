package com.evacipated.cardcrawl.modthespire;

import com.codedisaster.steamworks.*;

import java.util.Arrays;

public class SteamWorkshop
{
    private static final int appId = 646570;

    private static SteamUGC workshop;

    private static boolean kill = false;

    public static void main(String[] args)
    {
        try {
            if (!SteamAPI.init()) {
                System.err.println("Could not connect to Steam. Is it running?");
                System.exit(1);
            }
        } catch (SteamException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        if (SteamAPI.isSteamRunning(true)) {
            SteamApps apps = new SteamApps();
            System.out.println(apps.getAppOwner().getAccountID());

            workshop = new SteamUGC(new Callback());
            int items = workshop.getNumSubscribedItems();

            SteamPublishedFileID[] publishedFileIDS = new SteamPublishedFileID[items];
            items = workshop.getSubscribedItems(publishedFileIDS);

            SteamUGCQuery query = workshop.createQueryUGCDetailsRequest(Arrays.asList(publishedFileIDS));
            workshop.sendQueryUGCRequest(query);

            while (SteamAPI.isSteamRunning()) {
                try {
                    Thread.sleep(66L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SteamAPI.runCallbacks();

                if (kill) {
                    break;
                }
            }
        }

        SteamAPI.shutdown();
    }

    private static class Callback implements SteamUGCCallback {

        @Override
        public void onUGCQueryCompleted(SteamUGCQuery query, int numResultsReturned, int totalMatchingResults, boolean isCachedData, SteamResult result)
        {
            if (query.isValid()) {
                for (int i = 0; i < numResultsReturned; ++i) {
                    SteamUGCDetails details = new SteamUGCDetails();
                    if (workshop.getQueryUGCResult(query, i, details)) {
                        SteamUGC.ItemInstallInfo info = new SteamUGC.ItemInstallInfo();
                        if (workshop.getItemInstallInfo(details.getPublishedFileID(), info)) {
                            System.out.println(info.getFolder());
                        }

                        System.out.println(details.getTags());
                    } else {
                        System.out.println("query valid? " + query.isValid());
                        System.out.println("index: " + i);
                        System.out.println("Query result failed");
                    }
                }
            } else {
                System.err.println("Not a valid query?");
            }

            kill = true;
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
