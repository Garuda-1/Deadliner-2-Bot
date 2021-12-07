package ru.itmo.sd.deadliner2bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/ping")
@Slf4j
public class PingController {

    @GetMapping()
    public ResponseEntity<Object> ping() {
        log.info("Pinged");
        return ResponseEntity.ok().build();
    }
}
