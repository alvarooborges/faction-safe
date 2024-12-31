package net.hyze.safe.echo.packets;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacketResponse;
import dev.utils.echo.packet.IEchoRespondable;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
public class SelectSafeEchoPacket extends ServerEchoPacket implements IEchoRespondable<SelectSafeEchoPacket.SelectSafeResponse> {

    @NonNull
    private Integer userId;

    @NonNull
    private int safeId;

    @Setter
    private SelectSafeResponse response = null;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeInt(this.safeId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.userId = buffer.readInt();
        this.safeId = buffer.readInt();
    }

    @NoArgsConstructor
    public static class SelectSafeResponse extends AbstractEchoPacketResponse {

        @Override
        public void write(EchoByteBufferOutput buffer) {

        }

        @Override
        public void read(EchoByteBufferInput buffer) {

        }
    }
}
