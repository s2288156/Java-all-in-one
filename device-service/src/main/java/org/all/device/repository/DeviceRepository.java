package org.all.device.repository;

import org.all.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceCode(String deviceCode);

    List<Device> findByStatus(String status);

    List<Device> findByDeviceType(String deviceType);

    List<Device> findByLocation(String location);

    boolean existsByDeviceCode(String deviceCode);

    void deleteByDeviceCode(String deviceCode);
}