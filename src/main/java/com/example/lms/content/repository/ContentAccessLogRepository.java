// ContentAccessLogRepository.java
package com.example.lms.content.repository;

import com.example.lms.content.model.ContentAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentAccessLogRepository extends JpaRepository<ContentAccessLog, Long> {
}