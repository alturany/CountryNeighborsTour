package com.vmware.cnt.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Set;

@Data
@Builder
public class TripsBudgetDTO {
    private Long leftover;
    private String currency;
    private Long perDestinationVisits;

    @Singular
    private Set<DestinationDTO> destinations;
}

