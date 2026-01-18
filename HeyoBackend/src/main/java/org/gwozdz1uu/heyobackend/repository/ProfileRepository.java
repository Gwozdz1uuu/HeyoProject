package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.Profile;
import org.gwozdz1uu.heyobackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
    Optional<Profile> findByUserId(Long userId);
}
