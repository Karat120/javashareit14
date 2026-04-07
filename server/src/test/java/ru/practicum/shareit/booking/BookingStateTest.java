package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingStateTest {
    @Test
    void shouldContainAllStates() {
        assertTrue(BookingState.valueOf("ALL") == BookingState.ALL);
        assertTrue(BookingState.valueOf("CURRENT") == BookingState.CURRENT);
        assertTrue(BookingState.valueOf("PAST") == BookingState.PAST);
        assertTrue(BookingState.valueOf("FUTURE") == BookingState.FUTURE);
        assertTrue(BookingState.valueOf("WAITING") == BookingState.WAITING);
        assertTrue(BookingState.valueOf("REJECTED") == BookingState.REJECTED);
    }
}
