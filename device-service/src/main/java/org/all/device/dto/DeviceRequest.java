package org.all.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequest {

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @NotBlank(message = "设备状态不能为空")
    @Pattern(regexp = "^(online|offline|maintenance)$", message = "设备状态只能是 online、offline 或 maintenance")
    private String status;

    private String location;

    private String ipAddress;

    private String macAddress;

    private String serialNumber;

    private String manufacturer;

    private String model;

    private LocalDateTime installationDate;

    private LocalDateTime lastHeartbeat;
}