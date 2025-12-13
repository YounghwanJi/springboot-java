package com.boilerplate.springbootjava.application.user.port.in;

import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserUpdateRequestDto;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserUseCase {

    UserResponseDto createUser(UserCreateRequestDto request);

    UserResponseDto getUser(Long id) ;

    PageResponseDto<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto updateUser(Long id, UserUpdateRequestDto request);

    void deleteUser(Long id);
}
