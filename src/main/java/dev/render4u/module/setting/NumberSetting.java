package dev.render4u.module.setting;

import java.util.Locale;
import java.util.function.Supplier;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final int precision;
    private final String unit;

    public NumberSetting(String name, double defaultValue, double min, double max, double step, int precision, String unit, String description, Supplier<Boolean> visibility) {
        super(name, defaultValue, description, visibility);
        this.min = min;
        this.max = max;
        this.step = step;
        this.precision = precision;
        this.unit = unit != null ? unit : "";
        set(defaultValue);
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, int precision, String unit, String description) {
        this(name, defaultValue, min, max, step, precision, unit, description, () -> true);
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, int precision, String unit) {
        this(name, defaultValue, min, max, step, precision, unit, "", () -> true);
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, defaultValue, min, max, step, 1, "");
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public int getPrecision() {
        return precision;
    }

    public String getUnit() {
        return unit;
    }

    public float getFloat() {
        return get().floatValue();
    }

    public int getInt() {
        return get().intValue();
    }

    public long getLong() {
        return get().longValue();
    }

    public double getDouble() {
        return get();
    }

    @Override
    public void set(Double newValue) {
        if (newValue == null) return;
        double clamped = Math.max(min, Math.min(max, newValue));
        double stepped;
        if (step > 0.0) {
            stepped = Math.round((clamped - min) / step) * step + min;
            stepped = Math.max(min, Math.min(max, stepped));
        } else {
            stepped = clamped;
        }
        if (precision >= 0) {
            double factor = Math.pow(10, precision);
            stepped = Math.round(stepped * factor) / factor;
        }
        super.set(stepped);
    }

    public String getFormattedValue() {
        if (precision <= 0) {
            return String.format(Locale.US, "%d%s", getInt(), unit);
        } else {
            return String.format(Locale.US, "%." + precision + "f%s", get(), unit);
        }
    }
}
