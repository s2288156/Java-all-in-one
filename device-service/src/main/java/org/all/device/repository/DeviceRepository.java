package org.all.device.repository;

import org.all.device.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceCode(String deviceCode);

    Page<Device> findByStatus(String status, Pageable pageable);

    Page<Device> findByDeviceType(String deviceType, Pageable pageable);

    boolean existsByDeviceCode(String deviceCode);

    void deleteByDeviceCode(String deviceCode);
}
