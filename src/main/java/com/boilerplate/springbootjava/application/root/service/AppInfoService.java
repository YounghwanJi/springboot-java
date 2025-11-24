package com.boilerplate.springbootjava.application.root.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.AppInfoResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.BuildInformation;
import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.GitCommit;
import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.GitInformation;
import com.boilerplate.springbootjava.application.root.port.in.AppInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppInfoService implements AppInfoUseCase {
    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    @Value("${spring.profiles.active}")
    private String activeProfile;


    @Override
    public AppInfoResponseDto getApplicationInformation() {
        return AppInfoResponseDto.builder()
                .build(
                        BuildInformation.builder()
                                .name(buildProperties.getName())
                                .time(buildProperties.getTime())
                                .version(buildProperties.getVersion())
                                .profile(activeProfile)
                                .build()
                )
                .git(
                        GitInformation.builder()
                                .branch(gitProperties.getBranch())
                                .commit(
                                        GitCommit.builder()
                                                .id(gitProperties.getShortCommitId())
                                                .time(gitProperties.getCommitTime())
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
