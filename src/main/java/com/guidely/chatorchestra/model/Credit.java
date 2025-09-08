package com.guidely.chatorchestra.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a credit entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
    private String role;
    private String name;
}




