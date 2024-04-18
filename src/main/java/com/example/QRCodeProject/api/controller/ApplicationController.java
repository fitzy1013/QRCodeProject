package com.example.QRCodeProject.api.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.QRCodeProject.api.service.ApplicationService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class ApplicationController {
    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthStatus() {
        return ResponseEntity.status(200).body("");
    }

    @GetMapping(path = "/qrcode")
    public ResponseEntity<byte[]> getImage(@RequestParam(required = false, defaultValue = "250") Integer size , @RequestParam(required = false, defaultValue = "png") String type, @RequestParam String contents, @RequestParam(required = false, defaultValue = "L") Character correction ) {
        Character[] allowedCorrectionLevels = {'L', 'M', 'Q', 'H'};
        if (contents.isBlank()) {
            String errorJson = "{\"error\": \"Contents cannot be null or blank\"}";
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }

        if (size < 150 || size > 350) {
            String errorJson = "{\"error\": \"Image size must be between 150 and 350 pixels\"}";
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }

        if (!Arrays.asList(allowedCorrectionLevels).contains(correction)) {
            String errorJson = "{\"error\": \"Permitted error correction levels are L, M, Q, H\"}";
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }

        if (!type.equals("png") && !type.equals("gif") && !type.equals("jpeg")) {
            String errorJson = "{\"error\": \"Only png, jpeg and gif image types are supported\"}";
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }

        BufferedImage bufferedImage;
        bufferedImage = getBufferedImage(size, contents, correction);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, type, baos);
            byte[] bytes = baos.toByteArray();
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType("image/" + type))
                    .body(bytes);
        } catch (IOException e) {
            String errorJson = "{\"error\": \"Error processing the image.\"}";
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }
    }


    @Bean
    public HttpMessageConverter<BufferedImage> bufferedImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

    public BufferedImage getBufferedImage(int size, String data, Character correction) {
        Map<EncodeHintType, ?> hint = Map.of(EncodeHintType.ERROR_CORRECTION, getErrorCorrectionLevel(correction));
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size,size, hint);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            return null;
        }
    }

    private ErrorCorrectionLevel getErrorCorrectionLevel (Character correction) {
        return switch (correction) {
            case 'L' -> ErrorCorrectionLevel.L;
            case 'M' -> ErrorCorrectionLevel.M;
            case 'Q' -> ErrorCorrectionLevel.Q;
            case 'H' -> ErrorCorrectionLevel.H;
            default -> throw new IllegalArgumentException("Unsupported error correction level: " + correction);
        };
    }

}
