package es.cristichi.fnac.sound;

import kuusisto.tinysound.Sound;

/**
 * Composes a Sound with its Subtitle.
 * @param sound Sound.
 * @param subtitles Subtitles.
 */
public record SubtitledSound(Sound sound, Subtitles subtitles){
}
