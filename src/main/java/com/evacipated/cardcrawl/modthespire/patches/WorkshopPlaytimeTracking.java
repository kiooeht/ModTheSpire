package com.evacipated.cardcrawl.modthespire.patches;

import com.codedisaster.steamworks.*;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.integrations.steam.SteamIntegration;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

@SpirePatch2(
    clz=SteamIntegration.class,
    method=SpirePatch.CONSTRUCTOR
)
public class WorkshopPlaytimeTracking
{
    private static final int MAX_TRACK_PER_CALL = 100;
    private static SteamUGC workshop = null;

    @SpireInsertPatch(
        locator=Locator.class
    )
    public static void InitUGC(Logger ___logger)
    {
        workshop = new SteamUGC(new Callback());

        SteamPublishedFileID[] ids = Stream.concat(
            Stream.of(1605060445L), // ModTheSpire's ID
            Arrays.stream(ModTheSpire.MODINFOS)
                .filter(x -> x.workshopInfo != null)
                .map(x -> x.workshopInfo.getID())
        )
            .map(SteamPublishedFileID::new)
            .toArray(SteamPublishedFileID[]::new);
        ___logger.info("Tracking mod playtime");
        if (ids.length <= MAX_TRACK_PER_CALL) {
            //workshop.startPlaytimeTracking(ids);
            ___logger.info(Arrays.toString(ids));
        } else {
            for (int i = 0; i < ids.length; i += MAX_TRACK_PER_CALL) {
                SteamPublishedFileID[] range = Arrays.copyOfRange(ids, i, Math.min(i + MAX_TRACK_PER_CALL, ids.length));
                //workshop.startPlaytimeTracking(range);
                ___logger.info(Arrays.toString(range));
            }
        }
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
        {
            Matcher finalMatcher = new Matcher.NewExprMatcher(Thread.class);
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    private static class Callback implements SteamUGCCallback
    {

        @Override
        public void onUGCQueryCompleted(SteamUGCQuery query, int numResultsReturned, int totalMatchingResults, boolean isCachedData, SteamResult result)
        {
        }

        @Override
        public void onSubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result)
        {
        }

        @Override
        public void onUnsubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result)
        {
        }

        @Override
        public void onRequestUGCDetails(SteamUGCDetails details, SteamResult result)
        {
        }

        @Override
        public void onCreateItem(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
        {
        }

        @Override
        public void onSubmitItemUpdate(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
        {
        }

        @Override
        public void onDownloadItemResult(int appID, SteamPublishedFileID publishedFileID, SteamResult result)
        {
        }

        @Override
        public void onUserFavoriteItemsListChanged(SteamPublishedFileID publishedFileID, boolean wasAddRequest, SteamResult result)
        {
        }

        @Override
        public void onSetUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp, SteamResult result)
        {
        }

        @Override
        public void onGetUserItemVote(SteamPublishedFileID publishedFileID, boolean votedUp, boolean votedDown, boolean voteSkipped, SteamResult result)
        {
        }

        @Override
        public void onStartPlaytimeTracking(SteamResult result)
        {
        }

        @Override
        public void onStopPlaytimeTracking(SteamResult result)
        {
        }

        @Override
        public void onStopPlaytimeTrackingForAllItems(SteamResult result)
        {
        }

        @Override
        public void onDeleteItem(SteamPublishedFileID publishedFileID, SteamResult result)
        {
        }
    }
}
