package com.mediscan.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standard envelope for all successful API responses.
 *
 * @param <T> type of the payload returned in {@code data}
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private final boolean success = true;

    private final String message;

    private final T data;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> of(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> of(String message) {
        return ApiResponse.<T>builder().success(true).message(message).build();
    }
}
