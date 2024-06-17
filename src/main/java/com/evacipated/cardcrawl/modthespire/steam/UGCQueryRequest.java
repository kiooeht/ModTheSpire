package com.evacipated.cardcrawl.modthespire.steam;

import com.codedisaster.steamworks.*;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collection;

class UGCQueryRequest implements SteamUGCCallback {
    private SteamUGC workshop;
    private SteamData data;

    private int resultsReceived = 0;

    public UGCQueryRequest(){
        data = new SteamData();

        // Check if we're running on steamdeck
        try {
            SteamUtils utils = new SteamUtils(() -> {});
            data.steamDeck = utils.isSteamRunningOnSteamDeck();
        } catch (NoSuchMethodError | IllegalAccessError ignored) {}

        // Get all subscribed items and queue the info
        workshop = new SteamUGC(this);
        int items = workshop.getNumSubscribedItems();

        SteamPublishedFileID[] publishedFileIDS = new SteamPublishedFileID[items];
        items = workshop.getSubscribedItems(publishedFileIDS);

        System.err.println("subbed items: " + items);

        SteamUGCQuery query = workshop.createQueryUGCDetailsRequest(Arrays.asList(publishedFileIDS));
        workshop.sendQueryUGCRequest(query);
    }

    /** SteamUGC interface implementation */

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
                            SteamSearch.WorkshopInfo workshopInfo = new SteamSearch.WorkshopInfo(
                                details.getTitle(),
                                details.getPublishedFileID().toString(),
                                info.getFolder(),
                                details.getTimeUpdated(),
                                details.getTags()
                            );
                            data.workshopInfos.add(workshopInfo);
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
            Gson gson = new Gson();
            String json = gson.toJson(data);
            System.out.println(json);
        }
        workshop.releaseQueryUserUGCRequest(query);

        workshop.dispose();
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

    /** SteamUGC interface implementation end */
}
