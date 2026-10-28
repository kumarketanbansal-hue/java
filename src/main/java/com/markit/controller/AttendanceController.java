package com.markit.controller;

import com.markit.model.AttendanceRecord;
import com.markit.model.Event;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

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
        activeEvent.setPinExpiresAt(now + PIN_REFRESH_MS);
        attendanceDatabase.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("event", activeEvent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-status")
    public ResponseEntity<Map<String, Object>> getEventStatus() {
        refreshPinIfDue();
        Map<String, Object> response = new HashMap<>();
        response.put("event", activeEvent);
        response.put("attendance", attendanceDatabase);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody AttendanceRecord record) {
        refreshPinIfDue();
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
        private static final long PIN_REFRESH_MS = 60_000L;

    private synchronized void refreshPinIfDue() {
        if (activeEvent == null) return;
        long now = System.currentTimeMillis();
        if (now <= activeEvent.getExpiresAt() && now >= activeEvent.getPinExpiresAt()) {
            activeEvent.setPin(String.format("%04d", new Random().nextInt(10000)));
            activeEvent.setPinExpiresAt(now + PIN_REFRESH_MS);
        }


    }
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() {
        if (activeEvent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<AttendanceRecord> rows;
        synchronized (attendanceDatabase) {
            rows = new ArrayList<>(attendanceDatabase);
        }
        rows.sort(Comparator.comparing(AttendanceRecord::getYear)
                .thenComparing(AttendanceRecord::getDepartment)
                .thenComparing(AttendanceRecord::getSection)
                .thenComparing(AttendanceRecord::getEmail));

        StringBuilder csv = new StringBuilder("Email ID,Reg No,Name,Year,Department,Section,Checked In At\r\n");
        for (AttendanceRecord r : rows) {
            csv.append(csvCell(r.getEmail())).append(',')
               .append(csvCell(r.getRegNo())).append(',')
               .append(csvCell(r.getName())).append(',')
               .append(csvCell(r.getYear())).append(',')
               .append(csvCell(r.getDepartment())).append(',')
               .append(csvCell(r.getSection())).append(',')
               .append(csvCell(Instant.ofEpochMilli(r.getAt()).toString())).append("\r\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(activeEvent.getId() + "-report.csv").build().toString())
                .contentType(new MediaType("text", "csv"))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csvCell(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }
}