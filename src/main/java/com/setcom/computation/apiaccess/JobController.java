package com.setcom.computation.apiaccess;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.setcom.computation.balticlsc.DataHandler;
import com.setcom.computation.balticlsc.JobRegistry;
import com.setcom.computation.balticlsc.JobThread;
import com.setcom.computation.balticlsc.TokenListener;
import com.setcom.computation.datamodel.InputTokenMessage;
import com.setcom.computation.datamodel.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping
public class  JobController {
    private final JobRegistry registry;
    private final DataHandler handler;
    private final TokenListener listener;

    @Autowired
    public JobController(JobRegistry registry, DataHandler handler, TokenListener listener) {
        this.registry = registry;
        this.handler = handler;
        this.listener = listener;
    }

    @PostMapping("/token")
    public ResponseEntity<String> processTokenMessage(@RequestBody InputTokenMessage value) {
        try {
            log.info("Token message received: " + value);
            registry.registerToken(value);
            try {
                String retMessage;
                short result = handler.checkConnection(
                        value.pinName,
                        new Gson().fromJson(value.toString(), new TypeToken<Map<String, String>>(){}.getType()));
                switch (result) {
                    case 0:
                        JobThread jobThread = new JobThread(value.pinName, listener, registry, handler);
                        registry.registerThread(jobThread);
                        var threadTask = new Thread();
                        threadTask.start();
                        /*
                        //TODO #4
                        // Nie jestem pewien czy to dobrze zrobiłem.
                        // Nie wiem w ogóle czy to nie jest tak, że Spring ogarnia takie rzeczy i czy nie mogę po prostu
                        // wrzucic jobThread::run
                        // Update (29.03.2022): Nie mogę, Spring kolejkuje sam wątki (wątek per request) ale beany muszą
                        // być bezstanowe
                        var task = new Task(() => jobThread.run());
                        task.Start();
                         */
                        return new ResponseEntity<>(HttpStatus.OK);
                    case -1:
                        retMessage = "No response " + value.pinName;
                        log.error(retMessage);
                        return new ResponseEntity<>(retMessage, HttpStatus.NOT_FOUND);
                    case -2:
                        retMessage = String.format("Unauthorized ,%s", value.pinName);
                        log.error(retMessage);
                        return new ResponseEntity<>(retMessage, HttpStatus.UNAUTHORIZED);
                    case -3:
                        retMessage = String.format("Invalid path %s",value.pinName);
                        log.error(retMessage);
                        return new ResponseEntity<>(retMessage, HttpStatus.UNAUTHORIZED);
                    default:
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                log.error(String.format(
                        "Error of type %s: %s\n %s",e.getClass(), e.getMessage(), Arrays.toString(e.getStackTrace())));
                return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Corrupted token: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<JobStatus> getStatus() {
        return new ResponseEntity<>(registry.getJobStatus(), HttpStatus.OK);
    }
}
