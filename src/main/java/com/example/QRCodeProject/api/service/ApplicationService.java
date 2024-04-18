package com.example.QRCodeProject.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApplicationService {
    private final RestTemplate restTemplate;

    public ApplicationService() {
        this.restTemplate = new RestTemplate();
    }


}
