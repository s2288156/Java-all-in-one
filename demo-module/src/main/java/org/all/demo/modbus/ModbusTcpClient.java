package org.all.demo.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ModbusTcpClient implements ModbusClient {

    private ModbusTCPMaster master;
    private String host;
    private int port;
    private int timeout;
    private int retries;

    public ModbusTcpClient(String host, int port) {
        this(host, port, 5000, 3);
    }

    public ModbusTcpClient(String host, int port, int timeout, int retries) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.retries = retries;
    }

    @Override
    public void connect() throws IOException {
        if (master != null && master.isConnected()) {
            log.warn("Already connected to {}:{}", host, port);
            return;
        }

        try {
            master = new ModbusTCPMaster(host, port);
            master.setTimeout(timeout);
            master.setRetries(retries);
            master.connect();
            
            log.info("Connected to Modbus TCP server {}:{}", host, port);
        } catch (Exception e) {
            throw new IOException("Failed to connect to " + host + ":" + port, e);
        }
    }

    @Override
    public boolean isConnected() {
        return master != null && master.isConnected();
    }

    @Override
    public BitVector readCoils(int slaveId, int offset, int count) throws ModbusException {
        ensureConnected();
        return master.readCoils(slaveId, offset, count);
    }

    @Override
    public void writeCoil(int slaveId, int offset, boolean value) throws ModbusException {
        ensureConnected();
        master.writeCoil(slaveId, offset, value);
        log.debug("Wrote coil at offset {} with value {}", offset, value);
    }

    @Override
    public BitVector readDiscreteInputs(int slaveId, int offset, int count) throws ModbusException {
        ensureConnected();
        return master.readInputDiscretes(slaveId, offset, count);
    }

    @Override
    public Register[] readHoldingRegisters(int slaveId, int offset, int count) throws ModbusException {
        ensureConnected();
        return master.readMultipleRegisters(slaveId, offset, count);
    }

    @Override
    public void writeHoldingRegister(int slaveId, int offset, int value) throws ModbusException {
        ensureConnected();
        master.writeSingleRegister(slaveId, offset, new SimpleRegister(value));
        log.debug("Wrote holding register at offset {} with value {}", offset, value);
    }

    @Override
    public void writeMultipleHoldingRegisters(int slaveId, int offset, int[] values) throws ModbusException {
        ensureConnected();
        Register[] registers = new Register[values.length];
        for (int i = 0; i < values.length; i++) {
            registers[i] = new SimpleRegister(values[i]);
        }
        master.writeMultipleRegisters(slaveId, offset, registers);
        log.debug("Wrote {} holding registers starting at offset {}", values.length, offset);
    }

    @Override
    public InputRegister[] readInputRegisters(int slaveId, int offset, int count) throws ModbusException {
        ensureConnected();
        return master.readInputRegisters(slaveId, offset, count);
    }

    @Override
    public int readInputRegisterAsInt(int slaveId, int offset) throws ModbusException {
        ensureConnected();
        InputRegister[] registers = master.readInputRegisters(slaveId, offset, 1);
        if (registers != null && registers.length > 0) {
            return registers[0].getValue();
        }
        return 0;
    }

    @Override
    public int readHoldingRegisterAsInt(int slaveId, int offset) throws ModbusException {
        ensureConnected();
        Register[] registers = master.readMultipleRegisters(slaveId, offset, 1);
        if (registers != null && registers.length > 0) {
            return registers[0].getValue();
        }
        return 0;
    }

    private void ensureConnected() {
        if (master == null || !master.isConnected()) {
            throw new IllegalStateException("Not connected to Modbus TCP server");
        }
    }

    @Override
    public void close() {
        if (master != null) {
            if (master.isConnected()) {
                try {
                    master.disconnect();
                    log.info("Disconnected from Modbus TCP server {}:{}", host, port);
                } catch (Exception e) {
                    log.warn("Error disconnecting from Modbus TCP server: {}", e.getMessage());
                }
            }
            master = null;
        }
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }
}
