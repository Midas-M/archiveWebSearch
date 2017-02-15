package service;

import com.google.gson.Gson;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    private final AtomicLong counter = new AtomicLong();



    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String getUrl(@RequestParam(value="keywords") String keywords, @RequestParam(value="dateFrom", defaultValue = "none") String dateFrom, @RequestParam(value="dateTo", defaultValue = "none") String dateTo, @RequestParam(value="type", defaultValue = "keywords") String type) throws IOException {
        Gson gson = new Gson();
        keywords = java.net.URLDecoder.decode(keywords,"UTF-8");



        //String jsonInString = gson.toJson(s);
        return "";
    }


}