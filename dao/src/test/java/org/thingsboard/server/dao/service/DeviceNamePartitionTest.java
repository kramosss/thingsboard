/**
 * Copyright © 2016-2026 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thingsboard.server.exception.DataValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Partition testing for DataValidator.validateString() method.
 */
public class DeviceNamePartitionTest {
    
    private DataValidator<?> dataValidator;
    
    @BeforeEach
    public void setUp() {
        dataValidator = new DataValidator<>() {};
    }
    
    @Test
    public void testNullDeviceName() {
        String deviceName = null;
        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals("Device name should be specified!", exception.getMessage());
    }
    
    @Test
    public void testEmptyDeviceName() {
        String deviceName = "";
        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals("Device name should be specified!", exception.getMessage());
    }
    
    @Test
    public void testWhitespaceOnlyDeviceName() {
        String deviceName = "   ";
        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals("Device name should be specified!", exception.getMessage());
    }
    
    @Test
    public void testDeviceNameWithNullByteInMiddle() {
        String deviceName = "Device\u0000Name";
        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals("Device name should not contain 0x00 symbol!", exception.getMessage());
    }
    
    @Test
    public void testDeviceNameWithNullByteAtStart() {
        String deviceName = "\u0000DeviceName";
        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals("Device name should not contain 0x00 symbol!", exception.getMessage());
    }
    
    @Test
    public void testDeviceNameWithNullByteAtEnd() {
        String deviceName = "DeviceName\u0000";
        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals("Device name should not contain 0x00 symbol!", exception.getMessage());
    }
    
    @Test
    public void testValidSimpleAlphanumericName() {
        String deviceName = "TestDevice123";
        assertDoesNotThrow(
            () -> dataValidator.validateString("Device name", deviceName)
        );
    }
    
    @Test
    public void testValidDeviceNameWithSpecialChars() {
        String deviceName = "Temperature Sensor_01-Main";
        assertDoesNotThrow(
            () -> dataValidator.validateString("Device name", deviceName)
        );
    }
    
    @Test
    public void testValidDeviceNameWithUnicodeChars() {
        String deviceName = "传感器センサー";
        assertDoesNotThrow(
            () -> dataValidator.validateString("Device name", deviceName)
        );
    }
    
    @Test
    public void testValidSingleCharacterName() {
        String deviceName = "A";
        assertDoesNotThrow(
            () -> dataValidator.validateString("Device name", deviceName)
        );
        assertEquals(1, deviceName.length());
    }
}