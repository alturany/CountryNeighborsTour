package com.vmware.cnt.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Country {
    private String name;
    private String alpha2Code;
    private List<String> borders;
    private List<String> currencies;
}
