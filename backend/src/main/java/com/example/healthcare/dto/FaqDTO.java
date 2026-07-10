package com.example.healthcare.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqDTO {
    private Long id;
    private String question;
    private String answer;
}
