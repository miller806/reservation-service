package org.acme.reservation.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.acme.reservation.entity.Reservation;

@Data
@AllArgsConstructor
public class Invoice {
    public Reservation reservation;
    public Double price;
}
