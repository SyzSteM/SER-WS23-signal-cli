package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.DeviceNotFoundException;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.service.DeviceService;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.Device;
import org.asamk.signal.manager.api.DeviceLinkUrl;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.RateLimitException;
import org.asamk.signal.manager.api.UserStatus;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private Manager mockManager;

    @Mock
    private DBusConnection mockDBusConnection;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceServiceImpl(mockManager, mockDBusConnection, "objectPath");
    }

    @Test
    void testIsRegisteredDeprecated() {
        assertThat(deviceService.isRegistered()).isTrue();
    }

    @Test
    void testIsRegisteredWithSingleNumber() throws Exception {
        String testNumber = "1234567890";
        UUID uuid = UUID.randomUUID();
        UserStatus mockStatus = new UserStatus(testNumber, uuid, true);

        given(mockManager.getUserStatus(anySet())).willReturn(Map.of(testNumber, mockStatus));

        boolean result = deviceService.isRegistered(testNumber);

        assertThat(result).isTrue();
    }

    @Test
    void testIsRegisteredWithMultipleNumbers() throws Exception {
        String number1 = "1234567890";
        String number2 = "0987654321";
        UUID uuid1 = UUID.randomUUID();
        UserStatus status1 = new UserStatus(number1, uuid1, true);
        UserStatus status2 = new UserStatus(number2, null, false);

        given(mockManager.getUserStatus(anySet())).willReturn(Map.of(number1, status1, number2, status2));

        List<Boolean> results = deviceService.isRegistered(List.of(number1, number2));

        assertThat(results).containsExactly(true, false);
    }

    @Test
    void testIsRegisteredThrowsFailureExceptionOnIOException() throws Exception {
        given(mockManager.getUserStatus(anySet())).willThrow(IOException.class);

        assertThatThrownBy(() -> deviceService.isRegistered("1234567890")).isInstanceOf(FailureException.class);
    }

    @Test
    void testIsRegisteredThrowsFailureExceptionOnRateLimitException() throws Exception {
        given(mockManager.getUserStatus(anySet())).willThrow(RateLimitException.class);

        assertThatThrownBy(() -> deviceService.isRegistered("1234567890")).isInstanceOf(FailureException.class);
    }

    @Test
    void testAddDeviceSuccessfully() throws Exception {
        String validUri = "sgnl://linkdevice?uuid=-7D2GL1tW1kljhz18NUiUg&pub_key=BY0uv3boYzRt1w8LnCpcG9ghsqSEsOY%2FO7cSHDOD57VA";
        doNothing().when(mockManager).addDeviceLink(any(DeviceLinkUrl.class));

        deviceService.addDevice(validUri);

        verify(mockManager, times(1)).addDeviceLink(any(DeviceLinkUrl.class));
    }

    @Test
    void testAddDeviceThrowsFailureExceptionOnIOException() throws Exception {
        String validUri = "sgnl://linkdevice?uuid=-7D2GL1tW1kljhz18NUiUg&pub_key=BY0uv3boYzRt1w8LnCpcG9ghsqSEsOY%2FO7cSHDOD57VA";
        doThrow(IOException.class).when(mockManager).addDeviceLink(any(DeviceLinkUrl.class));

        assertThatThrownBy(() -> deviceService.addDevice(validUri)).isInstanceOf(FailureException.class)
                .hasMessageContaining("Add device link failed");
    }

    @Test
    void testAddDeviceThrowsFailureExceptionOnNotPrimaryDeviceException() throws Exception {
        String validUri = "sgnl://linkdevice?uuid=-7D2GL1tW1kljhz18NUiUg&pub_key=BY0uv3boYzRt1w8LnCpcG9ghsqSEsOY%2FO7cSHDOD57VA";
        doThrow(NotPrimaryDeviceException.class).when(mockManager).addDeviceLink(any(DeviceLinkUrl.class));

        assertThatThrownBy(() -> deviceService.addDevice(validUri)).isInstanceOf(FailureException.class)
                .hasMessageContaining("This command doesn't work on linked devices.");
    }

    @Test
    void testAddDeviceThrowsFailureExceptionOnURISyntaxException() {
        String validUri = "sgnl://linkdevice?uuid=&pub_key=BY0uv3boYzRt1w8LnCpcG9ghsqSEsOY%2FO7cSHDOD57VA";

        assertThatThrownBy(() -> deviceService.addDevice(validUri)).isInstanceOf(FailureException.class)
                .hasMessageContaining("Add device link failed.");
    }

    @Test
    void testUnregister() throws IOException {
        deviceService.unregister();

        then(mockManager).should().unregister();
    }

    @Test
    void testUnregisterException() throws IOException {
        doThrow(IOException.class).when(mockManager).unregister();

        assertThatThrownBy(() -> deviceService.unregister()).isInstanceOf(FailureException.class);
    }

    @Test
    void testUpdateDevices() throws IOException {
        given(mockManager.getLinkedDevices()).willReturn(List.of(new Device(1, "test", 1, 1, true)));

        deviceService.updateDevices();

        then(mockManager).should().getLinkedDevices();
    }

    @Test
    void testUpdateDevicesFailureExceptionOnIOException() throws IOException {
        doThrow(IOException.class).when(mockManager).getLinkedDevices();

        assertThatThrownBy(() -> deviceService.updateDevices()).isInstanceOf(FailureException.class)
                .hasMessageContaining("Failed to get linked devices");

    }

    @Test
    void testGetDeviceValidId() throws IOException {
        given(mockManager.getLinkedDevices()).willReturn(List.of(new Device(1, "test", 1, 1, true)));

        final DBusPath device = deviceService.getDevice(1);

        assertThat(device.getPath()).isEqualTo("objectPath/Devices/1");
    }

    @Test
    void testGetDeviceInvalidId() throws IOException {
        given(mockManager.getLinkedDevices()).willReturn(List.of(new Device(1, "test", 1, 1, true)));

        assertThatThrownBy(() -> deviceService.getDevice(2)).isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("DbusPropertyDevice not found");
    }

    @Test
    void testGetThisDevice() throws IOException {
        given(mockManager.getLinkedDevices()).willReturn(List.of(new Device(1, "test", 1, 1, true)));

        final DBusPath device = deviceService.getThisDevice();

        assertThat(device.getPath()).isEqualTo("objectPath/Devices/1");
    }

    @Test
    void testGetThisDeviceNotExists() throws IOException {
        given(mockManager.getLinkedDevices()).willReturn(List.of(new Device(1, "test", 1, 1, false)));

        assertThat(deviceService.getThisDevice()).isNull();
    }

    @Test
    void testListDevices() throws IOException {
        given(mockManager.getLinkedDevices()).willReturn(List.of(new Device(1, "test", 1, 1, true)));

        var devices = deviceService.listDevices();

        assertThat(devices).hasSize(1);
    }

}
