package com.quicknotes.controller;

import com.quicknotes.model.Note;
import com.quicknotes.service.NoteService;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Create note (JSON body)
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        Note saved = noteService.addNote(note);
        return ResponseEntity.ok(saved);
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNote(@PathVariable String id) {
        Optional<Note> n = noteService.getById(id);
        return n.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // List all notes
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAll());
    }

    // Search by title keyword
    @GetMapping("/search")
    public ResponseEntity<List<Note>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(noteService.searchByTitle(keyword));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        noteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Optional: set TTL on a specific note (seconds)
    @PostMapping("/{id}/ttl")
    public ResponseEntity<String> setTtl(@PathVariable String id, @RequestParam long seconds) {
        boolean ok = noteService.setTtl(id, seconds, TimeUnit.SECONDS);
        if (ok) return ResponseEntity.ok("TTL set to " + seconds + " seconds");
        return ResponseEntity.badRequest().body("Failed to set TTL (note may not exist)");
    }

    // Optional: check TTL
    @GetMapping("/{id}/ttl")
    public ResponseEntity<Long> getTtl(@PathVariable String id) {
        Long ttl = noteService.getTtl(id);
        if (ttl == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ttl);
    }
}



