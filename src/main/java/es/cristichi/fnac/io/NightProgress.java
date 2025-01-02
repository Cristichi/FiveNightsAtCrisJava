package es.cristichi.fnac.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the save file that has the Nights the player has completed.
 */
public class NightProgress {
    /**
     * Usual name of the save file.
     */
    public static final String SAVE_FILE_NAME = "save.fnac"; // Save file's name
    private static final String MAGIC_NUMBER = "FNACSV1"; // Unique magic number for FNAC save files. Version 1.

    private static String pathToFnacFolder = null;
    
    /**
     * It gets the Documents folder of the computer's user that runs the game, and creates a new folder inside it
     * for save files to use.
     */
    public static void init(){
        pathToFnacFolder = "%s/Documents/Five Nights at Cris/".formatted(System.getProperty("user.home"));
        new File(pathToFnacFolder).mkdirs();
    }

    /**
     * Loads the completed nights from a binary file.
     *
     * @param path The path from the user's Documents folder and name.
     * @return An FNACSaveFile object containing the loaded save data.
     * @throws IOException If an error occurs while reading the file.
     * @throws IllegalArgumentException If the magic number is invalid.
     */
    public static SaveFile loadFromFile(String path) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(pathToFnacFolder+path))) {
            // Verify the magic number
            String magic = new String(in.readNBytes(MAGIC_NUMBER.getBytes().length));
            if (!MAGIC_NUMBER.equals(magic)) {
                throw new IllegalArgumentException("Invalid save file format.");
            }

            // Read the number of completed nights
            int numberOfNights = in.readInt();

            // Read each completed night
            List<String> completedNights = new ArrayList<>();
            for (int i = 0; i < numberOfNights; i++) {
                completedNights.add(in.readUTF());
            }

            return new SaveFile(completedNights);
        } catch (FileNotFoundException e){
            return new SaveFile(List.of());
        }
    }
    
    /**
     * Information saved on a saved file.
     * @param completedNights
     */
    public record SaveFile(List<String> completedNights) {
            /**
             * Constructs a SaveFile object with the given completed Nights.
             *
             * @param completedNights The list of completed Night names.
             */
            public SaveFile(List<String> completedNights) {
                this.completedNights = new ArrayList<>(completedNights);
            }

            /**
             * Returns the list of completed Nights.
             *
             * @return The list of completed Nights.
             */
            @Override
            public List<String> completedNights() {
                return new ArrayList<>(completedNights);
            }

            /**
             * Adds a new completed Night to the list. This will not automatically save the file, it must be done
             * at a later point for the changes to be saved.
             *
             * @param night The name of the completed Night.
             */
            public void addCompletedNight(String night) {
                if (!completedNights.contains(night)) {
                    completedNights.add(night);
                }
            }

            /**
             * Saves the completed nights to a binary file.
             *
             * @param path The path from the user's Documents folder and name.
             * @throws IOException If an error occurs while writing to the file.
             */
            public void saveToFile(String path) throws IOException {
                try (DataOutputStream out = new DataOutputStream(new FileOutputStream(pathToFnacFolder+path))) {
                    out.writeBytes(MAGIC_NUMBER);
                    out.writeInt(completedNights.size());
                    for (String night : completedNights) {
                        out.writeUTF(night);
                    }
                }
            }
        }
}
