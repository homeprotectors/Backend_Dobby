package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {
}
