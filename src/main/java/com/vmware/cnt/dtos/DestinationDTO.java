package com.vmware.cnt.dtos;

import lombok.Data;

@Data
public class DestinationDTO {
    private String country;
    private String currency;
    private Double cost;
}