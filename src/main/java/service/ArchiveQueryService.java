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
import org.apache.tomcat.jni.Local;
import org.springframework.format.annotation.DateTimeFormat;
import structures.ArchiveUrl;
import structures.ResponseWrapper;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ListIterator;

/**
 *
 * @author pmeladianos
 */
public class ArchiveQueryService {

    DateTimeFormatter formatter=  DateTimeFormatter.ISO_INSTANT;

    public String getUrls(String keywords,String dateFromRaw,String dateToRaw) {
        //DATE FORMATS ISO_INSTANT
        ZonedDateTime dateFrom;
        ZonedDateTime dateTo;
        ZoneId zone= ZoneId.of("UTC");
        //YY-MM-DD
        if(!dateFromRaw.equals("none")) {
            LocalDateTime temp = LocalDateTime.of(Integer.valueOf(dateFromRaw.split("-")[0]), Integer.valueOf(dateFromRaw.split("-")[1]), Integer.valueOf(dateFromRaw.split("-")[2]), 0, 0);
            dateFrom=ZonedDateTime.of(temp,zone);
        }
        else {
            LocalDateTime temp = LocalDateTime.of(1999, Month.JANUARY, 1, 0, 0);
            dateFrom=ZonedDateTime.of(temp,zone);
        }
        if(!dateToRaw.equals("none")) {
            LocalDateTime temp = LocalDateTime.of(Integer.valueOf(dateToRaw.split("-")[0]), Integer.valueOf(dateToRaw.split("-")[1]), Integer.valueOf(dateToRaw.split("-")[2]), 0, 0);
            dateTo=ZonedDateTime.of(temp,zone);
        }
        else {
            LocalDateTime temp = LocalDateTime.now();
            dateTo=ZonedDateTime.of(temp,zone);
        }
        String dateRange="["+dateFrom.format(formatter)+" TO "+dateTo.format(formatter)+"]";


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
        String urlString = "http://localhost:8983/solr/aueb_archive";
        SolrClient server = new HttpSolrClient.Builder(urlString).build();
        //SolrClient server = new HttpSolrClient(urlString);
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
            String url = doc.get("url_s").toString();
            //String dateRaw = doc.get("date_dt").toString();
            Date dDate = (Date) doc.get("date_dt");
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dDate);
            //LocalDate date = LocalDate.parse(dateRaw);
            String title = doc.get("title_t").toString();
            String content = doc.get("content_t").toString();
            responseWrapper.add(new ArchiveUrl( url,  date,  title,  content));
        }
        Gson gson = new Gson();
        String APIresponse = gson.toJson(responseWrapper);
        return APIresponse;
    }

    private static String getQuery(String keywords,String dateRange) {
        String query = "";
        keywords.replaceAll(","," ");
        String query_0 = queryBuilder(keywords, "title_t");

        String query_1 = queryBuilder(keywords, "content_t");
        query = query_0 + " OR (" + query_1+")^10"+" AND date_dt:"+dateRange;
        return query;
    }

    private static String queryBuilder(String s, String field) {
        String res = "";
        res = field + ":" + "'" + s + "'~1000";
        return res;

    }

}
