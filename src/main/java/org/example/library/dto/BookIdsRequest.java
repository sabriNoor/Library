package org.example.library.dto;

import java.util.List;

public record BookIdsRequest(
        List<Long> bookIds
) {
}
