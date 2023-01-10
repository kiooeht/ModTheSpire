package com.evacipated.cardcrawl.modthespire.steam;

import com.codedisaster.steamworks.*;

import java.util.Arrays;
import java.util.Collection;

public class SteamWorkshop
{
    private static final int appId = 646570;

    private static SteamUGC workshop;

    private static boolean kill = false;

    public static void main(String[] args)
    {
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

        if (SteamAPI.isSteamRunning(true)) {
            try {
                SteamUtils utils = new SteamUtils(() -> {});
                boolean onDeck = utils.isSteamRunningOnSteamDeck();
                System.err.println("deck: " + onDeck);
                System.out.println(onDeck);
            } catch (NoSuchMethodError | IllegalAccessError ignored) {
                System.err.println("deck: " + false);
                System.out.println(false);
            }

            workshop = new SteamUGC(new Callback());
            int items = workshop.getNumSubscribedItems();

            SteamPublishedFileID[] publishedFileIDS = new SteamPublishedFileID[items];
            items = workshop.getSubscribedItems(publishedFileIDS);

            System.err.println("subbed items: " + items);

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

        int resultsReceived = 0;

        @Override
        public void onUGCQueryCompleted(SteamUGCQuery query, int numResultsReturned, int totalMatchingResults, boolean isCachedData, SteamResult result)
        {
            if (query.isValid()) {
                System.err.println("result: " + result);
                System.err.println("numResultsReturned: " + numResultsReturned);
                System.err.println("totalMatchingResults: " + totalMatchingResults);
                System.err.println("isCachedData: " + isCachedData);
                for (int i = 0; i < numResultsReturned; ++i) {
                    SteamUGCDetails details = new SteamUGCDetails();
                    if (workshop.getQueryUGCResult(query, i, details)) {
                        Collection<SteamUGC.ItemState> state = workshop.getItemState(details.getPublishedFileID());
                        if (state.contains(SteamUGC.ItemState.Installed)) {
                            SteamUGC.ItemInstallInfo info = new SteamUGC.ItemInstallInfo();
                            if (workshop.getItemInstallInfo(details.getPublishedFileID(), info)) {
                                System.out.println(details.getTitle());
                                System.out.println(details.getPublishedFileID());
                                System.out.println(info.getFolder());
                                System.out.println(details.getTimeUpdated());
                                System.out.println(details.getTags());
                            }
                        }
                    } else {
                        System.err.println("query valid? " + query.isValid());
                        System.err.println("index: " + i);
                        System.err.println("Query result failed");
                    }
                }
            } else {
                System.err.println("Not a valid query?");
            }

            resultsReceived += numResultsReturned;
            if (resultsReceived >= totalMatchingResults) {
                kill = true;
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
        public void onSubmitItemUpdate(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
        {

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

        @Override
        public void onDeleteItem(SteamPublishedFileID publishedFileID, SteamResult result)
        {

        }
    }
}
