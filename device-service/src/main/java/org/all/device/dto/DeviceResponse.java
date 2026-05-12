package org.all.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String status;

    private String location;

    private String ipAddress;

    private String macAddress;

    private String serialNumber;

    private String manufacturer;

    private String model;

    private LocalDateTime installationDate;

    private LocalDateTime lastHeartbeat;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}