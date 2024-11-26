package es.cristichi.fnac.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveFileIO {
    public static final String SAVE_FILE = "save.fnac"; // Usual save file
    private static final String MAGIC_NUMBER = "FNACSV1"; // Unique magic number for FNAC save files. Version 1.

    /**
     * Saves the completed nights to a binary file.
     *
     * @param path The path to the save file.
     * @param saveFile The FNACSaveFile object containing the save data.
     * @throws IOException If an error occurs while writing to the file.
     */
    public static void saveToFile(String path, SaveFile saveFile) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {
            // Write the magic number
            out.writeBytes(MAGIC_NUMBER);

            // Write the number of completed nights
            List<String> completedNights = saveFile.completedNights();
            out.writeInt(completedNights.size());

            // Write each completed night
            for (String night : completedNights) {
                out.writeUTF(night);
            }
        }
    }

    /**
     * Loads the completed nights from a binary file.
     *
     * @param path The path to the save file.
     * @return An FNACSaveFile object containing the loaded save data.
     * @throws IOException If an error occurs while reading the file.
     * @throws IllegalArgumentException If the magic number is invalid.
     */
    public static SaveFile loadFromFile(String path) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(path))) {
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

    public record SaveFile(List<String> completedNights) {
            /**
             * Constructs an FNACSaveFile object with the given completed nights.
             *
             * @param completedNights The list of completed night names.
             */
            public SaveFile(List<String> completedNights) {
                this.completedNights = new ArrayList<>(completedNights);
            }

            /**
             * Returns the list of completed nights.
             *
             * @return The list of completed nights.
             */
            @Override
            public List<String> completedNights() {
                return new ArrayList<>(completedNights);
            }

            /**
             * Adds a new completed night to the list.
             *
             * @param night The name of the completed night.
             */
            public void addCompletedNight(String night) {
                if (!completedNights.contains(night)) {
                    completedNights.add(night);
                }
            }
        }
}
