//package org.acme.reservation;
//
//import io.quarkus.test.junit.QuarkusTest;
//import io.quarkus.test.vertx.RunOnVertxContext;
//import jakarta.inject.Inject;
//import org.acme.reservation.entity.Reservation;
//import org.acme.reservation.reservation.ReservationsRepository;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//
//@QuarkusTest
//public class ReservationPersistenceTest {
//    @Inject
////    ReservationsRepository reservationsRepository;
//
//    @Test
//    @RunOnVertxContext
//    public void testCreateReservation() {
//        Reservation reservation = new Reservation();
//        reservation.startDay = LocalDate.now().plus(5, ChronoUnit.DAYS);
//        reservation.endDay = LocalDate.now().plus(12, ChronoUnit.DAYS);
//        reservation.carId = 384L;
////        asserter.<Reservation>assertThat(() -> reservation.persist(), r -> {
////            Assertions.assertNotNull(r.id);
////            asserter.putData("reservation.id", r.id);
////        });
////        asserter.assertEquals(() -> Reservation.count(), 1L);
////        asserter.assertThat(() -> Reservation.<Reservation>findById(asserter.getData("reservation.id")), persistedReservation -> {
////            Assertions.assertNotNull(persistedReservation);
////            Assertions.assertEquals(reservation.carId, persistedReservation.carId);
////        });
//    }
//}
