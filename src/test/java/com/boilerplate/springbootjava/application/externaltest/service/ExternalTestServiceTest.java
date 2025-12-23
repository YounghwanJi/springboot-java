package com.boilerplate.springbootjava.application.externaltest.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemPageResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemUpdateRequestDto;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import com.boilerplate.springbootjava.infrastructure.external.factory.ItemExternalRestClientFactory;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalRequestSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExternalTestService 단위 테스트
 * - ItemExternalRestClientFactory Mock 사용
 * - 외부 API 호출 시뮬레이션
 * - CRUD 동작 검증
 */
@ExtendWith(MockitoExtension.class)
class ExternalTestServiceTest {

    @Mock
    private ItemExternalRestClientFactory itemExternalRestClientFactory;

    @InjectMocks
    private ExternalTestService externalTestService;

    // ========== 헬퍼 메서드 ==========

    private ExternalItemCreateRequestDto createItemRequest() {
        return new ExternalItemCreateRequestDto(
                "Test Item",
                "Test Description",
                10000
        );
    }

    private ExternalItemResponseDto createItemResponse(Long id) {
        return new ExternalItemResponseDto(
                id,
                "Test Item",
                "Test Description",
                10000,
                true
        );
    }

    // ========== 외부 아이템 생성 테스트 ==========

