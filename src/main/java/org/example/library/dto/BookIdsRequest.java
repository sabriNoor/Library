package org.example.library.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookIdsRequest(
        @NotNull
        @NotEmpty
        List<Long> bookIds
) {
}
