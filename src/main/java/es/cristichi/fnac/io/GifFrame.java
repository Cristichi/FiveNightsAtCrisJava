package es.cristichi.fnac.io;

import java.awt.image.BufferedImage;

public record GifFrame(BufferedImage image, double delaySecs, GifDisposalMethod disposalMethod, int offsetX,
                       int offsetY, int width, int height) {
}
