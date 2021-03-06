package io.github.alphahelixdev.alpary.reflection.nms.packets;

import io.github.alphahelixdev.alpary.utils.Utils;
import io.github.whoisalphahelix.helix.reflection.SaveConstructor;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class EntityDestroyPacket implements IPacket {

    private static final SaveConstructor PACKET =
            Utils.nms().getDeclaredConstructor(Utils.nms().getNMSClass("PacketPlayOutEntityDestroy"),
                    int[].class);

    private int[] entityIDs;

    @Override
    public Object getPacket(boolean stackTrace) {
        return EntityDestroyPacket.PACKET.newInstance(stackTrace, this.getEntityIDs());
    }
}
