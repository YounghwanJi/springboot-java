package com.boilerplate.springbootjava.application.auth.port.out;

import com.boilerplate.springbootjava.infrastructure.persistence.auth.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
}
