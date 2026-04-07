package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void serialize_shouldWriteCreatedDateAndText() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setId(3L);
        dto.setAuthorName("Alice");
        dto.setText("Nice item");
        dto.setCreated(LocalDateTime.of(2026, 4, 7, 12, 15, 0));

        var written = json.write(dto);

        assertThat(written).extractingJsonPathNumberValue("$.id").isEqualTo(3);
        assertThat(written).extractingJsonPathStringValue("$.authorName").isEqualTo("Alice");
        assertThat(written).extractingJsonPathStringValue("$.created").startsWith("2026-04-07T12:15:00");
    }
}
