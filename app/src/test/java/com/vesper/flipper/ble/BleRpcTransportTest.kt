package com.vesper.flipper.ble

import com.flipperdevices.protobuf.Flipper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class BleRpcTransportTest {
    @Test fun `BLE diagnostics send protobuf ping without text CLI or reset`() = runBlocking {
        val service = mock<FlipperBleService>()
        whenever(service.usesBleCommandTransport()).thenReturn(true)
        whenever(service.isCommandTransportConnected()).thenReturn(true)
        whenever(service.awaitCommandTransportReady(any())).thenReturn(true)
        whenever(service.sendData(any(), any(), any())).thenReturn(false)
        val protocol = FlipperProtocol()
        protocol.setBleService(service)
        val result = protocol.probeCliAvailability(force = true)
        assertFalse(result.supportsCli)
        val packets = argumentCaptor<ByteArray>()
        verify(service, atLeastOnce()).sendData(packets.capture(), any(), any())
        assertTrue(packets.allValues.isNotEmpty())
        for (packet in packets.allValues) {
            val message = Flipper.Main.parseDelimitedFrom(packet.inputStream())
            assertTrue(message.hasSystemPingRequest())
        }
        verify(service, never()).restartSerialRpc()
        Unit
    }
}
