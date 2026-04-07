package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Validated
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String HEADER_USER = "X-Sharer-User-Id";
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER) Long userId,
                                         @Valid @RequestBody ItemRequestDto dto) {
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(HEADER_USER) Long userId) {
        return requestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getOthers(@RequestHeader(HEADER_USER) Long userId,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "20") Integer size) {
        return requestClient.getOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(HEADER_USER) Long userId,
                                          @PathVariable Long requestId) {
        return requestClient.getById(userId, requestId);
    }
}
