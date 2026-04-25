package areahint.teleport;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.ClientNetworking;
import areahint.network.Packets;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.util.List;

public class TeleportClientManager {
    private enum State {
        IDLE,
        INPUT_FORMAT
    }

    private static TeleportClientManager instance;
    private static boolean sendListenerRegistered;

    private State state = State.IDLE;

    private TeleportClientManager() {
    }

    public static TeleportClientManager getInstance() {
        if (instance == null) {
            instance = new TeleportClientManager();
        }
        return instance;
    }

    public static void init() {
        if (sendListenerRegistered) {
            return;
        }
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> !getInstance().handleChatInput(message));
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> !getInstance().handleChatInput(command));
        sendListenerRegistered = true;
    }

    public void handleClientCommand(String action) {
        if (action.equals("tcp_start")) {
            showAreaList("tcp");
        } else if (action.equals("udp_start")) {
            showAreaList("udp");
        } else if (action.startsWith("tcp_select:")) {
            requestTeleport("tcp", action.substring("tcp_select:".length()));
        } else if (action.startsWith("udp_select:")) {
            requestTeleport("udp", action.substring("udp_select:".length()));
        } else if (action.equals("settp_start")) {
            startFormatInput();
        } else if (action.startsWith("settp_set:")) {
            setTeleportFormat(action.substring("settp_set:".length()));
        }
    }

    public void handleServerResponse(boolean success, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        client.player.sendMessage(Text.literal((success ? "§a" : "§c") + message), false);
    }

    private void showAreaList(String mode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        List<AreaData> areas = loadCurrentDimensionAreas(client);
        if (areas.isEmpty()) {
            client.player.sendMessage(Text.literal("§c当前维度没有可传送域名"), false);
            return;
        }

        client.player.sendMessage(Text.literal("§6请选择要" + mode.toUpperCase() + "传送的域名："), false);
        for (int i = 0; i < areas.size(); i++) {
            AreaData area = areas.get(i);
            MutableText line = Text.literal(String.format("§a%d. §f%s", i + 1, area.getName()))
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint " + mode + " " + area.getName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("传送到 " + area.getName())))
                            .withColor(Formatting.AQUA));
            client.player.sendMessage(line, false);
        }
    }

    private List<AreaData> loadCurrentDimensionAreas(MinecraftClient client) {
        String dimension = Packets.convertDimensionPathToType(client.world.getRegistryKey().getValue().getPath());
        String fileName = Packets.getFileNameForDimension(dimension);
        if (fileName == null) {
            return List.of();
        }
        Path areaFile = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
        return FileManager.readAreaData(areaFile).stream()
                .filter(AreaData::isValid)
                .toList();
    }

    private void requestTeleport(String mode, String areaName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        if (areaName == null || areaName.trim().isEmpty()) {
            client.player.sendMessage(Text.literal("§c域名不能为空"), false);
            return;
        }
        String teleportFormat = ClientConfig.getTeleportFormat();
        if (!isValidTeleportFormat(teleportFormat)) {
            client.player.sendMessage(Text.literal("§c传送格式无效，请使用 /areahint settp 重新设置"), false);
            return;
        }
        client.player.sendMessage(Text.literal("§7正在请求传送到域名: " + areaName), false);
        ClientNetworking.sendTeleportRequest(mode, stripQuotes(areaName), teleportFormat);
    }

    private void startFormatInput() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        if (state != State.IDLE) {
            client.player.sendMessage(Text.literal("§c请先结束当前传送设置流程"), false);
            return;
        }
        state = State.INPUT_FORMAT;
        client.player.sendMessage(Text.literal("§6请输入传送命令头（当前: " + ClientConfig.getTeleportFormat() + "），输入 cancel 取消"), false);
    }

    private boolean handleChatInput(String input) {
        if (state != State.INPUT_FORMAT) {
            return false;
        }
        if (input == null) {
            return true;
        }
        String value = input.trim();
        if (value.equalsIgnoreCase("cancel") || value.equals("取消")) {
            state = State.IDLE;
            sendLocalMessage("§e已取消传送格式设置");
            return true;
        }
        if (setTeleportFormat(value)) {
            state = State.IDLE;
        }
        return true;
    }

    private boolean setTeleportFormat(String format) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        String value = stripQuotes(format);
        if (!isValidTeleportFormat(value)) {
            client.player.sendMessage(Text.literal("§c传送格式无效，只能作为命令头使用，例如 tp 或 minecraft:tp"), false);
            return false;
        }
        ClientConfig.setTeleportFormat(value);
        client.player.sendMessage(Text.literal("§a传送格式已设置为: " + ClientConfig.getTeleportFormat()), false);
        return true;
    }

    private void sendLocalMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    private boolean isValidTeleportFormat(String teleportFormat) {
        return areahint.data.ConfigData.isValidTeleportFormat(teleportFormat);
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
