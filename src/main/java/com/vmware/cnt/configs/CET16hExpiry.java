package com.vmware.cnt.configs;

import org.ehcache.expiry.ExpiryPolicy;

import java.time.*;
import java.util.function.Supplier;

public class CET16hExpiry implements ExpiryPolicy {

    private static final int EXPIRY_HOUR = 16;

    @Override
    public Duration getExpiryForCreation(Object key, Object value) {
        final ZoneId cet = ZoneId.of("CET");
        LocalDateTime now = LocalDateTime.now(cet);

        LocalDateTime expiryDateTime = LocalDateTime.of(
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                EXPIRY_HOUR, 0, 0, 0);
        if (now.getHour() >= EXPIRY_HOUR) {
            expiryDateTime = expiryDateTime.plusDays(1);
        }

        return Duration.between(now, expiryDateTime);
    }

    @Override
    public Duration getExpiryForAccess(Object key, Supplier value) {
        return null;
    }

    @Override
    public Duration getExpiryForUpdate(Object key, Supplier oldValue, Object newValue) {
        return null;
    }

}
