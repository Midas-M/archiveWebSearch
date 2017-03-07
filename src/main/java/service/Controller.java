package service;

import indexer.WarcIndexer;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    private final AtomicLong counter = new AtomicLong();


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String getUrl(@RequestParam(value="keywords", defaultValue = "none") String keywords, @RequestParam(value="datefrom", defaultValue = "none") String dateFrom, @RequestParam(value="dateto", defaultValue = "none") String dateTo, @RequestParam(value="url", defaultValue = "none") String url) throws IOException {
        ArchiveQueryService service=new ArchiveQueryService();
        if (url.equals("none")){
            keywords = java.net.URLDecoder.decode(keywords,"UTF-8");
            String k_response = service.getUrls(keywords, dateFrom, dateTo);
            return k_response;
        }else{
            String u_response = service.getUrls(url, dateFrom, dateTo);
            return u_response;
        }
    }

    @RequestMapping(value = "/indexing", method = RequestMethod.GET)
    public void warcIndexing(){
        WarcIndexer indexer = new WarcIndexer();
    }


}