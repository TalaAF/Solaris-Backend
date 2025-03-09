package com.example.lms.security.repository;

import com.example.lms.security.model.SecurityEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SecurityEndpointRepository extends JpaRepository<SecurityEndpoint, Long> {
    List<SecurityEndpoint> findByHttpMethod(String httpMethod);
}