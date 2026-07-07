package org.all.device.controller;

import jakarta.validation.Valid;
import org.all.common.model.ApiResponse;
import org.all.common.model.PageResponse;
import org.all.device.dto.DeviceRequest;
import org.all.device.dto.DeviceResponse;
import org.all.device.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceResponse>> createDevice(@Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(@PathVariable Long id) {
        DeviceResponse response = deviceService.getDeviceById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/code/{deviceCode}")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceByCode(@PathVariable String deviceCode) {
        DeviceResponse response = deviceService.getDeviceByCode(deviceCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeviceResponse>>> getAllDevices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<DeviceResponse> devices;
        if (status != null && !status.isEmpty()) {
            devices = deviceService.getDevicesByStatus(status, page, size);
        } else if (type != null && !type.isEmpty()) {
            devices = deviceService.getDevicesByType(type, page, size);
        } else {
            devices = deviceService.getAllDevices(page, size);
        }
        return ResponseEntity.ok(ApiResponse.ok(devices));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/code/{deviceCode}")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceByCode(@PathVariable String deviceCode) {
        deviceService.deleteDeviceByCode(deviceCode);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceStatus(
            @PathVariable Long id, @RequestParam String status) {
        DeviceResponse response = deviceService.updateDeviceStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{deviceCode}/heartbeat")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateHeartbeat(@PathVariable String deviceCode) {
        DeviceResponse response = deviceService.updateHeartbeat(deviceCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Device Service is running");
    }
}
