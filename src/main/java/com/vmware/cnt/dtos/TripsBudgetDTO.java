package com.vmware.cnt.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class TripsBudgetDTO {
    private Long leftover;
    private String currency;
    private Long destinationVisits;

    @Singular
    private List<DestinationDTO> destinations;
}

