package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serialize_shouldWriteDateFieldsAndStatus() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setItemId(2L);
        dto.setStatus(BookingStatus.WAITING);
        dto.setStart(LocalDateTime.of(2026, 4, 7, 10, 0, 0));
        dto.setEnd(LocalDateTime.of(2026, 4, 8, 10, 0, 0));

        var written = json.write(dto);

        assertThat(written).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(written).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(written).extractingJsonPathStringValue("$.start").startsWith("2026-04-07T10:00:00");
        assertThat(written).extractingJsonPathStringValue("$.end").startsWith("2026-04-08T10:00:00");
    }

    @Test
    void deserialize_shouldReadDateFields() throws Exception {
        String content = """
                {
                  "id": 5,
                  "itemId": 9,
                  "status": "APPROVED",
                  "start": "2026-04-10T09:30:00",
                  "end": "2026-04-11T09:30:00"
                }
                """;

        BookingDto parsed = json.parseObject(content);

        assertThat(parsed.getId()).isEqualTo(5L);
        assertThat(parsed.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(parsed.getStart()).isEqualTo(LocalDateTime.of(2026, 4, 10, 9, 30, 0));
    }
}
