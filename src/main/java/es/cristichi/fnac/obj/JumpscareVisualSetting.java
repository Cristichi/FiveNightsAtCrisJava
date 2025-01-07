package es.cristichi.fnac.obj;

/**
 * Visual setting to tell the Night's rendering method how to draw the Jumpscare on the screen.
 */
public enum JumpscareVisualSetting {
    /** Centered on the screen, scaled to fit as much of the screen as possible. Because centering was only a half
     * solution to avoid distortions, one of the CENTERED options should be used instead.
     */
    CENTERED,
    /** Centered-bottom on the screen, scaled to fit as much of the screen as possible. */
    MIDDLE_DOWN,
    /** Centered-up on the screen, scaled to fit as much of the screen as possible. */
    MIDDLE_UP,
    /** Stretched to fit the entire screen, distortions will appear but it allows the GIF to invade the entire screen. */
    FILL_SCREEN
}
