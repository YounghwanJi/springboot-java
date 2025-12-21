package com.boilerplate.springbootjava.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

//@OpenAPIDefinition(
//        info = @Info(title = "API Documentation", version = "1.0", description = "API Documentation")
//)
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public OpenAPI openApi() {
        String description = buildStyledDescription();
        String version = (buildProperties != null) ? buildProperties.getVersion() : "N/A";
        String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .addSecurityItem(
                        new SecurityRequirement().addList(securitySchemeName)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new io.swagger.v3.oas.models.info.Info()
                                .title("My API")
                                .description(description)
                                .version(version)
//                        .contact(new io.swagger.v3.oas.models.info.Contact()
//                                .name("개발팀")
//                                .email("dev@example.com"))
                );
    }

    // 그룹별 API 설정 (User API)
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("User-API")
                .pathsToMatch("/api/**")
                .build();
    }

    // 그룹별 API 설정 (Dev API)
    @Bean
    public GroupedOpenApi devApi() {
        return GroupedOpenApi.builder()
                .group("Dev-API")
                .pathsToMatch("/dev/**")
                .build();
    }

    private String buildStyledDescription() {
        StringBuilder sb = new StringBuilder();

        // 스타일 정의
        sb.append("<style>")
                .append(".info-table { ")
                .append("border-collapse: collapse; ")
                .append("width: 100%; ")
                .append("max-width: 800px; ")
                .append("margin: 20px 0; ")
                .append("font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; ")
                .append("box-shadow: 0 2px 8px rgba(0,0,0,0.1); ")
                .append("border-radius: 8px; ")
                .append("overflow: hidden; ")
                .append("} ")
                .append(".info-table tr { ")
                .append("border-bottom: 1px solid #e0e0e0; ")
                .append("} ")
                .append(".info-table tr:last-child { ")
                .append("border-bottom: none; ")
                .append("} ")
                .append(".info-table tr:nth-child(even) { ")
                .append("background-color: #f8f9fa; ")
                .append("} ")
                .append(".info-table tr:hover { ")
                .append("background-color: #e3f2fd; ")
                .append("transition: background-color 0.2s; ")
                .append("} ")
                .append(".info-table td { ")
                .append("padding: 12px 16px; ")
                .append("} ")
                .append(".info-table .label { ")
                .append("font-weight: 600; ")
                .append("color: #1976d2; ")
                .append("width: 180px; ")
                .append("white-space: nowrap; ")
                .append("} ")
                .append(".info-table .value { ")
                .append("color: #424242; ")
                .append("} ")
                .append(".profile-badge { ")
                .append("display: inline-block; ")
                .append("padding: 4px 12px; ")
                .append("border-radius: 12px; ")
                .append("font-size: 0.9em; ")
                .append("font-weight: 600; ")
                .append("text-transform: uppercase; ")
                .append("} ")
                .append(".profile-local { background-color: #4caf50; color: white; } ")
                .append(".profile-dev { background-color: #2196f3; color: white; } ")
                .append(".profile-staging { background-color: #ff9800; color: white; } ")
                .append(".profile-prod { background-color: #f44336; color: white; } ")
                .append(".profile-default { background-color: #9e9e9e; color: white; } ")
                .append(".section-header { ")
                .append("background-color: #1976d2 !important; ")
                .append("color: white !important; ")
                .append("font-weight: bold; ")
                .append("font-size: 1.1em; ")
                .append("} ")
                .append(".section-header td { ")
                .append("padding: 14px 16px !important; ")
                .append("} ")
                .append("</style>");

        // 테이블 시작
        sb.append("<table class='info-table'>");

        // Application Section
        sb.append("<tr class='section-header'><td colspan='2'>📱 Application Information</td></tr>");

        sb.append("<tr>")
                .append("<td class='label'>Profile</td>")
                .append("<td class='value'>")
                .append(getProfileBadge(activeProfile))
                .append("</td></tr>");

        if (buildProperties != null) {
            sb.append("<tr>")
                    .append("<td class='label'>Group</td>")
                    .append("<td class='value'>").append(buildProperties.getGroup()).append("</td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Artifact</td>")
                    .append("<td class='value'>").append(buildProperties.getArtifact()).append("</td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Name</td>")
                    .append("<td class='value'>").append(buildProperties.getName()).append("</td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Version</td>")
                    .append("<td class='value'><strong>").append(buildProperties.getVersion()).append("</strong></td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Build Time</td>")
                    .append("<td class='value'>")
                    .append(buildProperties.getTime()
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")))
                    .append("</td>")
                    .append("</tr>");
        }

        // Git Section
        if (gitProperties != null) {
            sb.append("<tr class='section-header'><td colspan='2'>🔧 Git Information</td></tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Branch</td>")
                    .append("<td class='value'><code>").append(gitProperties.getBranch()).append("</code></td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Commit ID</td>")
                    .append("<td class='value'><code>").append(gitProperties.getShortCommitId()).append("</code></td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Commit Message</td>")
                    .append("<td class='value'>").append(escapeHtml(gitProperties.get("commit.message.short"))).append("</td>")
                    .append("</tr>");

            sb.append("<tr>")
                    .append("<td class='label'>Commit Time</td>")
                    .append("<td class='value'>")
                    .append(gitProperties.getCommitTime()
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")))
                    .append("</td>")
                    .append("</tr>");

            // TODO: 적당히 제거 필요.
            // git.commit.user.xxx 동작 안 하는 것으로 보임. 존재는 하는 것 같은데, 어떤 설정이 문제인지 알 수 없음.
            // build.gradle에 keys를 추가해보아도 안 되어 일단 제거.
            // https://github.com/n0mer/gradle-git-properties
            sb.append("<tr>")
                    .append("<td class='label'>Committer</td>")
                    .append("<td class='value'>")
                    .append(gitProperties.get("git.commit.user.name"))
                    .append("(")
                    .append(gitProperties.get("git.commit.user.email"))
                    .append(")")
                    .append("</td>")
                    .append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }

    private String getProfileBadge(String profile) {
        String badgeClass = switch (profile.toLowerCase()) {
            case "local" -> "profile-local";
            case "dev", "development" -> "profile-dev";
            case "staging", "stg" -> "profile-staging";
            case "prod", "production" -> "profile-prod";
            default -> "profile-default";
        };

        return "<span class='profile-badge " + badgeClass + "'>" + profile.toUpperCase() + "</span>";
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}