package org.acme.reservation.billing;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.reservation.constant.KafkaTopic;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class BillingService {
    @Incoming(KafkaTopic.INVOICES)
    public void processInvoice(Invoice invoice) {
        Log.infof("Processed incoming invoice: %s" + invoice.toString());
    }
}
