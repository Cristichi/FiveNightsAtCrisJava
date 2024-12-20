package es.cristichi.fnac.gui;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public record MenuItem(String id, String display, @Nullable BufferedImage loadingScreen){
}
