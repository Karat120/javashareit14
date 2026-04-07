package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
}
