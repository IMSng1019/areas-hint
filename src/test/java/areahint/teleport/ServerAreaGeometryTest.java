package areahint.teleport;

import areahint.data.AreaData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerAreaGeometryTest {
    private static AreaData squareArea() {
        return new AreaData(
                "square",
                List.of(
                        new AreaData.Vertex(0, 0),
                        new AreaData.Vertex(10, 0),
                        new AreaData.Vertex(10, 10),
                        new AreaData.Vertex(0, 10)
                ),
                List.of(
                        new AreaData.Vertex(0, 10),
                        new AreaData.Vertex(10, 10),
                        new AreaData.Vertex(10, 0),
                        new AreaData.Vertex(0, 0)
                ),
                new AreaData.AltitudeData(80.0, 40.0),
                1,
                null,
                null
        );
    }

    @Test
    void detectsPointInsideAabbPolygonAndAltitude() {
        AreaData area = squareArea();

        assertTrue(ServerAreaGeometry.contains(area, 5.0, 64.0, 5.0));
    }

    @Test
    void rejectsPointOutsidePolygonOrAltitude() {
        AreaData area = squareArea();

        assertFalse(ServerAreaGeometry.contains(area, 12.0, 64.0, 5.0));
        assertFalse(ServerAreaGeometry.contains(area, 5.0, 90.0, 5.0));
    }
}
