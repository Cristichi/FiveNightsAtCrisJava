package es.cristichi.fnac.anim;

/**
 * Visual setting to tell the Night's rendering method how to draw the Jumpscare on the screen.
 */
public enum JumpscareVisualSetting {
    /** Centered on the screen, scaled to fit as much of the screen as possible. Because centering was only a half
     * solution to avoid distortions, one of the CENTERED options should be used instead.
     */
    CENTERED,
    /** Centered-up on the screen, scaled to fit as much of the screen as possible. */
    CENTER_TOP,
    /** Centered-left on the screen, scaled to fit as much of the screen as possible. */
    CENTER_LEFT,
    /** Centered-rght on the screen, scaled to fit as much of the screen as possible. */
    CENTER_RIGHT,
    /** Centered-bottom on the screen, scaled to fit as much of the screen as possible. */
    CENTER_BOTTOM,
    /** Stretched to fit the entire screen, distortions will appear but it allows the GIF to invade the entire screen. */
    FILL_SCREEN
}
