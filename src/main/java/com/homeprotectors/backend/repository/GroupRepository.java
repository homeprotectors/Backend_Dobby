package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByPublicId(UUID publicId);
}
