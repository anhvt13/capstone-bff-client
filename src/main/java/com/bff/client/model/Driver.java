package com.bff.client.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Driver implements Serializable {

    private Integer id;

    @NotBlank (message = "Name is required")
    private String name;

    @NotBlank (message = "Team is required")
    private String team;
}
