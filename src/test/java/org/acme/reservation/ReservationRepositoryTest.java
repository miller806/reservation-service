package org.acme.reservation;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.reservation.entity.Reservation;
import org.acme.reservation.reservation.ReservationsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@QuarkusTest
public class ReservationRepositoryTest {
    @Inject
    ReservationsRepository reservationsRepository;

    @Test
    public void testCreateReservation() {
        Reservation reservation = new Reservation();
        reservation.startDay = LocalDate.now().plusDays(5);
        reservation.endDay = LocalDate.now().plusDays(12);
        reservation.carId = 384L;
        reservationsRepository.save(reservation);
        Assertions.assertNotNull(reservation.id);
        Assertions.assertTrue(reservationsRepository.findAll().contains(reservation));
    }
}
