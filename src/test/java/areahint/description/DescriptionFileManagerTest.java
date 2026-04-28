package areahint.description;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import areahint.world.WorldFolderManager;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DescriptionFileManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void sanitizeDescriptionFileNameReplacesIllegalWindowsCharactersAndControlCharacters() {
        String sanitized = DescriptionFileManager.sanitizeDescriptionFileName("bad<name>:with\"chars|?*\\/line\nnext\r\t\u0000");

        assertEquals("bad_name__with_chars_____line_next___", sanitized);
    }

    @Test
    void sanitizeDescriptionFileNameFallsBackToUnknownForBlankName() {
        assertEquals("Unknown", DescriptionFileManager.sanitizeDescriptionFileName("   \r\n\t  "));
        assertEquals("Unknown", DescriptionFileManager.sanitizeDescriptionFileName(null));
    }

    @Test
    void sanitizeDescriptionFileNameAvoidsWindowsReservedDeviceNames() {
        assertEquals("CON_", DescriptionFileManager.sanitizeDescriptionFileName("CON"));
        assertEquals("lpt1_", DescriptionFileManager.sanitizeDescriptionFileName("lpt1"));
        assertEquals("NUL.txt_", DescriptionFileManager.sanitizeDescriptionFileName("NUL.txt"));
    }

    @Test
    void getAreaDescriptionFileUsesSanitizedSurfaceName() {
        Path file = DescriptionFileManager.getAreaDescriptionFile(tempDir, "overworld", "域名<Name>");

        assertEquals(tempDir.resolve("overworld").resolve("域名_Name_.json"), file);
    }

    @Test
    void getDimensionalDescriptionFileUsesDatabaseRootAndSanitizedDisplayName() {
        Path file = DescriptionFileManager.getDimensionalDescriptionFile(tempDir, "维度:名");

        assertEquals(tempDir.resolve("维度_名.json"), file);
    }

    @Test
    void readReturnsNullWhenFileDoesNotExist() {
        assertNull(DescriptionFileManager.readDescription(tempDir.resolve("missing.json")));
    }

    @Test
    void writeThenReadPreservesPlanFields() {
        Path file = tempDir.resolve("database").resolve("area.json");
        DescriptionData expected = new DescriptionData();
        expected.setSchemaVersion(1);
        expected.setTargetType("area");
        expected.setTargetName("真实域名");
        expected.setSurfaceName("联合域名");
        expected.setDimension("overworld");
        expected.setDescription("这是一段描述\n第二行");
        expected.setAuthor("Tester");
        expected.setUpdatedAt(1714219200000L);

        assertTrue(DescriptionFileManager.writeDescription(file, expected));

        DescriptionData actual = DescriptionFileManager.readDescription(file);
        assertNotNull(actual);
        assertEquals(expected.getSchemaVersion(), actual.getSchemaVersion());
        assertEquals(expected.getTargetType(), actual.getTargetType());
        assertEquals(expected.getTargetName(), actual.getTargetName());
        assertEquals(expected.getSurfaceName(), actual.getSurfaceName());
        assertEquals(expected.getDimension(), actual.getDimension());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt());
    }

    @Test
    void deleteReturnsTrueWhenFileDoesNotExistAndDeletesWhenPresent() throws Exception {
        Path file = tempDir.resolve("database").resolve("delete.json");

        assertTrue(DescriptionFileManager.deleteDescription(file));

        Files.createDirectories(file.getParent());
        Files.writeString(file, "{}");
        assertTrue(DescriptionFileManager.deleteDescription(file));
        assertFalse(Files.exists(file));
    }

    @Test
    void createDatabaseFoldersCreatesRootAndVanillaDimensionFolders() throws Exception {
        Path worldFolder = tempDir.resolve("world");

        WorldFolderManager.createDatabaseFolders(worldFolder);

        assertTrue(Files.isDirectory(worldFolder.resolve("database")));
        assertTrue(Files.isDirectory(worldFolder.resolve("database").resolve("overworld")));
        assertTrue(Files.isDirectory(worldFolder.resolve("database").resolve("the_nether")));
        assertTrue(Files.isDirectory(worldFolder.resolve("database").resolve("the_end")));
    }
}
