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
    public String getUrl(@RequestParam(value="keywords") String keywords, @RequestParam(value="datefrom", defaultValue = "none") String dateFrom, @RequestParam(value="dateto", defaultValue = "none") String dateTo, @RequestParam(value="type", defaultValue = "keywords") String type) throws IOException {
        keywords = java.net.URLDecoder.decode(keywords,"UTF-8");
        ArchiveQueryService service=new ArchiveQueryService();
        String response = service.getUrls(keywords, dateFrom, dateTo);
        return response;
    }

    @RequestMapping(value = "/indexing", method = RequestMethod.GET)
    public void warcIndexing(){
        WarcIndexer indexer = new WarcIndexer();
    }


}