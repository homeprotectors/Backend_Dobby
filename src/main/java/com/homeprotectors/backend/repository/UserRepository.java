package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByInstallId(UUID installId);
    Optional<User> findByPublicId(UUID publicId);
}
