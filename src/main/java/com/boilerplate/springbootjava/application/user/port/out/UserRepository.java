package com.boilerplate.springbootjava.application.user.port.out;

import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

}
