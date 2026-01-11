package org.acme.reservation.rental;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author miller
 * @version 1.0.0
 * @since 2025/12/26
 */
@Data
public class Rental {
    private final String id;
    private final String userId;
    private final Long reservationId;
    private final LocalDate startDate;
}
