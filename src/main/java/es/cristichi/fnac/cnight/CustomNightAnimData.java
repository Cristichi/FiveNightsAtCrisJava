package es.cristichi.fnac.cnight;

/**
 * Custom Night's Animatronics must work with these parameters to generate the Animatronic.
 *
 * @param ai      AI level chosen. Custom Night does not allow increasing AI during the Night (it would be too complicated
 *                for users to have the choice).
 */
public record CustomNightAnimData(int ai) {
}
