package com.mediscan.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Lightweight pagination wrapper for list endpoints.
 *
 * @param <T> type of each item in the page
 */
@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
