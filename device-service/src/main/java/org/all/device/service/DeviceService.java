package org.all.device.service;

import org.all.device.dto.DeviceRequest;
import org.all.device.dto.DeviceResponse;

import java.util.List;

public interface DeviceService {

    DeviceResponse createDevice(DeviceRequest request);

    DeviceResponse getDeviceById(Long id);

    DeviceResponse getDeviceByCode(String deviceCode);

    List<DeviceResponse> getAllDevices();

    List<DeviceResponse> getDevicesByStatus(String status);

    List<DeviceResponse> getDevicesByType(String deviceType);

    DeviceResponse updateDevice(Long id, DeviceRequest request);

    void deleteDevice(Long id);

    void deleteDeviceByCode(String deviceCode);

    DeviceResponse updateDeviceStatus(Long id, String status);

    DeviceResponse updateHeartbeat(String deviceCode);
}