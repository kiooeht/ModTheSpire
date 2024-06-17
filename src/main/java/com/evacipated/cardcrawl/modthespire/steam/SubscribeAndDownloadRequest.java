package com.evacipated.cardcrawl.modthespire.steam;

import com.codedisaster.steamworks.*;
import com.google.gson.Gson;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SubscribeAndDownloadRequest implements SteamUGCCallback {
    private SteamUGC workshop;

    private SteamPublishedFileID pendingDownload;

    private ArrayList<Long> completedDownloads = new ArrayList<>();
    private ArrayList<Long> failedDownloads = new ArrayList<>();

    public SubscribeAndDownloadRequest(List<Long> toDownloadIds){
        workshop = new SteamUGC(this);
        
        for(Long toDownload : toDownloadIds){
            subscribeAndDownloadItem(toDownload);
        }

        System.out.println(new Gson().toJson(new Pair<List<Long>, List<Long>>(completedDownloads, failedDownloads)));

        workshop.dispose();
    }

    private void subscribeAndDownloadItem(Long item){
        SteamPublishedFileID itemId = new SteamPublishedFileID(item);
        Collection<SteamUGC.ItemState> states = workshop.getItemState(itemId);

        //If the user already owns the item, return
        if(states.contains(SteamUGC.ItemState.Downloading)){
            completedDownloads.add(SteamNativeHandle.getNativeHandle(itemId));
            return;
        }

        //If the user isn't subscribed to the mod, subscribe
        if(!states.contains(SteamUGC.ItemState.Subscribed)){
            workshop.subscribeItem(itemId);
        }

        //Request the item download
        pendingDownload = itemId;
        System.out.println("downloading_" + item);

        boolean downloading = workshop.downloadItem(itemId, true);
        if(!downloading){
            pendingDownload = null;
            failedDownloads.add(SteamNativeHandle.getNativeHandle(itemId));
            return;
        }

        //While it's downloading, periodically send status updates to the main app for user display.
        while(pendingDownload != null){
            SteamAPI.runCallbacks();

            try{
                Thread.sleep(200);
            }catch (Exception ignored){}

            SteamUGC.ItemDownloadInfo downloadInfo = new SteamUGC.ItemDownloadInfo();
            workshop.getItemDownloadInfo(itemId, downloadInfo);

            long top = downloadInfo.getBytesDownloaded() * 100;
            long bottom = downloadInfo.getBytesTotal();

            if(bottom == 0){
                //Download is at 100%
                System.out.println("download_perc_100");
            }
            else{
                System.out.println("download_perc_" + (top / bottom));
            }
        }
    }

    @Override
    public void onUGCQueryCompleted(SteamUGCQuery steamUGCQuery, int i, int i1, boolean b, SteamResult steamResult) {

    }

    @Override
    public void onSubscribeItem(SteamPublishedFileID steamPublishedFileID, SteamResult steamResult) {

    }

    @Override
    public void onUnsubscribeItem(SteamPublishedFileID steamPublishedFileID, SteamResult steamResult) {

    }

    @Override
    public void onRequestUGCDetails(SteamUGCDetails steamUGCDetails, SteamResult steamResult) {

    }

    @Override
    public void onCreateItem(SteamPublishedFileID steamPublishedFileID, boolean b, SteamResult steamResult) {

    }

    @Override
    public void onSubmitItemUpdate(SteamPublishedFileID steamPublishedFileID, boolean b, SteamResult steamResult) {

    }

    @Override
    public void onDownloadItemResult(int i, SteamPublishedFileID itemId, SteamResult steamResult) {
        if(SteamNativeHandle.getNativeHandle(pendingDownload) != SteamNativeHandle.getNativeHandle(itemId)){
            return;
        }

        pendingDownload = null;

        if(steamResult == SteamResult.OK){
            completedDownloads.add(SteamNativeHandle.getNativeHandle(itemId));
        }
        else{
            failedDownloads.add(SteamNativeHandle.getNativeHandle(itemId));
        }
    }

    @Override
    public void onUserFavoriteItemsListChanged(SteamPublishedFileID steamPublishedFileID, boolean b, SteamResult steamResult) {

    }

    @Override
    public void onSetUserItemVote(SteamPublishedFileID steamPublishedFileID, boolean b, SteamResult steamResult) {

    }

    @Override
    public void onGetUserItemVote(SteamPublishedFileID steamPublishedFileID, boolean b, boolean b1, boolean b2, SteamResult steamResult) {

    }

    @Override
    public void onStartPlaytimeTracking(SteamResult steamResult) {

    }

    @Override
    public void onStopPlaytimeTracking(SteamResult steamResult) {

    }

    @Override
    public void onStopPlaytimeTrackingForAllItems(SteamResult steamResult) {

    }

    @Override
    public void onDeleteItem(SteamPublishedFileID steamPublishedFileID, SteamResult steamResult) {

    }
}
