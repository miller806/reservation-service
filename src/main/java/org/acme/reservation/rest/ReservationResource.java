package org.acme.reservation.rest;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.InventoryClient;
import org.acme.reservation.rental.Rental;
import org.acme.reservation.rental.RentalClient;
import org.acme.reservation.reservation.Reservation;
import org.acme.reservation.reservation.ReservationsRepository;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("reservation")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {
    @Inject
    InventoryClient inventoryClient;
    @Inject
    ReservationsRepository reservationsRepository;
    @Inject
    @RestClient
    RentalClient rentalClient;


    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Reservation make(Reservation reservation) {
        Reservation result = reservationsRepository.save(reservation);
        String userId = "x";
        if (reservation.startDay.equals(LocalDate.now())) {
            Rental rental = rentalClient.start(userId, result.id);
            Log.info("Start rental: " + rental);
        }
        return result;
    }

    @GET
    @Path("availability")
    public Collection<Car> availability(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
        List<Car> carList = inventoryClient.allCars();
        Map<Long, Car> carMap = new HashMap<>();
        for (Car car : carList) {
            carMap.put(car.id, car);
        }

        List<Reservation> reservationList = reservationsRepository.findAll();
        reservationList.forEach(reservation -> {
            carMap.remove(reservation.carId);
        });

        return carMap.values();
    }
}
