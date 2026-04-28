package com.meshcommand.app.comm;

/**
 * Handles slicing a firmware binary and sending it via LoRa chunks.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ(\u0010\n\u001a\u00020\u000b*\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\tH\u0082H\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/meshcommand/app/comm/OtaManager;", "", "()V", "startOtaUpload", "Lkotlinx/coroutines/flow/Flow;", "Lcom/meshcommand/app/comm/CommandPacket;", "targetNodeId", "", "firmware", "", "yieldPacket", "", "Lkotlinx/coroutines/flow/FlowCollector;", "payload", "(Lkotlinx/coroutines/flow/FlowCollector;I[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class OtaManager {
    
    public OtaManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.meshcommand.app.comm.CommandPacket> startOtaUpload(int targetNodeId, @org.jetbrains.annotations.NotNull()
    byte[] firmware) {
        return null;
    }
    
    private final java.lang.Object yieldPacket(kotlinx.coroutines.flow.FlowCollector<? super com.meshcommand.app.comm.CommandPacket> $this$yieldPacket, int targetNodeId, byte[] payload, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}