package com.boilerplate.springbootjava.infrastructure.persistence.user;


import com.boilerplate.springbootjava.infrastructure.converter.SecureStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // 서비스 성격에 따라 선택적 암호화 - ID로 사용해야 할 경우, email을 직접 사용하는 것보다 UUID를 추가로 생성하여 사용하는 방법으로...
    // Security에서 username으로 사용하는 항목을 암호화할 경우 복잡해짐.
//    @Convert(converter = SecureStringConverter.class)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    // 서비스 성격에 따라 선택적 암호화
    @Convert(converter = SecureStringConverter.class)
    @Column(nullable = false, length = 255)
    private String name;

    @Convert(converter = SecureStringConverter.class)
    @Column(nullable = false, length = 255)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

}
