package org.com.dianping.controller;

import java.util.Map;
import org.com.dianping.event.DeadLetterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/operations")
public class OperationsController {
    private final DeadLetterService deadLetters;
    public OperationsController(DeadLetterService deadLetters) { this.deadLetters = deadLetters; }
    @GetMapping("/dead-letters") public Map<String, Integer> pendingDeadLetters() { return Map.of("pending", deadLetters.pending()); }
    @PostMapping("/dead-letters/replay") public ResponseEntity<?> replayDeadLetter() { return ResponseEntity.ok(Map.of("replayed", deadLetters.replayOne())); }
}
