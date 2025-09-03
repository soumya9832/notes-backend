package com.notesapp.notes_backend.repository;

import com.notesapp.notes_backend.model.Note;
import com.notesapp.notes_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
    Optional<Note> findByShareToken(String shareToken);



}
