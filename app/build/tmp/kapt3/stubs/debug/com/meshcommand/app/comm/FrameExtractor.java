package com.meshcommand.app.comm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\u0010\u0005\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eBi\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00126\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\f\u0012\u0004\u0012\u00020\u00050\u0007\u0012\u0016\b\u0002\u0010\r\u001a\u0010\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0013\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u0015J\u001e\u0010\u0016\u001a\u00020\u00172\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00120\u00192\u0006\u0010\u001a\u001a\u00020\u0015H\u0002J\b\u0010\u001b\u001a\u00020\u0005H\u0002J\b\u0010\u001c\u001a\u00020\u001dH\u0002R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\r\u001a\u0010\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R>\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\f\u0012\u0004\u0012\u00020\u00050\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/meshcommand/app/comm/FrameExtractor;", "", "onPacketReceived", "Lkotlin/Function1;", "Lcom/meshcommand/app/comm/SoldierPacket;", "", "onGatewayGpsReceived", "Lkotlin/Function2;", "", "Lkotlin/ParameterName;", "name", "lat", "lon", "onAckReceived", "Lcom/meshcommand/app/comm/AckPacket;", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;)V", "buffer", "", "", "append", "bytes", "", "indexOfSubList", "", "list", "", "subList", "processBuffer", "processGatewayGps", "", "Companion", "app_debug"})
public final class FrameExtractor {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.meshcommand.app.comm.SoldierPacket, kotlin.Unit> onPacketReceived = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<java.lang.Double, java.lang.Double, kotlin.Unit> onGatewayGpsReceived = null;
    @org.jetbrains.annotations.Nullable()
    private final kotlin.jvm.functions.Function1<com.meshcommand.app.comm.AckPacket, kotlin.Unit> onAckReceived = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.Byte> buffer = null;
    public static final byte HEADER_BYTE = (byte)-86;
    public static final byte FOOTER_BYTE = (byte)85;
    public static final int PACKET_SIZE_WITH_FRAMING = 42;
    public static final byte ACK_MAGIC1 = (byte)90;
    public static final byte ACK_MAGIC2 = (byte)-90;
    public static final int ACK_SIZE = 8;
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.comm.FrameExtractor.Companion Companion = null;
    
    public FrameExtractor(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.meshcommand.app.comm.SoldierPacket, kotlin.Unit> onPacketReceived, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Double, ? super java.lang.Double, kotlin.Unit> onGatewayGpsReceived, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super com.meshcommand.app.comm.AckPacket, kotlin.Unit> onAckReceived) {
        super();
    }
    
    public final void append(@org.jetbrains.annotations.NotNull()
    byte[] bytes) {
    }
    
    private final void processBuffer() {
    }
    
    private final boolean processGatewayGps() {
        return false;
    }
    
    private final int indexOfSubList(java.util.List<java.lang.Byte> list, byte[] subList) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/meshcommand/app/comm/FrameExtractor$Companion;", "", "()V", "ACK_MAGIC1", "", "ACK_MAGIC2", "ACK_SIZE", "", "FOOTER_BYTE", "HEADER_BYTE", "PACKET_SIZE_WITH_FRAMING", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}