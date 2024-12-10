package es.cristichi.fnac.io;

import java.awt.image.BufferedImage;

public record GifFrame(BufferedImage image, double delaySecs, GifDisposalMethod disposalMethod, int offsetX,
                       int offsetY, int width, int height) {

    @Override
    public String toString() {
        return "GifFrame{" +
                "image=" + (image==null?"image":"null") +
                ", delaySecs=" + delaySecs +
                ", disposalMethod=" + disposalMethod +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
