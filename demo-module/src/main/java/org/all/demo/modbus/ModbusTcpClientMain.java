package org.all.demo.modbus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusTcpClientMain {

    public static void main(String[] args) {
        log.info("Starting Modbus TCP Client Demo");

        String host = "127.0.0.1";
        int port = 512;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        log.info("Connecting to Modbus TCP server at {}:{}", host, port);

        try (ModbusClient client = new ModbusTcpClient(host, port, 5000, 3)) {
            client.connect();
            log.info("Connected successfully");

            int slaveId = 1;
            
            try {
                int value0 = client.readHoldingRegisterAsInt(slaveId, 0);
                int value1 = client.readHoldingRegisterAsInt(slaveId, 1);
                log.info("Before write - Holding register 0: {}, register 1: {}", value0, value1);
            } catch (Exception e) {
                log.warn("Failed to read registers: {}", e.getMessage());
            }

            try {
                client.writeHoldingRegister(slaveId, 0, 2);
                client.writeHoldingRegister(slaveId, 1, 2);
                log.info("Successfully wrote value 2 to holding registers 0 and 1");
            } catch (Exception e) {
                log.error("Failed to write registers: {}", e.getMessage());
            }

            try {
                int value0 = client.readHoldingRegisterAsInt(slaveId, 0);
                int value1 = client.readHoldingRegisterAsInt(slaveId, 1);
                log.info("After write - Holding register 0: {}, register 1: {}", value0, value1);
            } catch (Exception e) {
                log.warn("Failed to read registers after write: {}", e.getMessage());
            }

            log.info("Demo completed");

        } catch (Exception e) {
            log.error("Error during Modbus client operation", e);
        }
    }
}
