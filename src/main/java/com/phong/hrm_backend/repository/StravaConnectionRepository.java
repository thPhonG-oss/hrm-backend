package com.phong.hrm_backend.repository;

import com.phong.hrm_backend.entity.StravaConnection;
import com.phong.hrm_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StravaConnectionRepository extends JpaRepository<StravaConnection, Long> {
    Optional<Object> findByUser(User user);
    boolean existsByUser(User user);
}
