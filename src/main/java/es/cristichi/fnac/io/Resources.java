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
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class manages the resources, loading them correctly and keeping images on cache for future calls to the
 * same path.
 */
public class Resources {
    private static final HashMap<String, BufferedImage> loadedImgs = new HashMap<>(50);
    
    /**
     * Loads a {@link BufferedImage} from the resources.
     * @param resourcePath Path inside the resources folder.
     * @return The image.
     * @throws ResourceException If the image does not exist.
     */
    public static BufferedImage loadImageResource(String resourcePath) throws ResourceException {
        if (loadedImgs.containsKey(resourcePath)){
            return loadedImgs.get(resourcePath);
        }
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            BufferedImage img = ImageIO.read(in);
            loadedImgs.put(resourcePath, img);
            return img;
        } catch (IOException | NullPointerException e) {
            throw new ResourceException(
                    "Resource not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                            .formatted(resourcePath), e);
        }
    }
    
    /**
     * Loads a {@link GifAnimation} from the resources.
     * @param resourcePath Path inside the resources folder.
     * @return The GIF.
     * @throws ResourceException If the GIF does not exist.
     */
    public static GifAnimation loadGif(String resourcePath) throws ResourceException {
        try {
            InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                throw new ResourceException(
                        "Resource not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                                .formatted(resourcePath));
            }
            try (ImageInputStream stream = ImageIO.createImageInputStream(in)) {
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
                    GifFrameDisposalMethod disposalMethod = null;
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
                                    case "restoreToBackgroundColor" -> GifFrameDisposalMethod.RESTORE_TO_BACKGROUND_COLOR;
                                    case "restoreToPrevious" -> GifFrameDisposalMethod.RESTORE_TO_PREVIOUS;
                                    case "doNotDispose" -> GifFrameDisposalMethod.DO_NOT_DISPOSE;
                                    default -> GifFrameDisposalMethod.UNSPECIFIED;
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
            }
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            throw new ResourceException("Error when reading \"" + resourcePath + "\". Perhaps its missing.", e);
        }
    }
    
    /**
     * Loads a {@link Music} file from the resources. It stores it in the temp folder of the computer's user
     * in order to load it better, since Music instances are constantly read the file.
     * @param resourcePath Path inside the resources folder.
     * @param tmpFile Name of the temp file. Must be unique.
     * @return The Music.
     * @throws ResourceException If the Music does not exist.
     */
    public static Music loadMusic(String resourcePath, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return TinySound.loadMusic(tempFile.toFile(), false);
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException(
                    "Music file not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                            .formatted(resourcePath), notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"" + resourcePath + "\".", e);
        }
    }
    
    /**
     * Loads a {@link Sound} file from the resources. It stores it in the temp folder of the computer's user
     * in order to load it better, since Sounds are constantly read the file.
     * @param resourcePath Path inside the resources folder.
     * @param tmpFile Name of the temp file. Must be unique.
     * @return The Sound.
     * @throws ResourceException If the Sound does not exist.
     */
    public static Sound loadSound(String resourcePath, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return TinySound.loadSound(tempFile.toFile());
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException(
                    "Sound file not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                            .formatted(resourcePath), notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"" + resourcePath + "\".", e);
        }
    }
    
    /**
     * Loads a {@link Font} from the resources.
     * @param resourcePath Path inside the resources folder.
     * @return The Font.
     * @throws ResourceException If the Font does not exist.
     */
    public static Font loadCustomFont(String resourcePath) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new NullPointerException("Resource not found.");
            }
            return Font.createFont(Font.TRUETYPE_FONT, in);
            
        } catch (NullPointerException notFoundE) {
            throw new ResourceException(
                    "Font file not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                            .formatted(resourcePath), notFoundE);
        } catch (IOException | FontFormatException e) {
            throw new ResourceException("Error when trying to load Font "+resourcePath, e);
        }
    }
}