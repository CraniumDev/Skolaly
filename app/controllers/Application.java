package controllers;

import models.MyResultObject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import play.data.validation.Required;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Application extends Controller {

    private static List<MyResultObject> myResults = new ArrayList<MyResultObject>();

    public static void index() {
        render();
    }

    public static void privacy() {
        render();
    }

    public static void terms() {
        render();
    }

    public static void about() {
        render();
    }

    public static void query(@Required String keyword) throws SolrServerException, IOException {
        String DEFAULT_COLLECTION = "skolaly";
        String zkHostString = "ec2-34-193-230-172.compute-1.amazonaws.com:2181," +
                "ec2-34-194-75-157.compute-1.amazonaws.com:2181";

        CloudSolrClient solrClient = new CloudSolrClient.Builder().withZkHost(zkHostString).build();
        solrClient.setDefaultCollection(DEFAULT_COLLECTION);

        params.flash();  // add http parameters to the flash scope and persist search form query data

        SolrQuery query = new SolrQuery();
        query.setQuery(keyword);
        query.setFields("digest", "id", "content", "title", "url", "tstamp");
        query.setHighlight(true).setHighlightSnippets(4);
        query.setParam("hl.tag.pre", "<strong>").setParam("hl.tag.post", "</strong>");
        query.setParam("hl.fl", "content");
        query.setParam("hl.maxAnalyzedChars", "350");
        query.set("q", keyword);
        query.setSort("score", SolrQuery.ORDER.desc);
        query.addSort(SolrQuery.SortClause.desc("digest"));
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;

        myResults.clear();
        while (!done) {
            query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse response = solrClient.query(query);
            String nextCursorMark = response.getNextCursorMark();
            processSearchResults(response);

            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }

        ValuePaginator<MyResultObject> paginator = new ValuePaginator<MyResultObject>(myResults);
        paginator.setBoundaryControlsEnabled(false);
        int rows = 10;
        paginator.setPageSize(rows);
        paginator.setPagesDisplayed(rows);

        render(paginator);
    }

    private static void processSearchResults(QueryResponse response) {
        SolrDocumentList results = response.getResults();
        try {
            for (SolrDocument result : results) {

                MyResultObject tmpObj = new MyResultObject();

                String title = result.getFieldValue("title").toString();
                tmpObj.setTitle(title);

                String url = result.getFieldValue("url").toString();
                tmpObj.setUrl(url);

                String indexed_date = result.getFieldValue("tstamp").toString();
                tmpObj.setIndexedDate(indexed_date);

                String id = result.getFieldValue("digest").toString();

                if (response.getHighlighting().get(id) != null) {
                    List<String> highlightSnippet = response.getHighlighting().get(id).get("content");
                    String snippet = highlightSnippet.get(0);
                    tmpObj.setDescription(snippet);
                }

                myResults.add(tmpObj);
            }
        } catch (NullPointerException ex) {
            ex.getMessage();
        }
    }
}