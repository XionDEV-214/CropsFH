package com.deveisia.enceladus.cropsfh.crop;

public record CropStats(int growth, int gain, double resistance) {
    public static final int MAX_GROWTH = 10_000;
    public static final int MAX_GAIN = 10_000;
    public static final double MAX_RESISTANCE = 100.0;

    public static final CropStats DEFAULT_SEED = new CropStats(0, 1, 0.0);

    public CropStats {
        growth = clamp(growth, 0, MAX_GROWTH);
        gain = clamp(gain, 1, MAX_GAIN);
        resistance = Math.max(0.0, Math.min(resistance, MAX_RESISTANCE));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
