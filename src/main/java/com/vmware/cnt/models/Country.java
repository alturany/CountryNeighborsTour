package com.vmware.cnt.models;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class Country {
    private String name;
    private String alpha2Code;
    @Singular
    private List<String> borders;
    @Singular
    private List<String> currencies;
}
