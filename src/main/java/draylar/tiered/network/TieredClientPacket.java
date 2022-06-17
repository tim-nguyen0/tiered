package draylar.tiered.network;

import draylar.tiered.access.AnvilScreenHandlerAccess;
import draylar.tiered.access.MouseAccessor;
import draylar.tiered.reforge.ReforgeScreenHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class TieredClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(TieredServerPacket.SYNC_POS_SC, (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            Boolean reforgeHandler = buf.readBoolean();
            client.execute(() -> {
                if (reforgeHandler)
                    ((AnvilScreenHandlerAccess) client.player.currentScreenHandler).setPos(pos);
                else
                    ((ReforgeScreenHandler) client.player.currentScreenHandler).pos = pos;
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(TieredServerPacket.SET_MOUSE_POSITION, (client, handler, buf, sender) -> {
            int mouseX = buf.readInt();
            int mouseY = buf.readInt();
            client.execute(() -> {
                ((MouseAccessor) client.mouse).setMousePosition(mouseX, mouseY);
            });
        });
    }

    public static void writeC2SScreenPacket(BlockPos pos, int mouseX, int mouseY, boolean reforgingScreen) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        buf.writeBlockPos(pos);
        buf.writeBoolean(reforgingScreen);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(TieredServerPacket.SET_SCREEN, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SSyncPosPacket(Boolean reforgeHandler) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(reforgeHandler);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(TieredServerPacket.SYNC_POS_CS, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

}
