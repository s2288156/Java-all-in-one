package org.all.device.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.all.common.exception.BusinessException;
import org.all.common.model.PageResponse;
import org.all.device.dto.DeviceRequest;
import org.all.device.dto.DeviceResponse;
import org.all.device.entity.Device;
import org.all.device.repository.DeviceRepository;
import org.all.device.service.DeviceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private static final Pattern STATUS_PATTERN = Pattern.compile("^(online|offline|maintenance)$");

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public DeviceResponse createDevice(DeviceRequest request) {
        if (deviceRepository.existsByDeviceCode(request.getDeviceCode())) {
            throw new BusinessException(409, "设备编码已存在: " + request.getDeviceCode());
        }

        Device device = Device.builder()
                .deviceCode(request.getDeviceCode())
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .status(request.getStatus())
                .location(request.getLocation())
                .ipAddress(request.getIpAddress())
                .macAddress(request.getMacAddress())
                .serialNumber(request.getSerialNumber())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .installationDate(request.getInstallationDate())
                .lastHeartbeat(request.getLastHeartbeat())
                .build();

        Device savedDevice = deviceRepository.save(device);
        log.info("设备创建成功: {}", savedDevice.getDeviceCode());
        return toDeviceResponse(savedDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "设备不存在: " + id));
        return toDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceByCode(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new BusinessException(404, "设备不存在: " + deviceCode));
        return toDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeviceResponse> getAllDevices(int page, int size) {
        Page<Device> devicePage = deviceRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return toPageResponse(devicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeviceResponse> getDevicesByStatus(String status, int page, int size) {
        Page<Device> devicePage = deviceRepository.findByStatus(status, PageRequest.of(page, size, Sort.by("id")));
        return toPageResponse(devicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeviceResponse> getDevicesByType(String deviceType, int page, int size) {
        Page<Device> devicePage = deviceRepository.findByDeviceType(deviceType, PageRequest.of(page, size, Sort.by("id")));
        return toPageResponse(devicePage);
    }

    @Override
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "设备不存在: " + id));

        Device existingDevice = deviceRepository.findByDeviceCode(request.getDeviceCode())
                .orElse(null);
        if (existingDevice != null && !existingDevice.getId().equals(id)) {
            throw new BusinessException(409, "设备编码已被使用: " + request.getDeviceCode());
        }

        device.setDeviceCode(request.getDeviceCode());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setStatus(request.getStatus());
        device.setLocation(request.getLocation());
        device.setIpAddress(request.getIpAddress());
        device.setMacAddress(request.getMacAddress());
        device.setSerialNumber(request.getSerialNumber());
        device.setManufacturer(request.getManufacturer());
        device.setModel(request.getModel());
        device.setInstallationDate(request.getInstallationDate());
        device.setLastHeartbeat(request.getLastHeartbeat());

        Device updatedDevice = deviceRepository.save(device);
        log.info("设备更新成功: {}", updatedDevice.getDeviceCode());
        return toDeviceResponse(updatedDevice);
    }

    @Override
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new BusinessException(404, "设备不存在: " + id);
        }
        deviceRepository.deleteById(id);
        log.info("设备删除成功: {}", id);
    }

    @Override
    public void deleteDeviceByCode(String deviceCode) {
        if (!deviceRepository.existsByDeviceCode(deviceCode)) {
            throw new BusinessException(404, "设备不存在: " + deviceCode);
        }
        deviceRepository.deleteByDeviceCode(deviceCode);
        log.info("设备删除成功: {}", deviceCode);
    }

    @Override
    public DeviceResponse updateDeviceStatus(Long id, String status) {
        if (!STATUS_PATTERN.matcher(status).matches()) {
            throw new BusinessException(400, "设备状态只能是 online、offline 或 maintenance");
        }
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "设备不存在: " + id));
        device.setStatus(status);
        Device updatedDevice = deviceRepository.save(device);
        log.info("设备状态更新成功: {} -> {}", updatedDevice.getDeviceCode(), status);
        return toDeviceResponse(updatedDevice);
    }

    @Override
    public DeviceResponse updateHeartbeat(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new BusinessException(404, "设备不存在: " + deviceCode));
        device.setLastHeartbeat(LocalDateTime.now());
        device.setStatus("online");
        Device updatedDevice = deviceRepository.save(device);
        log.info("设备心跳更新成功: {}", deviceCode);
        return toDeviceResponse(updatedDevice);
    }

    private PageResponse<DeviceResponse> toPageResponse(Page<Device> page) {
        return PageResponse.<DeviceResponse>builder()
                .content(page.getContent().stream().map(this::toDeviceResponse).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private DeviceResponse toDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .status(device.getStatus())
                .location(device.getLocation())
                .ipAddress(device.getIpAddress())
                .macAddress(device.getMacAddress())
                .serialNumber(device.getSerialNumber())
                .manufacturer(device.getManufacturer())
                .model(device.getModel())
                .installationDate(device.getInstallationDate())
                .lastHeartbeat(device.getLastHeartbeat())
                .createdTime(device.getCreatedTime())
                .updatedTime(device.getUpdatedTime())
                .build();
    }
}
