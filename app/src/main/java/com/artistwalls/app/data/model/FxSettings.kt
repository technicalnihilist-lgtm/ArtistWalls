package com.artistwalls.app.data.model

/**
 * Every tweakable value from the Settings screen, with the same defaults and ranges
 * as the web build.
 *
 * A note on parity: the web can blur and saturate whatever is *behind* a panel
 * (`backdrop-filter`). Compose cannot sample the backdrop, so the blur / saturation /
 * brightness / refraction sliders have no Android equivalent and are not offered here;
 * everything that can be drawn is.
 */
data class FxValues(
    // --- Liquid glass ---
    val glassTint: Float = 1f,          // 0..2   fill / frost strength
    val glassRim: Float = 1f,           // 0..2   edge highlight
    // --- 4D depth ---
    val tilt: Float = 9f,               // 0..25  degrees
    val lift: Float = 22f,              // 0..60  how far the card rises on touch
    val shadow: Float = 1f,             // 0..2   shadow depth
    val parallax: Float = 1f,           // 0..2   photo parallax
    val glare: Float = 1f,              // 0..2   glare strength
    // --- Bounce ---
    val bounceDuration: Float = 0.68f,  // 0.3..1.5 s
    val bounceAmount: Float = 1f,       // 0..2.5  overshoot
    // --- macOS ---
    val macDuration: Float = 0.52f,     // 0.2..1.4 s
    val macSize: Float = 0.30f          // 0.05..0.9 genie start size
)

/** Which effect a slider belongs to. */
enum class FxGroup { GLASS, DEPTH, BOUNCE, MAC }

/** One slider: key, label, range, step and unit — mirrors the web `FX_SLIDERS`. */
data class FxSlider(
    val group: FxGroup,
    val label: String,
    val min: Float,
    val max: Float,
    val step: Float,
    val unit: String = "",
    val note: String? = null,
    val get: (FxValues) -> Float,
    val set: (FxValues, Float) -> FxValues
)

object FxSliders {
    val all: List<FxSlider> = listOf(
        FxSlider(FxGroup.GLASS, "Glass fill / frost", 0f, 2f, 0.05f, "×",
            get = { it.glassTint }, set = { v, x -> v.copy(glassTint = x) }),
        FxSlider(FxGroup.GLASS, "Edge highlight", 0f, 2f, 0.05f, "×",
            get = { it.glassRim }, set = { v, x -> v.copy(glassRim = x) }),

        FxSlider(FxGroup.DEPTH, "Tilt angle", 0f, 25f, 1f, "°",
            get = { it.tilt }, set = { v, x -> v.copy(tilt = x) }),
        FxSlider(FxGroup.DEPTH, "Lift on touch", 0f, 60f, 1f, "",
            get = { it.lift }, set = { v, x -> v.copy(lift = x) }),
        FxSlider(FxGroup.DEPTH, "Shadow depth", 0f, 2f, 0.05f, "×",
            get = { it.shadow }, set = { v, x -> v.copy(shadow = x) }),
        FxSlider(FxGroup.DEPTH, "Photo parallax", 0f, 2f, 0.05f, "×",
            get = { it.parallax }, set = { v, x -> v.copy(parallax = x) }),
        FxSlider(FxGroup.DEPTH, "Glare", 0f, 2f, 0.05f, "×",
            get = { it.glare }, set = { v, x -> v.copy(glare = x) }),

        FxSlider(FxGroup.BOUNCE, "Speed (duration)", 0.3f, 1.5f, 0.02f, "s",
            get = { it.bounceDuration }, set = { v, x -> v.copy(bounceDuration = x) }),
        FxSlider(FxGroup.BOUNCE, "Bounce amount", 0f, 2.5f, 0.05f, "×",
            get = { it.bounceAmount }, set = { v, x -> v.copy(bounceAmount = x) }),

        FxSlider(FxGroup.MAC, "Speed (duration)", 0.2f, 1.4f, 0.02f, "s",
            get = { it.macDuration }, set = { v, x -> v.copy(macDuration = x) }),
        FxSlider(FxGroup.MAC, "Genie start size", 0.05f, 0.9f, 0.01f, "",
            note = "small = deep genie",
            get = { it.macSize }, set = { v, x -> v.copy(macSize = x) })
    )

    fun forGroup(group: FxGroup) = all.filter { it.group == group }

    fun format(slider: FxSlider, value: Float): String {
        val number = if (slider.step >= 1f) value.toInt().toString()
        else {
            val decimals = if (slider.step < 0.1f) 2 else 2
            String.format("%.${decimals}f", value).trimEnd('0').trimEnd('.')
        }
        return number + slider.unit
    }
}

/**
 * Visual effects the user can switch on or off, plus their fine-tuning values.
 * All four default to on, matching the web build.
 */
data class FxSettings(
    val glass: Boolean = true,
    val depth: Boolean = true,
    val bounce: Boolean = true,
    val mac: Boolean = true,
    val values: FxValues = FxValues()
) {
    fun isOn(group: FxGroup): Boolean = when (group) {
        FxGroup.GLASS -> glass
        FxGroup.DEPTH -> depth
        FxGroup.BOUNCE -> bounce
        FxGroup.MAC -> mac
    }

    fun withGroup(group: FxGroup, on: Boolean): FxSettings = when (group) {
        FxGroup.GLASS -> copy(glass = on)
        FxGroup.DEPTH -> copy(depth = on)
        FxGroup.BOUNCE -> copy(bounce = on)
        FxGroup.MAC -> copy(mac = on)
    }

    /** Reset only this group's values back to the defaults. */
    fun resetValues(group: FxGroup): FxSettings {
        val d = FxValues()
        var v = values
        FxSliders.forGroup(group).forEach { slider -> v = slider.set(v, slider.get(d)) }
        return copy(values = v)
    }

    fun resetAll(): FxSettings = FxSettings()
}
