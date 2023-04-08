package com.project.imgcrawler.services;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse extends Account {
    private boolean error;
    private String message;
}