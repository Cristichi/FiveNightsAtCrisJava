package es.cristichi.fnac.obj.sound;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that handles subtitle files in the SubRip Subtitle (.srt) format.
 */
@SuppressWarnings("unused") // TODO WIP stop shouting at me intellij I'm on it
public class Subtitles {
    /**
     * Loads the given .srt file.
     * @param path Path to the .srt file.
     * @return A Subtitles object with the information of these subtitles.
     * @throws IOException If file could not be read or the format is incorrect.
     * @throws NumberFormatException If the start or end times of a line could not be parsed to milliseconds.
     */
    public static Subtitles fromFile(Path path) throws IOException, NumberFormatException {
        List<String> lines = Files.readAllLines(path);
        ArrayList<Subtitle> subs = new ArrayList<>(lines.size()/3);
        
        long startTime = -1;
        long endTime = -1;
        StringBuilder text = new StringBuilder();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = line.trim();
            if (line.isEmpty()) {
                if (text.isEmpty() || startTime <= 0 || endTime <= 0) {
                    throw new IOException("Unexpected empty line on line "+i+".");
                }
                subs.add(new Subtitle(startTime, endTime, text.toString().trim()));
                text = new StringBuilder();
                startTime = -1;
                endTime = -1;
            } else if (line.contains(" --> ")) {
                String[] times = line.split(" --> ");
                if (times.length != 2) {
                    throw new IOException(
                            "Line \"" + line + "\" is not formatted correctly https://en.wikipedia.org/wiki/SubRip#Format.");
                }
                startTime = parseTime(times[0]);
                endTime = parseTime(times[1]);
            } else if (!text.isEmpty()) {
                text.append(" ").append(line);
            } else {
                text.append(line);
            }
        }
        
        // Last line is not an empty line.
        if (text.isEmpty() || startTime <= 0 || endTime <= 0) {
            throw new IOException("Unexpected empty line on line "+(lines.size()-1)+".");
        }
        subs.add(new Subtitle(startTime, endTime, text.toString().trim()));
        
        return new Subtitles(subs);
    }
    
    private final List<Subtitle> subtitles;
    
    /**
     * Creates a new empty Subtitles object.
     * @param list List of subtitles.
     */
    public Subtitles(List<Subtitle> list) {
        this.subtitles = Collections.unmodifiableList(list);
    }
    
    /**
     * It gets the correct Subtitle for this msecs.
     * @param msecs The msecs in milliseconds, relative to the start of the Sound.
     * @return The instance of Subtitle that should be live on the given msecs,
     * or {@code null} if no Subtitle should appear.
     */
    public Subtitle getSubtitle(long msecs) {
        for (Subtitle sub : subtitles){
            if (msecs >= sub.startTimeMs() && msecs < sub.endTimeMs()){
                return sub;
            }
        }
        return null;
    }
    
    /**
     * @return List of Subtitles, ordered.
     */
    public List<Subtitle> getSubtitles() {
        return subtitles;
    }
    
    /**
     * @param time Time in the format used by .srt files.
     * @return Number of milliseconds represented by the given time.
     * @throws NumberFormatException If the time given is not correctly formatted.
     */
    public static long parseTime(String time) throws NumberFormatException {
        String[] parts = time.split("[:,]");
        if (parts.length!=4){
            throw new NumberFormatException("The time is not in the right format.");
        }
        long hours = Long.parseLong(parts[0]) * 3600000;
        long minutes = Long.parseLong(parts[1]) * 60000;
        long seconds = Long.parseLong(parts[2]) * 1000;
        long milliseconds = Long.parseLong(parts[3]);
        return hours + minutes + seconds + milliseconds;
    }
    
    /**
     * Class that represents a single subtitle line.
     *
     * @param startTimeMs Start time in milliseconds.
     * @param endTimeMs   End time in milliseconds.
     * @param text        Text to show during that time.
     */
    public record Subtitle(long startTimeMs, long endTimeMs, String text) {
    }
}
