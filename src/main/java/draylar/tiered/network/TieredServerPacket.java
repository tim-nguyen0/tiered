package draylar.tiered.network;

import draylar.tiered.access.AnvilScreenHandlerAccess;
import draylar.tiered.reforge.ReforgeScreenHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TieredServerPacket {

    public static final Identifier SET_SCREEN = new Identifier("tiered", "set_screen");
    public static final Identifier SYNC_POS_SC = new Identifier("tiered", "sync_pos_sc");
    public static final Identifier SYNC_POS_CS = new Identifier("tiered", "sync_pos_cs");
    public static final Identifier REFORGE_READY = new Identifier("tiered", "reforge_ready");
    public static final Identifier REFORGE = new Identifier("tiered", "reforge");
    public static final Identifier SET_MOUSE_POSITION = new Identifier("tiered", "set_mouse_position");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(SET_SCREEN, (server, player, handler, buffer, sender) -> {
            int mouseX = buffer.readInt();
            int mouseY = buffer.readInt();
            BlockPos pos = buffer.readBlockPos();
            Boolean reforgingScreen = buffer.readBoolean();
            if (player != null) {
                if (reforgingScreen)
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                        return new ReforgeScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(playerx.world, pos));
                    }, Text.translatable("container.reforge")));
                else
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                        return new AnvilScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(playerx.world, pos));
                    }, Text.translatable("container.repair")));
                writeS2CMousePositionPacket(player, mouseX, mouseY);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SYNC_POS_CS, (server, player, handler, buffer, sender) -> {
            Boolean reforgeHandler = buffer.readBoolean();
            server.execute(() -> {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                BlockPos pos;
                if (reforgeHandler)
                    pos = ((AnvilScreenHandlerAccess) player.currentScreenHandler).getPos();
                else
                    pos = ((ReforgeScreenHandler) player.currentScreenHandler).getPos();
                buf.writeBlockPos(pos);
                buf.writeBoolean(reforgeHandler);
                CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SYNC_POS_SC, buf);
                handler.sendPacket(packet);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(REFORGE, (server, player, handler, buffer, sender) -> {
            server.execute(() -> {
                if (player.currentScreenHandler instanceof ReforgeScreenHandler)
                    ((ReforgeScreenHandler) player.currentScreenHandler).reforge();
            });
        });
    }

    public static void writeS2CReforgeReadyPacket(ServerPlayerEntity serverPlayerEntity, boolean disableButton) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(disableButton);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(REFORGE_READY, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    private static void writeS2CMousePositionPacket(ServerPlayerEntity serverPlayerEntity, int mouseX, int mouseY) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_MOUSE_POSITION, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
