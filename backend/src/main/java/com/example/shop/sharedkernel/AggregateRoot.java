package com.example.shop.sharedkernel;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots.
 * <ul>
 *   <li>Collects domain events raised by behaviour methods; the application service
 *       publishes them after saving (see {@link #pullDomainEvents()}).</li>
 *   <li>Carries a JPA {@code @Version} column for optimistic locking — the aggregate is the
 *       consistency boundary, so concurrent edits to the same aggregate must conflict.</li>
 * </ul>
 * Deliberately not using Spring Data's {@code AbstractAggregateRoot} so the domain has no Spring dependency.
 */
@MappedSuperclass
public abstract class AggregateRoot {

    @Version
    private long version;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /** Returns and clears pending events. Called once per transaction by the application layer. */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public long getVersion() {
        return version;
    }
}
