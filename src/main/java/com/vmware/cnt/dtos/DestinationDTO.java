package com.vmware.cnt.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DestinationDTO {
    private String country;
    private String currency;
    private Double cost;
}