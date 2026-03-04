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
package org.thingsboard.server.service.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.data.rpc.ToDeviceRpcRequestBody;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequest;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.queue.discovery.TbServiceInfoProvider;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultTbCoreDeviceRpcServiceTest {

    private DeviceService deviceService;
    private TbClusterService clusterService;
    private TbServiceInfoProvider serviceInfoProvider;
    private ActorSystemContext actorContext;
    private DefaultTbCoreDeviceRpcService rpcService;

    private final TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
    private final DeviceId deviceId = new DeviceId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        clusterService = mock(TbClusterService.class);
        serviceInfoProvider = mock(TbServiceInfoProvider.class);
        actorContext = mock(ActorSystemContext.class);

        when(serviceInfoProvider.getServiceId()).thenReturn("test-service-id");

        rpcService = new DefaultTbCoreDeviceRpcService(
                deviceService, clusterService, serviceInfoProvider, actorContext);
        rpcService.setTbRuleEngineRpcService(Optional.empty());
        rpcService.initExecutor();
    }

    @Test
    void whenRpcSent_thenDeviceMetadataIsEnrichedIntoMessage() {
        Device device = new Device();
        device.setName("temperature-sensor-01");
        device.setType("SENSOR");
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(device);

        ToDeviceRpcRequest request = new ToDeviceRpcRequest(
                UUID.randomUUID(), tenantId, deviceId, true,
                System.currentTimeMillis() + 60000,
                new ToDeviceRpcRequestBody("getTemp", "{\"unit\":\"C\"}"),
                false, null, null);

        SecurityUser user = new SecurityUser();
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        user.setCustomerId(customerId);

        rpcService.processRestApiRpcRequest(request, response -> {}, user);

        ArgumentCaptor<TbMsg> msgCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(clusterService).pushMsgToRuleEngine(eq(tenantId), eq(deviceId), msgCaptor.capture(), isNull());

        TbMsg captured = msgCaptor.getValue();

        assertEquals(TbMsgType.RPC_CALL_FROM_SERVER_TO_DEVICE.name(), captured.getType());

        assertEquals("temperature-sensor-01", captured.getMetaData().getValue("deviceName"));
        assertEquals("SENSOR", captured.getMetaData().getValue("deviceType"));
        assertEquals("test-service-id", captured.getMetaData().getValue("originServiceId"));
        assertEquals(request.getId().toString(), captured.getMetaData().getValue("requestUUID"));
        assertEquals("true", captured.getMetaData().getValue("oneway"));

        JsonNode body = JacksonUtil.toJsonNode(captured.getData());
        assertEquals("getTemp", body.get("method").asText());
        assertEquals("{\"unit\":\"C\"}", body.get("params").asText());
    }

    @Test
    void whenDeviceNotFound_thenMessageIsSentWithoutDeviceMetadata() {
        when(deviceService.findDeviceById(tenantId, deviceId)).thenReturn(null);

        ToDeviceRpcRequest request = new ToDeviceRpcRequest(
                UUID.randomUUID(), tenantId, deviceId, false,
                System.currentTimeMillis() + 60000,
                new ToDeviceRpcRequestBody("reboot", "{}"),
                true, 3, null);

        rpcService.processRestApiRpcRequest(request, response -> {}, null);

        ArgumentCaptor<TbMsg> msgCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(clusterService).pushMsgToRuleEngine(eq(tenantId), eq(deviceId), msgCaptor.capture(), isNull());

        TbMsg captured = msgCaptor.getValue();

        assertTrue(captured.getMetaData().getValue("deviceName") == null
                || captured.getMetaData().getValue("deviceName").isEmpty());

        assertEquals("false", captured.getMetaData().getValue("oneway"));
        assertEquals("true", captured.getMetaData().getValue("persistent"));
        assertEquals("3", captured.getMetaData().getValue("retries"));

        verify(deviceService).findDeviceById(tenantId, deviceId);
    }
}
