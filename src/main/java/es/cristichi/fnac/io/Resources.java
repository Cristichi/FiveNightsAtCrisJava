package es.cristichi.fnac.io;

import es.cristichi.fnac.exception.ResourceException;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;

public class Resources {
    public static InputStream loadInputStream(String path) throws ResourceException {
        InputStream in = Resources.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new ResourceException("Resource not found at \"" + path + "\". Probably Cristichi forgot to add it.");
        }
        return in;
    }

    public static BufferedImage loadImageResource(String path) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            return ImageIO.read(in);
        } catch (IOException | NullPointerException e) {
            throw new ResourceException("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
        }
    }

    public static GifAnimation loadGif(String resourcePath) throws ResourceException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(Resources.loadInputStream(resourcePath))) {
            if (stream == null) {
                throw new ResourceException("No suitable reader found for " + resourcePath + ".");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("No reader for: " + resourcePath);
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int numFrames = reader.getNumImages(true);
            ArrayList<GifFrame> frames = new ArrayList<>(numFrames);

            for (int i = 0; i < numFrames; i++) {
                Integer delay = null;
                GifDisposalMethod disposalMethod = null;
                int offsetX = 0;
                int offsetY = 0;
                int width = 0;
                int height = 0;

                BufferedImage frame = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);

                String metaFormatName = metadata.getNativeMetadataFormatName();
                if (!metaFormatName.equals("javax_imageio_gif_image_1.0")) {
                    throw new IllegalArgumentException("Unsupported metadata format: " + metaFormatName);
                }

                Node root = metadata.getAsTree(metaFormatName);
                NodeList children = root.getChildNodes();

                for (int i1 = 0; i1 < children.getLength(); i1++) {
                    Node child = children.item(i1);

                    if (child.getNodeName().equals("GraphicControlExtension")) {
                        Node delayNode = child.getAttributes().getNamedItem("delayTime");
                        if (delayNode != null) {
                            delay = Integer.parseInt(delayNode.getNodeValue());
                        }

                        Node disposalNode = child.getAttributes().getNamedItem("disposalMethod");
                        if (disposalNode != null) {
                            String method = disposalNode.getNodeValue();
                            disposalMethod = switch (method) {
                                case "restoreToBackgroundColor" -> GifDisposalMethod.RESTORE_TO_BACKGROUND_COLOR;
                                case "restoreToPrevious" -> GifDisposalMethod.RESTORE_TO_PREVIOUS;
                                case "doNotDispose" -> GifDisposalMethod.DO_NOT_DISPOSE;
                                default -> GifDisposalMethod.UNSPECIFIED;
                            };
                        }
                        if (delay != null && disposalMethod != null){
                            break;
                        }
                    } else if (child.getNodeName().equals("ImageDescriptor")){
                        Node offsetXNode = child.getAttributes().getNamedItem("imageLeftPosition");
                        Node offsetYNode = child.getAttributes().getNamedItem("imageTopPosition");
                        Node wNode = child.getAttributes().getNamedItem("imageWidth");
                        Node hNode = child.getAttributes().getNamedItem("imageHeight");
                        Node interlace = child.getAttributes().getNamedItem("interlaceFlag");
                        if (offsetXNode != null){
                            offsetX = Integer.parseInt(offsetXNode.getNodeValue());
                        }
                        if (offsetYNode != null){
                            offsetY = Integer.parseInt(offsetYNode.getNodeValue());
                        }
                        if (wNode != null){
                            width = Integer.parseInt(wNode.getNodeValue());
                        }
                        if (hNode != null){
                            height = Integer.parseInt(hNode.getNodeValue());
                        }

                    }
                }
                if (delay == null){
                    throw new ResourceException("GIF has a frame without delay.");
                }

                frames.add(new GifFrame(frame, (double) delay /100, disposalMethod, offsetX, offsetY, width, height));
            }

            reader.dispose();
            return new GifAnimation(frames);
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            throw new ResourceException("Error when reading \"" + resourcePath + "\".", e);
        }
    }


    public static Music loadMusic(String path, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return TinySound.loadMusic(tempFile.toFile(), false);
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException("Audio resource not found at \"" + path + "\". Probably Cristichi forgot to add it.", notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"" + path + "\".", e);
        }
    }

    public static Sound loadSound(String path, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return TinySound.loadSound(tempFile.toFile());
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException("Audio resource not found at \"" + path + "\". Probably Cristichi forgot to add it.", notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"" + path + "\".", e);
        }
    }

    public static Font loadCustomFont(String path) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (IOException | FontFormatException | NullPointerException e) {
            throw new ResourceException("Error when trying to load Font "+path, e);
        }
    }
}