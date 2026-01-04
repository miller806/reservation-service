package org.acme.reservation.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@ToString
public class Reservation extends PanacheEntity {
    public String userId;
    public Long carId;
    public LocalDate startDay;
    public LocalDate endDay;

    public boolean isReserved(LocalDate startDay, LocalDate endDay) {
        return (!(this.endDay.isBefore(startDay) || this.startDay.isAfter(endDay)));
    }

    public static List<Reservation> findByCarId(Long carId) {
        return list("catId", carId);
    }
}
