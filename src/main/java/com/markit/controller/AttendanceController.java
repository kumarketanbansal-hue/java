package com.markit.controller;

import com.markit.model.AttendanceRecord;
import com.markit.model.Event;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private Event activeEvent = null;
    private final List<AttendanceRecord> attendanceDatabase = Collections.synchronizedList(new ArrayList<>());

    @PostMapping("/create-event")
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String venue = (String) payload.get("venue");
        int duration = payload.containsKey("duration") ? Integer.parseInt(payload.get("duration").toString()) : 10;

        long now = System.currentTimeMillis();
        String eventId = "EVT-" + Long.toString(now, 36).toUpperCase();
        String pin = String.format("%04d", new Random().nextInt(10000));

        activeEvent = new Event(eventId, name, venue, pin, duration, now, now + (duration * 60000L));
        attendanceDatabase.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("event", activeEvent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-status")
    public ResponseEntity<Map<String, Object>> getEventStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("event", activeEvent);
        response.put("attendance", attendanceDatabase);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody AttendanceRecord record) {
        Map<String, Object> response = new HashMap<>();

        if (activeEvent == null || System.currentTimeMillis() > activeEvent.getExpiresAt()) {
            response.put("error", "Event is unavailable or expired.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!activeEvent.getPin().equals(record.getPin())) {
            response.put("error", "Incorrect PIN code.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean alreadyCheckedIn = attendanceDatabase.stream()
                .anyMatch(r -> r.getEmail().equalsIgnoreCase(record.getEmail()));
        if (alreadyCheckedIn) {
            response.put("error", "Attendance already recorded!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        record.setAt(System.currentTimeMillis());
        attendanceDatabase.add(record);

        response.put("success", true);
        response.put("record", record);
        return ResponseEntity.ok(response);
    }
}