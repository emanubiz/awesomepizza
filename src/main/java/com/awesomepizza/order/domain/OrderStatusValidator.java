package com.awesomepizza.order.domain;

import com.awesomepizza.common.exception.InvalidOrderStatusException;
import org.springframework.stereotype.Component;
import com.awesomepizza.order.domain.enums.OrderStatus;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validatore per le transizioni di stato degli ordini.
 * Implementa una macchina a stati per garantire che solo transizioni valide siano permesse.
 */
@Component
public class OrderStatusValidator {

    // Mappa delle transizioni valide: stato corrente -> stati successivi permessi
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING, EnumSet.of(
            OrderStatus.IN_PREPARATION,  // Il pizzaiolo prende in carico
            OrderStatus.CANCELED         // Il cliente cancella
        ),
        OrderStatus.IN_PREPARATION, EnumSet.of(
            OrderStatus.READY,           // La pizza è pronta
            OrderStatus.CANCELED         // Annullamento eccezionale
        ),
        OrderStatus.READY, EnumSet.of(
            OrderStatus.COMPLETED        // Ordine consegnato/ritirato
        ),
        OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class), // Stato finale
        OrderStatus.CANCELED, EnumSet.noneOf(OrderStatus.class)   // Stato finale
    );

    /**
     * Valida se una transizione di stato è permessa
     * 
     * @param currentStatus lo stato corrente dell'ordine
     * @param newStatus il nuovo stato desiderato
     * @throws InvalidOrderStatusException se la transizione non è valida
     */
    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new InvalidOrderStatusException("Current status and new status cannot be null");
        }

        if (currentStatus == newStatus) {
            return; // Transizione verso lo stesso stato è permessa (no-op)
        }

        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new InvalidOrderStatusException(
                String.format("Invalid status transition from %s to %s. Allowed transitions: %s",
                    currentStatus,
                    newStatus,
                    allowedTransitions != null ? allowedTransitions : "none"
                )
            );
        }
    }

    /**
     * Verifica se un ordine può essere modificato dal cliente
     * 
     * @param currentStatus lo stato corrente dell'ordine
     * @return true se l'ordine può essere modificato, false altrimenti
     */
    public boolean canBeModifiedByCustomer(OrderStatus currentStatus) {
        return currentStatus == OrderStatus.PENDING;
    }

    /**
     * Verifica se un ordine può essere preso in carico dal pizzaiolo
     * 
     * @param currentStatus lo stato corrente dell'ordine
     * @return true se l'ordine può essere preso in carico, false altrimenti
     */
    public boolean canBeTakenByPizzaiolo(OrderStatus currentStatus) {
        return currentStatus == OrderStatus.PENDING;
    }
}