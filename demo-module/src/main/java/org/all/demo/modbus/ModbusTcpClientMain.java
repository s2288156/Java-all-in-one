package org.all.demo.modbus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusTcpClientMain {

    private static final int CONNECTION_RETRY_COUNT = 3;
    private static final long CONNECTION_RETRY_DELAY_MS = 2000;
    private static final int OPERATION_RETRY_COUNT = 2;
    private static final long OPERATION_RETRY_DELAY_MS = 1000;

    public static void main(String[] args) {
        log.info("Starting Modbus TCP Client Demo");

        String host = "127.0.0.1";
        int port = 512;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        log.info("Connecting to Modbus TCP server at {}:{}", host, port);

        ModbusClient client = new ModbusTcpClient(host, port, 5000, 3);

        try {
            if (!connectWithRetry(client, host, port)) {
                log.error("Failed to connect to Modbus TCP server after {} retries", CONNECTION_RETRY_COUNT);
                return;
            }

            int slaveId = 1;

            int value0 = readHoldingRegisterWithRetry(client, slaveId, 0);
            int value1 = readHoldingRegisterWithRetry(client, slaveId, 1);
            log.info("Before write - Holding register 0: {}, register 1: {}", value0, value1);

            if (writeHoldingRegisterWithRetry(client, slaveId, 0, 2) &&
                writeHoldingRegisterWithRetry(client, slaveId, 1, 2)) {
                log.info("Successfully wrote value 2 to holding registers 0 and 1");
            } else {
                log.error("Failed to write to holding registers");
            }

            value0 = readHoldingRegisterWithRetry(client, slaveId, 0);
            value1 = readHoldingRegisterWithRetry(client, slaveId, 1);
            log.info("After write - Holding register 0: {}, register 1: {}", value0, value1);

            log.info("Demo completed");

        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing client: {}", e.getMessage());
            }
        }
    }

    private static boolean connectWithRetry(ModbusClient client, String host, int port) {
        int attempts = 0;
        while (attempts < CONNECTION_RETRY_COUNT) {
            attempts++;
            try {
                log.info("Connection attempt {}/{}", attempts, CONNECTION_RETRY_COUNT);
                client.connect();
                log.info("Connected successfully to {}:{}", host, port);
                return true;
            } catch (Exception e) {
                log.warn("Connection attempt {} failed: {}", attempts, e.getMessage());
                if (attempts < CONNECTION_RETRY_COUNT) {
                    try {
                        log.info("Waiting {}ms before next connection attempt...", CONNECTION_RETRY_DELAY_MS);
                        Thread.sleep(CONNECTION_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Connection retry interrupted");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private static int readHoldingRegisterWithRetry(ModbusClient client, int slaveId, int offset) {
        int attempts = 0;
        while (attempts <= OPERATION_RETRY_COUNT) {
            attempts++;
            try {
                return client.readHoldingRegisterAsInt(slaveId, offset);
            } catch (Exception e) {
                log.warn("Read holding register attempt {} failed: {}", attempts, e.getMessage());
                if (attempts <= OPERATION_RETRY_COUNT) {
                    if (reconnect(client)) {
                        try {
                            log.info("Waiting {}ms before retrying operation...", OPERATION_RETRY_DELAY_MS);
                            Thread.sleep(OPERATION_RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.warn("Operation retry interrupted");
                            return 0;
                        }
                    } else {
                        log.error("Failed to reconnect after operation failure");
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    private static boolean writeHoldingRegisterWithRetry(ModbusClient client, int slaveId, int offset, int value) {
        int attempts = 0;
        while (attempts <= OPERATION_RETRY_COUNT) {
            attempts++;
            try {
                client.writeHoldingRegister(slaveId, offset, value);
                return true;
            } catch (Exception e) {
                log.warn("Write holding register attempt {} failed: {}", attempts, e.getMessage());
                if (attempts <= OPERATION_RETRY_COUNT) {
                    if (reconnect(client)) {
                        try {
                            log.info("Waiting {}ms before retrying operation...", OPERATION_RETRY_DELAY_MS);
                            Thread.sleep(OPERATION_RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.warn("Operation retry interrupted");
                            return false;
                        }
                    } else {
                        log.error("Failed to reconnect after operation failure");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private static boolean reconnect(ModbusClient client) {
        log.info("Attempting to reconnect...");
        try {
            client.close();
        } catch (Exception e) {
            log.warn("Error closing broken connection: {}", e.getMessage());
        }

        try {
            client.connect();
            log.info("Reconnected successfully");
            return true;
        } catch (Exception e) {
            log.warn("Failed to reconnect: {}", e.getMessage());
            return false;
        }
    }
}
