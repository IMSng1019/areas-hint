package areahint.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigDataTest {
    @Test
    void defaultTeleportFormatIsTp() {
        ConfigData config = new ConfigData();

        assertEquals("tp", config.getTeleportFormat());
    }

    @Test
    void blankTeleportFormatFallsBackToTp() {
        ConfigData config = new ConfigData();

        config.setTeleportFormat("   ");

        assertEquals("tp", config.getTeleportFormat());
    }
}
