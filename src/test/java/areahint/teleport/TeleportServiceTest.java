package areahint.teleport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportServiceTest {
    @Test
    void acceptsSimpleTeleportCommandHead() {
        assertTrue(TeleportService.isValidTeleportFormat("tp"));
        assertTrue(TeleportService.isValidTeleportFormat("minecraft:tp"));
        assertTrue(TeleportService.isValidTeleportFormat("teleport"));
        assertTrue(TeleportService.isValidTeleportFormat("minecraft:teleport"));
    }

    @Test
    void rejectsBlankTemplateLikeOrNonTeleportCommandHead() {
        assertFalse(TeleportService.isValidTeleportFormat(""));
        assertFalse(TeleportService.isValidTeleportFormat("tp {x} {y} {z}"));
        assertFalse(TeleportService.isValidTeleportFormat("../tp"));
        assertFalse(TeleportService.isValidTeleportFormat("time"));
    }
}
