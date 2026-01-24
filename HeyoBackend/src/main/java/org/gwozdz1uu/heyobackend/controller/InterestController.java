package org.gwozdz1uu.heyobackend.controller;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.model.Interest;
import org.gwozdz1uu.heyobackend.repository.InterestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestRepository interestRepository;

    @GetMapping
    public ResponseEntity<List<Interest>> getAllInterests() {
        return ResponseEntity.ok(interestRepository.findAll());
    }
}
