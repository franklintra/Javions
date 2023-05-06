package ch.epfl;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public final class CONFIGURATION {
    private CONFIGURATION() {}

    public static final class MAP {
        private MAP() {}

        /**
         * The max memory size of the MAP in bytes.
         */
        public static final double MAP_MEMORY_SIZE = 26.3 * 1e6; // 26.3 MB by default to load 100 tiles

        /**
         * The max memory size of the MAP in tiles loaded in the cache memory.
         */
        public static final int TILES_CACHE_SIZE = (int) (MAP_MEMORY_SIZE / constants.TILE_MEMORY_SIZE);

        /**
         * The default position of the MAP.
         */
        public static final GeoPos INITIAL_POSITION = new GeoPos((int) Units.convert(2.2794736259693136, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(48.88790023468289, Units.Angle.DEGREE, Units.Angle.T32));
        /**
         * The url of the tile server.
         */
        public static final String TILE_SERVER_URL = "tile.openstreetmap.org";


    }
}
