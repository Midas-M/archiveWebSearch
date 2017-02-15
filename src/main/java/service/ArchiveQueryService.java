/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.google.gson.Gson;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import structures.ArchiveUrl;
import structures.ResponseWrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ListIterator;

/**
 *
 * @author pmeladianos
 */
public class ArchiveQueryService {

    DateTimeFormatter formatter=  DateTimeFormatter.ISO_INSTANT;

    public String getUrls(String keywords,String dateFromRaw,String dateToRaw) {
        LocalDate dateFrom;
        LocalDate dateTo;

        if(!dateFromRaw.equals("none"))
             dateFrom = LocalDate.parse(dateFromRaw, formatter);
        else
            dateFrom = LocalDate.parse("2000-12-03T10:15:30Z", formatter);

        if(!dateToRaw.equals("none"))
            dateTo = LocalDate.parse(dateToRaw, formatter);
        else {
            LocalDate date = LocalDate.now();
            String text = date.format(formatter);
            dateTo = LocalDate.parse(text, formatter);
        }
        String dateRange="["+dateFrom.toString()+" TO "+dateTo.toString()+"]";
        String query = getQuery(keywords,dateRange);
        SolrQuery solrQuery;
        solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        if (!query.equals("")) {
            solrQuery.setHighlight(true).setHighlightSnippets(1).setHighlightSimplePost("</strong>").setHighlightSimplePre("<strong>"); //set other params as needed
            solrQuery.setParam("hl.fl", "content");
            solrQuery.setParam("hl.requireFieldMatch", "true");
        }

        solrQuery.setRows(15);
        String urlString = "http://195.251.252.8:8983/solr/mail";
        //SolrClient server = new HttpSolrClient.Builder(urlString).build();
        SolrClient server = new HttpSolrClient(urlString);
        QueryResponse response = null;
        try {
            response = server.query(solrQuery);

        } catch (Exception e) {
            System.out.println(e);
        }
        SolrDocumentList rs = response.getResults();
        long numFound = rs.getNumFound();
        int numResultsDisplay = (int) numFound;
        ResponseWrapper responseWrapper=new ResponseWrapper();
                
        ListIterator<SolrDocument> iter = rs.listIterator();

        while (iter.hasNext()) {
            SolrDocument doc = iter.next();
            String url = doc.get("url").toString();
            String dateRaw = doc.get("date").toString();
            LocalDate date = LocalDate.parse(dateRaw, formatter);
            String title = doc.get("title").toString();
            String content = doc.get("content").toString();
            responseWrapper.add(new ArchiveUrl( url,  date,  title,  content));
        }
        Gson gson = new Gson();
        String APIresponse = gson.toJson(responseWrapper);
        return APIresponse;
    }

    private static String getQuery(String keywords,String dateRange) {
        String query = "";

        String query_0 = queryBuilder(keywords, "title");

        String query_1 = queryBuilder(keywords, "content");
        query = query_0 + " OR (" + query_1+")^10"+" AND date:"+dateRange;
        return query;
    }

    private static String queryBuilder(String s, String field) {
        String res = "";
        res = field + ":" + "'" + s + "'~1000";
        return res;

    }

}
