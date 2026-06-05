package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.MeterReading;
import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.dto.MeterReadingResponse;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingMapper {
    public MeterReadingResponse toResponse(MeterReading reading) {
        User recordedBy = reading.getRecordedBy();
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .readingDate(reading.getReadingDate())
                .recordedByName(recordedBy != null ? recordedBy.getFullName() : null)
                .recordedByEmail(recordedBy != null ? recordedBy.getEmail() : null)
                .createdAt(reading.getCreatedAt())
                .build();
    }
}
