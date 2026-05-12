package org.all.device.controller;

import jakarta.validation.Valid;
import org.all.device.dto.DeviceRequest;
import org.all.device.dto.DeviceResponse;
import org.all.device.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        DeviceResponse response = deviceService.getDeviceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{deviceCode}")
    public ResponseEntity<DeviceResponse> getDeviceByCode(@PathVariable String deviceCode) {
        DeviceResponse response = deviceService.getDeviceByCode(deviceCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        List<DeviceResponse> devices;
        if (status != null && !status.isEmpty()) {
            devices = deviceService.getDevicesByStatus(status);
        } else if (type != null && !type.isEmpty()) {
            devices = deviceService.getDevicesByType(type);
        } else {
            devices = deviceService.getAllDevices();
        }
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/code/{deviceCode}")
    public ResponseEntity<Void> deleteDeviceByCode(@PathVariable String deviceCode) {
        deviceService.deleteDeviceByCode(deviceCode);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeviceResponse> updateDeviceStatus(@PathVariable Long id, @RequestParam String status) {
        DeviceResponse response = deviceService.updateDeviceStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deviceCode}/heartbeat")
    public ResponseEntity<DeviceResponse> updateHeartbeat(@PathVariable String deviceCode) {
        DeviceResponse response = deviceService.updateHeartbeat(deviceCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Device Service is running");
    }
}