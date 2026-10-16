package moth.tempestra.client.lightning;

public final class TempestraLightningVisuals {
    public static final boolean CUSTOM_VISUALS_ENABLED = true;

    public static final double FALLBACK_CLOUD_BASE_Y = 192.0D;
    public static final double MIN_FULL_BOLT_VERTICAL_DISTANCE = 32.0D;
    public static final double COMPACT_BOLT_HEIGHT = 22.0D;

    public static final int MIN_MAIN_SEGMENTS = 3;
    public static final int MAX_MAIN_SEGMENTS = 16;
    public static final int MIN_COMPACT_SEGMENTS = 2;
    public static final int MAX_COMPACT_SEGMENTS = 4;

    public static final int VISUAL_LIFETIME_TICKS = 16;
    public static final int COMPACT_VISUAL_LIFETIME_TICKS = 9;
    public static final int IMPACT_SPARK_LIFETIME_TICKS = 12;
    public static final int ATMOSPHERIC_SPARK_LIFETIME_TICKS = 10;
    public static final int IMPACT_AFTERGLOW_TICKS = 28;

    public static final float MAIN_BRANCH_CHANCE_MAX = 0.8F;
    public static final float SPLIT_BRANCH_CHANCE_MAX = 0.16F;
    public static final float SPLIT_DENSITY = 0.82F;
    public static final float TERMINAL_FILAMENT_CHANCE = 0.68F;

    public static final int CORE_COLOR = 0xF5FCFF;
    public static final int INNER_COLOR = 0xC9F0FF;
    public static final int OUTER_COLOR = 0x64BEFF;
    public static final int EDGE_COLOR = 0x9BDEFF;
    public static final int IMPACT_COLOR = 0xE1F7FF;
    public static final int COPPER_IMPACT_COLOR = 0xECFFFF;
    public static final int COPPER_ELECTRIC_COLOR = 0x53E6FF;
    public static final int GROUND_IMPACT_COLOR = 0xD7F4FF;

    public static final float MAIN_CORE_RADIUS = 0.024F;
    public static final float MAIN_INNER_RADIUS = 0.052F;
    public static final float MAIN_OUTER_RADIUS = 0.118F;
    public static final float SEGMENT_WIDTH_RANDOM_MIN = 0.86F;
    public static final float SEGMENT_WIDTH_RANDOM_MAX = 1.30F;
    public static final float SPLIT_WIDTH_SCALE = 0.72F;
    public static final float BRANCH_WIDTH_SCALE = 0.56F;
    public static final float FILAMENT_WIDTH_SCALE = 0.27F;
    public static final float IMPACT_SIZE_SCALE = 2.0F;
    public static final float SPLIT_FADE_ALPHA_FLOOR = 0.18F;
    public static final float BRANCH_FADE_ALPHA_FLOOR = 0.24F;
    public static final float DISTANT_READABILITY_START = 48.0F;
    public static final float DISTANT_READABILITY_END = 168.0F;
    public static final float DISTANT_RADIUS_BOOST = 0.62F;
    public static final float DISTANT_SECONDARY_ALPHA_BOOST = 0.22F;
    public static final int TUBE_SIDES = 6;

    private TempestraLightningVisuals() {
    }
}
