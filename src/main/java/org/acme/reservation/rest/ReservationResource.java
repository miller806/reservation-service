package org.acme.reservation.rest;

import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.reservation.entity.Reservation;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.GraphQLInventoryClient;
import org.acme.reservation.rental.Rental;
import org.acme.reservation.rental.RentalClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("reservation")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {
    @Inject
    @GraphQLClient("inventory")
    GraphQLInventoryClient inventoryClient;
    @Inject
    @RestClient
    RentalClient rentalClient;
    @Inject
    SecurityContext securityContext;


    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Transactional
    public Reservation make(Reservation reservation) {
        reservation.userId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "anonymous";
        reservation.persist();
        if (reservation.startDay.equals(LocalDate.now())) {
            Rental rental = rentalClient.start(reservation.userId, reservation.id);
            Log.info("Start rental: " + rental);
        }
        return reservation;
    }

    @GET
    @Path("availability")
    public Collection<Car> availability(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
        List<Car> carList = inventoryClient.allCars();
        Map<Long, Car> carMap = new HashMap<>();
        for (Car car : carList) {
            carMap.put(car.id, car);
        }

        List<Reservation> reservationList = Reservation.listAll();
        reservationList.forEach(reservation -> {
            carMap.remove(reservation.carId);
        });

        return carMap.values();
    }

    @GET
    @Path("all")
    public Collection<Reservation> allReservations() {
        String userId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        return Reservation.<Reservation>streamAll()
                .filter(reservation -> userId == null || userId.equals(reservation.userId))
                .collect(Collectors.toList());
    }
}
