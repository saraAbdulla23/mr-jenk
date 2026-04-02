package com.user_service.repository;

import com.user_service.model.Otp;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRepository extends Neo4jRepository<Otp, String> {

    Optional<Otp> findByEmail(String email);

    @Query("MATCH (o:Otp {email: $email}) DELETE o")
    void deleteByEmail(@Param("email") String email);
}