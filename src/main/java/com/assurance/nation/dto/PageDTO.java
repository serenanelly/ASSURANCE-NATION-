package com.assurance.nation.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Réponse paginée standard pour les listes API.
 */
@Data
public class PageDTO<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageDTO<T> of(Page<T> page) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setContent(page.getContent());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        return dto;
    }

    public static <T, R> PageDTO<R> of(Page<T> page, List<R> mapped) {
        PageDTO<R> dto = new PageDTO<>();
        dto.setContent(mapped);
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        return dto;
    }
}
