package org.wikipedia.page;

import org.wikipedia.Site;
import org.wikipedia.search.FullSearchArticlesTask;
import org.wikipedia.search.FullSearchResult;
import org.mediawiki.api.json.Api;
import org.mediawiki.api.json.ApiResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Task for getting suggestions for further reading.
 * Take page title, run full text search with that query, get four results,
 * show top three that aren't the actual page title in question.
 */
public class SuggestionsTask extends FullSearchArticlesTask {
    private static final int MAX_SIZE = 3;
    private static final int MAX_REQUESTED = MAX_SIZE + 1;
    private final String title;

    public SuggestionsTask(Api api, Site site, String title) {
        super(api, site, title, MAX_REQUESTED, null);
        this.title = title;
    }

    @Override
    public FullSearchResults processResult(final ApiResult result) throws Throwable {
        FullSearchResults searchResults = super.processResult(result);
        List<FullSearchResult> filteredResults = new ArrayList<FullSearchResult>();
        List<FullSearchResult> results = searchResults.getResults();
        for (int i = 0, count = 0; i < MAX_REQUESTED && count < MAX_SIZE; i++) {
            final FullSearchResult res = results.get(i);
            if (!title.equalsIgnoreCase(res.getTitle().getPrefixedText())) {
                filteredResults.add(res);
                count++;
            }
        }
        return new FullSearchResults(filteredResults, null, null);
    }
}