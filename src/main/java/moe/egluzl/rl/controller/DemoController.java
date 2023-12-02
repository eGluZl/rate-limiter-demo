package moe.egluzl.rl.controller;

import moe.egluzl.rl.annotation.Limit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping("/1")
    @Limit(key = "limit", permitsPerSecond = 1, timeout = 500, timeUnit = TimeUnit.MILLISECONDS)
    public String demo1() {
        var now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        return "aka - " + now;
    }


}
