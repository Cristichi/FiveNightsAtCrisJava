package es.cristichi.fnac.cnight;

import java.util.Random;

/**
 * Custom Night's Animatronics must work with these parameters to generate the Animatronic.
 *
 * @param name    Name of the Animatronic.
 * @param variant Name of the Variant, if it is. For instance, Cris has several variants with different behaviours.
 *                If this Animatronic is not a variant, or is the default one, this is an empty String.
 * @param ai      AI level chosen. Custom Night does not allow increasing AI during the Night (it would be too complicated
 *                for users to have the choice).
 * @param rng     Random for this Custom Night that is going to start. Used to randomize properties if useful.
 */
public record CustomNightAnimatronicData(String name, String variant, int ai, Random rng) {
}
