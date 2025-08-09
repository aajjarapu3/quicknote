package com.quicknotes.service;

import com.quicknotes.model.Note;
import com.quicknotes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class NoteService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String NOTE_KEY_PREFIX = "note:";
    private static final String NOTE_IDS_KEY = "note:ids";

    public NoteService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Note addNote(Note note) {
        // auto-generate id if not provided (avoid spaces)
        String id = note.getId();
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9\\-]", "");
        }
        note.setId(id);

        String key = NOTE_KEY_PREFIX + id;
        // store fields in a hash
        redisTemplate.opsForHash().put(key, "id", id);
        if (note.getTitle() != null) redisTemplate.opsForHash().put(key, "title", note.getTitle());
        if (note.getContent() != null) redisTemplate.opsForHash().put(key, "content", note.getContent());

        // Add id to index set
        redisTemplate.opsForSet().add(NOTE_IDS_KEY, id);

        // Optional: do not set TTL by default; can be added later
        return note;
    }

    public Optional<Note> getById(String id) {
        String key = NOTE_KEY_PREFIX + id;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return Optional.empty();
        }
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) return Optional.empty();

        Note note = mapToNote(id, entries);
        return Optional.of(note);
    }

    public List<Note> getAll() {
        Set<String> ids = redisTemplate.opsForSet().members(NOTE_IDS_KEY);
        List<Note> result = new ArrayList<>();
        if (ids != null) {
            for (String id : ids) {
                getById(id).ifPresent(result::add);
            }
        }
        return result;
    }

    public void deleteById(String id) {
        String key = NOTE_KEY_PREFIX + id;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(NOTE_IDS_KEY, id);
    }

    public List<Note> searchByTitle(String keyword) {
        if (keyword == null) keyword = "";
        String lower = keyword.toLowerCase();

        Set<String> ids = redisTemplate.opsForSet().members(NOTE_IDS_KEY);
        List<Note> result = new ArrayList<>();
        if (ids != null) {
            for (String id : ids) {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(NOTE_KEY_PREFIX + id);
                if (entries == null || entries.isEmpty()) continue;
                Object titleObj = entries.get("title");
                String title = titleObj == null ? "" : titleObj.toString();
                if (title.toLowerCase().contains(lower)) {
                    result.add(mapToNote(id, entries));
                }
            }
        }
        return result;
    }

    // helper
    private Note mapToNote(String id, Map<Object, Object> entries) {
        String title = entries.get("title") != null ? entries.get("title").toString() : null;
        String content = entries.get("content") != null ? entries.get("content").toString() : null;
        return new Note(id, title, content);
    }

    // Optional: helper to set TTL on a note
    public boolean setTtl(String id, long timeout, TimeUnit unit) {
        String key = NOTE_KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    // Optional: check TTL
    public Long getTtl(String id) {
        String key = NOTE_KEY_PREFIX + id;
        return redisTemplate.getExpire(key);
    }
}