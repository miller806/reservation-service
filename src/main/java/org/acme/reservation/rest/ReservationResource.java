package org.acme.reservation.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.InventoryClient;
import org.acme.reservation.reservation.Reservation;
import org.acme.reservation.reservation.ReservationsRepository;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("reservation")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {
    private final InventoryClient inventoryClient;
    private final ReservationsRepository reservationsRepository;

    public ReservationResource(InventoryClient inventoryClient, ReservationsRepository reservationsRepository) {
        this.inventoryClient = inventoryClient;
        this.reservationsRepository = reservationsRepository;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Reservation reservation(Reservation reservation) {
        return reservationsRepository.save(reservation);
    }
}
