package com.notesapp.notes_backend.controller;

import com.notesapp.notes_backend.model.Note;
import com.notesapp.notes_backend.model.User;
import com.notesapp.notes_backend.repository.NoteRepository;
import com.notesapp.notes_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteController(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // Get all notes of logged-in user
    @GetMapping
    public List<Note> getNotes(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return noteRepository.findByUser(user);
    }

    // Create note
    @PostMapping
    public Note createNote(@RequestBody Note note, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        note.setUser(user);
        return noteRepository.save(note);
    }

    // Update note
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note noteDetails, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Note note = noteRepository.findById(id).orElseThrow();

        if (!note.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        return ResponseEntity.ok(noteRepository.save(note));
    }

    // Delete note
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Note note = noteRepository.findById(id).orElseThrow();

        if (!note.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        noteRepository.delete(note);
        return ResponseEntity.ok().build();
    }

    // Enable sharing: generate token
    @PostMapping("/{id}/share")
    public ResponseEntity<?> enableSharing(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Note note = noteRepository.findById(id).orElseThrow();

        if (!note.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        note.setShareToken(java.util.UUID.randomUUID().toString());
        noteRepository.save(note);

        String publicUrl = "/api/notes/share/" + note.getShareToken();
        return ResponseEntity.ok(Map.of("shareUrl", publicUrl));
    }

    // Disable sharing: remove token
    @PostMapping("/{id}/unshare")
    public ResponseEntity<?> disableSharing(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Note note = noteRepository.findById(id).orElseThrow();

        if (!note.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        note.setShareToken(null);
        noteRepository.save(note);
        return ResponseEntity.ok(Map.of("message", "Sharing disabled"));
    }

    // Public endpoint: view note by token (no auth required)
    @GetMapping("/share/{token}")
    public ResponseEntity<?> getSharedNote(@PathVariable String token) {
        return noteRepository.findByShareToken(token)
                .map(note -> ResponseEntity.ok(Map.of(
                        "id", note.getId(),
                        "title", note.getTitle(),
                        "content", note.getContent(),
                        "createdAt", note.getCreatedAt(),
                        "updatedAt", note.getUpdatedAt()
                )))
                .orElse(ResponseEntity.notFound().build());
    }



}
