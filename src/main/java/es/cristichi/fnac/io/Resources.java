package es.cristichi.fnac.io;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.sound.Subtitles;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class manages the resources, loading them correctly and keeping images on cache for future calls to the
 * same path.
 */
public class Resources {
    private static final Logger LOGGER = LoggerFactory.getLogger(Resources.class);
    /**
     * This is the folder inside the computer user's temp folder that is created for all the resources
     * that must be kept in temporary files (like Music and Sounds).
     */
    @SuppressWarnings("CanBeFinal") //To allow dependants to modify it if they want.
    public static String TEMP_FOLDER_NAME = "FNAC";
    private static File TEMP_FOLDER = null;
    
    /**
     * Provides a folder inside the temporary folder of the user.
     * @return An instance of File that represents a folder that is already created and will be deleted on exit.
     * @throws IOException If any issues happen when creating the folder.
     */
    private static File getTempFolder() throws IOException {
        if (TEMP_FOLDER != null && TEMP_FOLDER.exists()){
            return TEMP_FOLDER;
        }
        
        
        TEMP_FOLDER = Files.createTempDirectory(TEMP_FOLDER_NAME).toFile();
        TEMP_FOLDER.mkdirs();
        TEMP_FOLDER.deleteOnExit();
        return TEMP_FOLDER;
    }
    
    private static final HashMap<String, BufferedImage> loadedImgs = new HashMap<>(50);
    
