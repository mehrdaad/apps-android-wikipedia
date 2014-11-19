package org.wikipedia.test;

import org.wikipedia.PageTitle;
import org.wikipedia.Site;
import org.wikipedia.WikipediaApp;
import org.wikipedia.search.FullSearchResult;
import org.wikipedia.wikidata.WikidataCache;
import org.wikipedia.wikidata.WikidataDescriptionFeeder;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests retrieval of Wikidata descriptions. Talks to wikidata.org.
 */
public final class WikidataDescriptionFeederTests extends ActivityUnitTestCase<TestDummyActivity> {
    private static final int TASK_COMPLETION_TIMEOUT = 200000;
    private static final Site SITE = new Site("test.wikipedia.org");
    private String originalLanguage;

    public WikidataDescriptionFeederTests() {
        super(TestDummyActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startActivity(new Intent(), null, null);
    }

    @Override
    public void tearDown() throws Exception {
        final WikipediaApp app = (WikipediaApp) getInstrumentation().getTargetContext().getApplicationContext();
        app.setPrimaryLanguage(originalLanguage);
        super.tearDown();
    }

    public void testZeroID() throws Throwable {
        getWikidataDescriptions(new FullSearchResult[] {
        });
    }

    public void testOneIDTwice() throws Throwable {
        getWikidataDescriptions(new FullSearchResult[] {
                new FullSearchResult(new PageTitle("p1", SITE), null, "Q42")
        });
        getWikidataDescriptions(new FullSearchResult[] {
                new FullSearchResult(new PageTitle("p1", SITE), null, "Q42")
        });
    }

    public void testThreeIDs() throws Throwable {
        getWikidataDescriptions(new FullSearchResult[] {
                new FullSearchResult(new PageTitle("p1", SITE), null, "Q1"),
                new FullSearchResult(new PageTitle("p2", SITE), null, "Q2"),
                new FullSearchResult(new PageTitle("p3", SITE), null, "Q3")
        });
    }

    public void getWikidataDescriptions(final FullSearchResult[] input) throws Throwable {
        final ArrayList<FullSearchResult> inputList = new ArrayList<FullSearchResult>(Arrays.asList(input));
        final CountDownLatch completionLatch = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                final WikipediaApp app = (WikipediaApp) getInstrumentation().getTargetContext().getApplicationContext();

                // TODO: it would be better to pass in the language all the way down to the cache level instead of using getPrimaryLanguage.
                originalLanguage = app.getPrimaryLanguage();
                app.setPrimaryLanguage("en");

                WikidataDescriptionFeeder.retrieveWikidataDescriptions(inputList, app, new WikidataCache.OnWikidataReceiveListener() {
                    @Override
                    public void onWikidataReceived(Map<String, String> descriptions) {
                        for (FullSearchResult res : input) {
                            assertFalse(descriptions.get(res.getWikiBaseId()).isEmpty());
                        }
                        completionLatch.countDown();
                    }

                    @Override
                    public void onWikidataFailed(Throwable caught) {
                        fail();
                    }
                });
            }
        });
        assertTrue(completionLatch.await(TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS));
    }
}

