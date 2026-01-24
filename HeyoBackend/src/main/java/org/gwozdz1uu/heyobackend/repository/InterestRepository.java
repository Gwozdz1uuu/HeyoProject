package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByName(String name);
    boolean existsByName(String name);
}
