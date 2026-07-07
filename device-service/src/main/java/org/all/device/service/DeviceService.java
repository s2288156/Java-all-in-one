package org.all.device.service;

import org.all.common.model.PageResponse;
import org.all.device.dto.DeviceRequest;
import org.all.device.dto.DeviceResponse;

public interface DeviceService {

    DeviceResponse createDevice(DeviceRequest request);

    DeviceResponse getDeviceById(Long id);

    DeviceResponse getDeviceByCode(String deviceCode);

    PageResponse<DeviceResponse> getAllDevices(int page, int size);

    PageResponse<DeviceResponse> getDevicesByStatus(String status, int page, int size);

    PageResponse<DeviceResponse> getDevicesByType(String deviceType, int page, int size);

    DeviceResponse updateDevice(Long id, DeviceRequest request);

    void deleteDevice(Long id);

    void deleteDeviceByCode(String deviceCode);

    DeviceResponse updateDeviceStatus(Long id, String status);

    DeviceResponse updateHeartbeat(String deviceCode);
}
