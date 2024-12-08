package es.cristichi.fnac.gui;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public record MenuItem(String id, String display, boolean stopMusic, @Nullable BufferedImage loadingScreen){
}
