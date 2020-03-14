package com.vmware.cnt.models;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class Conversion {
    @Singular
    private Map<String, Double> rates;
    private String base;
    private LocalDate date;
}
