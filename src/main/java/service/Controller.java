package service;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    private final AtomicLong counter = new AtomicLong();



    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String getUrl(@RequestParam(value="keywords") String keywords, @RequestParam(value="dateFrom", defaultValue = "none") String dateFrom, @RequestParam(value="dateTo", defaultValue = "none") String dateTo, @RequestParam(value="type", defaultValue = "keywords") String type) throws IOException {
        Gson gson = new Gson();
        keywords = java.net.URLDecoder.decode(keywords,"UTF-8");
        ArchiveQueryService service=new ArchiveQueryService();
        String response = service.getUrls(keywords, dateFrom, dateTo);
        return response;
    }


}