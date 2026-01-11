package org.acme.reservation.rest;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.reservation.billing.Invoice;
import org.acme.reservation.constant.KafkaTopic;
import org.acme.reservation.entity.Reservation;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.GraphQLInventoryClient;
import org.acme.reservation.rental.RentalClient;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
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
    @Inject
    @Channel(KafkaTopic.INVOICES)
    MutinyEmitter<Invoice> invoiceEmitter;
    public static final double STANDARD_RATE_PER_DAY = 19.99;


    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @WithTransaction
    public Uni<Reservation> make(Reservation reservation) {
        reservation.userId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "anonymous";
        return reservation.<Reservation>persist().onItem().call(perssitReservation -> {
            Log.infof("Making reservation %s", reservation);
            Uni<Void> invoiceCs = invoiceEmitter.send(new Invoice(reservation, computePrice(perssitReservation)))
                    .onFailure().invoke(throwable -> {
                        Log.errorf("Failed to send invoice to reservation %s,Message %s%n", reservation, throwable.getMessage());

                    });
            if (perssitReservation.startDay.equals(LocalDate.now())) {
                return invoiceCs.chain(() -> rentalClient.start(perssitReservation.userId, perssitReservation.id)
                        .onItem()
                        .invoke(rental -> {
                            Log.infof("Successfully started rental %s", rental);

                        })
                        .replaceWith(perssitReservation));
            }
            return invoiceCs.replaceWith(perssitReservation);
        });
    }

    private Double computePrice(Reservation reservation) {
        return (ChronoUnit.DAYS.between(reservation.startDay, reservation.endDay) + 1) * STANDARD_RATE_PER_DAY;
    }

    @GET
    @Path("availability")
    public Uni<Collection<Car>> availability(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
        Uni<List<Car>> avalableCarsUni = inventoryClient.allCars();
        Uni<List<Reservation>> reservationsUni = Reservation.listAll();
        return Uni.combine().all().unis(avalableCarsUni, reservationsUni).with((availableCars, reservations) -> {
            Map<Long, Car> carMap = new HashMap<>();
            for (Car car : availableCars) {
                carMap.put(car.id, car);
            }
            for (Reservation reservation : reservations) {
                if (reservation.isReserved(reservation.startDay, reservation.endDay)) {
                    carMap.remove(reservation.id);
                }
            }
            return carMap.values();
        });
    }

    @GET
    @Path("all")
    public Uni<Collection<Reservation>> allReservations() {
        String userId = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        return Reservation.<Reservation>listAll().onItem().transform(reservations -> reservations.stream().filter(reservation -> userId == null || userId.equals(reservation.userId)).collect(Collectors.toList()));
    }
}
