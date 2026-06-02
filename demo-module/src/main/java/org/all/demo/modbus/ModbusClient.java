package org.all.demo.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.Closeable;
import java.io.IOException;

public interface ModbusClient extends Closeable {

    void connect() throws IOException;

    boolean isConnected();

    BitVector readCoils(int slaveId, int offset, int count) throws ModbusException;

    void writeCoil(int slaveId, int offset, boolean value) throws ModbusException;

    BitVector readDiscreteInputs(int slaveId, int offset, int count) throws ModbusException;

    Register[] readHoldingRegisters(int slaveId, int offset, int count) throws ModbusException;

    void writeHoldingRegister(int slaveId, int offset, int value) throws ModbusException;

    void writeMultipleHoldingRegisters(int slaveId, int offset, int[] values) throws ModbusException;

    InputRegister[] readInputRegisters(int slaveId, int offset, int count) throws ModbusException;

    int readInputRegisterAsInt(int slaveId, int offset) throws ModbusException;

    int readHoldingRegisterAsInt(int slaveId, int offset) throws ModbusException;

    String getHost();

    int getPort();
}