    /**
     * Loads a {@link BufferedImage} from the resources.
     * @param resourcePath Path inside the resources folder.
     * @return The image.
     * @throws ResourceException If the image does not exist.
     */
    public static BufferedImage loadImage(String resourcePath) throws ResourceException {
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
     *
     * @param resourcePath Path inside the resources folder.
     * @param loop Whether this GifAnimation should loop to the start after finishing.
     * @return The GIF.
     * @throws ResourceException If the GIF does not exist.
     */
    public static GifAnimation loadGif(String resourcePath, boolean loop) throws ResourceException {
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
                    GifFrame.DisposalMethod disposalMethod = null;
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
                    NodeList metaKids = root.getChildNodes();
                    
                    for (int i1 = 0; i1 < metaKids.getLength(); i1++) {
                        Node metadataKid = metaKids.item(i1);
                        
                        if (metadataKid.getNodeName().equals("GraphicControlExtension")) {
                            Node delayNode = metadataKid.getAttributes().getNamedItem("delayTime");
                            if (delayNode != null) {
                                delay = Integer.parseInt(delayNode.getNodeValue());
                            }
                            
                            Node disposalNode = metadataKid.getAttributes().getNamedItem("disposalMethod");
                            if (disposalNode != null) {
                                String method = disposalNode.getNodeValue();
                                disposalMethod = switch (method) {
                                    case "restoreToBackgroundColor" ->
                                            GifFrame.DisposalMethod.RESTORE_TO_BACKGROUND_COLOR;
                                    case "restoreToPrevious" -> GifFrame.DisposalMethod.RESTORE_TO_PREVIOUS;
                                    case "doNotDispose" -> GifFrame.DisposalMethod.DO_NOT_DISPOSE;
                                    default -> GifFrame.DisposalMethod.UNSPECIFIED;
                                };
                            }
                        } else if (metadataKid.getNodeName().equals("ImageDescriptor")) {
                            Node offsetXNode = metadataKid.getAttributes().getNamedItem("imageLeftPosition");
                            Node offsetYNode = metadataKid.getAttributes().getNamedItem("imageTopPosition");
                            Node wNode = metadataKid.getAttributes().getNamedItem("imageWidth");
                            Node hNode = metadataKid.getAttributes().getNamedItem("imageHeight");
                            //Node interlace = metadataKid.getAttributes().getNamedItem("interlaceFlag");
                            if (offsetXNode != null) {
                                offsetX = Integer.parseInt(offsetXNode.getNodeValue());
                            }
                            if (offsetYNode != null) {
                                offsetY = Integer.parseInt(offsetYNode.getNodeValue());
                            }
                            if (wNode != null) {
                                width = Integer.parseInt(wNode.getNodeValue());
                            }
                            if (hNode != null) {
                                height = Integer.parseInt(hNode.getNodeValue());
                            }
                        }
                    }
                    if (delay == null){
                        throw new ResourceException("GIF has a frame without delay.");
                    }
    
                    frames.add(new GifFrame(frame, (double) delay /100, disposalMethod, offsetX, offsetY, width, height));
                }
                
                IIOMetadata streamMetadata = reader.getStreamMetadata();
                int width = -1;
                int height = -1;
                if (streamMetadata != null) {
                    String formatName = streamMetadata.getNativeMetadataFormatName();
                    if (formatName.equals("javax_imageio_gif_stream_1.0")) {
                        Node root = streamMetadata.getAsTree(formatName);
                        NodeList children = root.getChildNodes();
                        for (int i = 0; i < children.getLength(); i++) {
                            Node child = children.item(i);
                            if (child.getNodeName().equals("LogicalScreenDescriptor")) {
                                width = Integer.parseInt(child.getAttributes()
                                        .getNamedItem("logicalScreenWidth").getNodeValue());
                                height = Integer.parseInt(child.getAttributes()
                                        .getNamedItem("logicalScreenHeight").getNodeValue());
                            }
                        }
                    }
                }
                reader.dispose();
                if (width<0 || height<0){
                    throw new ResourceException("GIF %s has an invalid width (%d) or height (%d).%n"
                            .formatted(resourcePath, width, height));
                }
                return new GifAnimation(resourcePath, frames, width, height, loop);
            }
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            throw new ResourceException("Error when reading \"" + resourcePath + "\". Perhaps its missing.", e);
        }
    }
    
    /**
     * Loads a {@link Music} file from the resources. It stores it in the temp folder of the computer's user
     * in order to load it better, since Music instances are constantly read the file.
     * @param resourcePath Path inside the resources folder.
     * @return The Music.
     * @throws ResourceException If the Music does not exist.
     */
    public static Music loadMusic(String resourcePath) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            File tempFolder = getTempFolder();
            
            Path normalizedPath = Paths.get(resourcePath).normalize();
            String tempFileName = normalizedPath.toString().replace(File.separatorChar, '_').replace('/', '_');
            File tempFile = new File(tempFolder, tempFileName);
            tempFile.deleteOnExit();
            if (!tempFile.exists()){
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Music music = TinySound.loadMusic(tempFile, false);
            if (music == null){
                throw new NullPointerException("Music file %s could not be loaded from disk.".formatted(resourcePath));
            }
            return music;
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
     *
     * @param resourcePath Path inside the resources folder.
     * @return The Sound.
     * @throws ResourceException If the Sound does not exist.
     */
    public static Sound loadSound(String resourcePath) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null){
                throw new NullPointerException("Resource %s not found.".formatted(resourcePath));
            }
            File tempFolder = getTempFolder();
            
            Path normalizedPath = Paths.get(resourcePath).normalize();
            String tempFileName = normalizedPath.toString().replace(File.separatorChar, '_').replace('/', '_');
            File tempFile = new File(tempFolder, tempFileName);
            tempFile.deleteOnExit();
            if (!tempFile.exists()){
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            Sound sound = TinySound.loadSound(tempFile);
            if (sound == null){
                throw new NullPointerException("Sound file %s could not be loaded from disk.".formatted(resourcePath));
            }
            return sound;
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException(
                    "Sound file not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                            .formatted(resourcePath), notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"%s\".".formatted(resourcePath), e);
        }
    }
    
    /**
     * Loads a {@link Font} from the resources.
     * @param resourcePath Path inside the resources folder.
     * @return The Font.
     * @throws ResourceException If the Font does not exist.
     */
    public static Font loadFont(String resourcePath) throws ResourceException {
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
    
    /**
     * Loads a {@link Subtitles} from the resources.
     * @param resourcePath Path inside the resources folder.
     * @return The Font.
     * @throws ResourceException If the Font does not exist.
     */
    public static Subtitles loadSubtitles(String resourcePath) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new NullPointerException("Resource not found.");
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            ArrayList<Subtitles.Subtitle> subs = new ArrayList<>(30);
            
            int i = 0;
            long startTime = -1;
            long endTime = -1;
            StringBuilder text = new StringBuilder();
            
            for (Iterator<String> it = br.lines().iterator(); it.hasNext(); ) {
                String line = it.next().trim();
                if (line.isEmpty()) {
                    if (text.isEmpty() || startTime < 0 || endTime < 0) {
                        throw new IOException("Unexpected empty line on line " + i + ".");
                    }
                    subs.add(new Subtitles.Subtitle(startTime, endTime, text.toString().trim()));
                    text = new StringBuilder();
                    startTime = -1;
                    endTime = -1;
                } else if (line.contains(" --> ")) {
                    String[] times = line.split(" --> ");
                    if (times.length != 2) {
                        throw new IOException(
                                "Line \"" + line + "\" is not formatted correctly https://en.wikipedia.org/wiki/SubRip#Format.");
                    }
                    startTime = Subtitles.parseTime(times[0]);
                    endTime = Subtitles.parseTime(times[1]);
                } else {
                    try {
                        Integer.parseInt(line);
                        // We ignore the indexes.
                    } catch (NumberFormatException notInt){
                        if (!text.isEmpty()) {
                            text.append(" ").append(line);
                        } else {
                            text.append(line);
                        }
                    }
                }
                i++;
            }
            
            // Last line is not an empty line.
            if (!text.isEmpty() && startTime >= 0 && endTime >= 0) {
                subs.add(new Subtitles.Subtitle(startTime, endTime, text.toString().trim()));
                LOGGER.debug("Unexpected empty line at the end of the Subtitles file {}. We can ignore it.", resourcePath);
            }
            
            return new Subtitles(subs);
        } catch (NullPointerException notFoundE) {
            throw new ResourceException(
                    "Font file not found at \"%s\". Cristichi or otherwise the modder probably forgot to add it."
                            .formatted(resourcePath), notFoundE);
        } catch (Exception e) {
            throw new ResourceException("Error when trying to load Font "+resourcePath, e);
        }
    }
}