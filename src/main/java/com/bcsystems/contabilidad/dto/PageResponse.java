package com.bcsystems.contabilidad.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page >= totalPages - 1,
                page == 0
        );
    }
}