    @Test
    @DisplayName("외부 아이템 생성 - 정상 동작")
    void createExternalItem_Success() {
        // given
        ExternalItemCreateRequestDto request = createItemRequest();
        ExternalItemResponseDto expectedResponse = createItemResponse(1L);

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemResponseDto.class)
        )).thenReturn(expectedResponse);

        // when
        ExternalItemResponseDto response = externalTestService.createExternalItem(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Item");
        assertThat(response.description()).isEqualTo("Test Description");
        assertThat(response.price()).isEqualTo(10000);
        assertThat(response.created()).isTrue();

        verify(itemExternalRestClientFactory).execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemResponseDto.class)
        );
    }

    @Test
    @DisplayName("외부 아이템 생성 - 요청 검증")
    void createExternalItem_VerifyRequest() {
        // given
        ExternalItemCreateRequestDto request = new ExternalItemCreateRequestDto(
                "New Item",
                "New Description",
                5000
        );
        ExternalItemResponseDto expectedResponse = createItemResponse(1L);

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemResponseDto.class)
        )).thenReturn(expectedResponse);

        // when
        externalTestService.createExternalItem(request);

        // then - 요청이 올바르게 전달되었는지 확인
        verify(itemExternalRestClientFactory).execute(
                argThat(spec ->
                        spec.getPath().equals("/api/items") &&
                        spec.getMethod().name().equals("POST") &&
                        spec.getBody() == request
                ),
                eq(ExternalItemResponseDto.class)
        );
    }

    // ========== 외부 아이템 조회 테스트 ==========

    @Test
    @DisplayName("외부 아이템 단건 조회 - 정상 동작")
    void getExternalItem_Success() {
        // given
        Long itemId = 1L;
        ExternalItemResponseDto expectedResponse = createItemResponse(itemId);

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemResponseDto.class)
        )).thenReturn(expectedResponse);

        // when
        ExternalItemResponseDto response = externalTestService.getExternalItem(itemId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(itemId);
        assertThat(response.name()).isEqualTo("Test Item");

        verify(itemExternalRestClientFactory).execute(
                argThat(spec ->
                        spec.getPath().equals("/api/items/" + itemId) &&
                        spec.getMethod().name().equals("GET")
                ),
                eq(ExternalItemResponseDto.class)
        );
    }

    @Test
    @DisplayName("외부 아이템 단건 조회 - 다양한 ID")
    void getExternalItem_VariousIds() {
        // given
        Long[] testIds = {1L, 99L, 1234L};

        for (Long testId : testIds) {
            ExternalItemResponseDto response = createItemResponse(testId);
            when(itemExternalRestClientFactory.execute(
                    any(ExternalRequestSpec.class),
                    eq(ExternalItemResponseDto.class)
            )).thenReturn(response);

            // when
            ExternalItemResponseDto result = externalTestService.getExternalItem(testId);

            // then
            assertThat(result.id()).isEqualTo(testId);
        }

        verify(itemExternalRestClientFactory, times(testIds.length)).execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemResponseDto.class)
        );
    }

    // ========== 외부 아이템 목록 조회 테스트 ==========

    @Test
    @DisplayName("외부 아이템 목록 조회 - 정상 동작")
    void getAllExternalItems_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        List<ExternalItemResponseDto> items = List.of(
                createItemResponse(1L),
                createItemResponse(2L),
                createItemResponse(3L)
        );

        ExternalItemPageResponseDto externalResponse = new ExternalItemPageResponseDto(
                3,      // total
                0,      // offset
                10,     // limit
                items
        );

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemPageResponseDto.class)
        )).thenReturn(externalResponse);

        // when
        PageResponseDto<ExternalItemResponseDto> response = externalTestService.getAllExternalItems(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(3);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(3);

        verify(itemExternalRestClientFactory).execute(
                argThat(spec ->
                        spec.getPath().equals("/api/items") &&
                        spec.getMethod().name().equals("GET") &&
                        spec.getQueryParams().get("limit").equals(10) &&
                        spec.getQueryParams().get("offset").equals(0)
                ),
                eq(ExternalItemPageResponseDto.class)
        );
    }

    @Test
    @DisplayName("외부 아이템 목록 조회 - 페이징 (2페이지)")
    void getAllExternalItems_WithPagination() {
        // given
        Pageable pageable = PageRequest.of(2, 20);  // 3번째 페이지, size=20

        List<ExternalItemResponseDto> items = List.of(
                createItemResponse(41L),
                createItemResponse(42L)
        );

        ExternalItemPageResponseDto externalResponse = new ExternalItemPageResponseDto(
                100,    // total
                40,     // offset (2 * 20)
                20,     // limit
                items
        );

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemPageResponseDto.class)
        )).thenReturn(externalResponse);

        // when
        PageResponseDto<ExternalItemResponseDto> response = externalTestService.getAllExternalItems(pageable);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.page()).isEqualTo(40);  // offset
        assertThat(response.size()).isEqualTo(20);  // limit
        assertThat(response.totalElements()).isEqualTo(100);

        verify(itemExternalRestClientFactory).execute(
                argThat(spec ->
                        spec.getQueryParams().get("limit").equals(20) &&
                        spec.getQueryParams().get("offset").equals(40)
                ),
                eq(ExternalItemPageResponseDto.class)
        );
    }

    @Test
    @DisplayName("외부 아이템 목록 조회 - 빈 결과")
    void getAllExternalItems_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        ExternalItemPageResponseDto externalResponse = new ExternalItemPageResponseDto(
                0,
                0,
                10,
                List.of()
        );

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemPageResponseDto.class)
        )).thenReturn(externalResponse);

        // when
        PageResponseDto<ExternalItemResponseDto> response = externalTestService.getAllExternalItems(pageable);

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);

        verify(itemExternalRestClientFactory).execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemPageResponseDto.class)
        );
    }

    // ========== 외부 아이템 수정 테스트 ==========

    @Test
    @DisplayName("외부 아이템 수정 - 정상 동작")
    void updateExternalItem_Success() {
        // given
        Long itemId = 1L;
        ExternalItemUpdateRequestDto updateRequest = new ExternalItemUpdateRequestDto(
                itemId,
                "Updated Item",
                "Updated Description",
                20000
        );

        ExternalItemResponseDto expectedResponse = new ExternalItemResponseDto(
                itemId,
                "Updated Item",
                "Updated Description",
                20000,
                false
        );

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(ExternalItemResponseDto.class)
        )).thenReturn(expectedResponse);

        // when
        ExternalItemResponseDto response = externalTestService.updateExternalItem(itemId, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(itemId);
        assertThat(response.name()).isEqualTo("Updated Item");
        assertThat(response.description()).isEqualTo("Updated Description");
        assertThat(response.price()).isEqualTo(20000);

        verify(itemExternalRestClientFactory).execute(
                argThat(spec ->
                        spec.getPath().equals("/api/items/" + itemId) &&
                        spec.getMethod().name().equals("PUT") &&
                        spec.getBody() == updateRequest
                ),
                eq(ExternalItemResponseDto.class)
        );
    }

    // ========== 외부 아이템 삭제 테스트 ==========

    @Test
    @DisplayName("외부 아이템 삭제 - 정상 동작")
    void deleteExternalItem_Success() {
        // given
        Long itemId = 1L;

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(Void.class)
        )).thenReturn(null);

        // when
        externalTestService.deleteExternalItem(itemId);

        // then
        verify(itemExternalRestClientFactory).execute(
                argThat(spec ->
                        spec.getPath().equals("/api/items/" + itemId) &&
                        spec.getMethod().name().equals("DELETE")
                ),
                eq(Void.class)
        );
    }

    @Test
    @DisplayName("외부 아이템 삭제 - 다양한 ID")
    void deleteExternalItem_VariousIds() {
        // given
        Long[] testIds = {1L, 50L, 999L};

        when(itemExternalRestClientFactory.execute(
                any(ExternalRequestSpec.class),
                eq(Void.class)
        )).thenReturn(null);

        // when
        for (Long testId : testIds) {
            externalTestService.deleteExternalItem(testId);
        }

        // then
        verify(itemExternalRestClientFactory, times(testIds.length)).execute(
                any(ExternalRequestSpec.class),
                eq(Void.class)
        );
    }
}
