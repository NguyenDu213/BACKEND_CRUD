package com.backend.backend_crud.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'H:m:s"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'H:m"),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText();

        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        // Thử parse với các formatter khác nhau
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
                    // Chỉ có ngày, chuyển thành LocalDateTime với thời gian 00:00:00
                    LocalDate localDate = LocalDate.parse(date, formatter);
                    return localDate.atStartOfDay();
                } else {
                    return LocalDateTime.parse(date, formatter);
                }
            } catch (DateTimeParseException e) {
                // Thử formatter tiếp theo
            }
        }

        throw new IOException("Không thể parse ngày tháng: " + date + ". Định dạng hợp lệ: yyyy-MM-dd hoặc yyyy-MM-dd'T'HH:mm:ss");
    }
}
