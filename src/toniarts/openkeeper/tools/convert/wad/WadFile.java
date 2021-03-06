/*
 * Copyright (C) 2014-2015 OpenKeeper
 *
 * OpenKeeper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenKeeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenKeeper.  If not, see <http://www.gnu.org/licenses/>.
 */
package toniarts.openkeeper.tools.convert.wad;

import toniarts.openkeeper.tools.convert.ConversionUtils;
import toniarts.openkeeper.tools.convert.IResourceReader;
import toniarts.openkeeper.tools.convert.ResourceReader;
import toniarts.openkeeper.utils.PathUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the wad file structure and contains the methods to handle the WAD archive<br>
 * The file is LITTLE ENDIAN I might say<br>
 * Converted to JAVA from C code, C code by:
 * <li>Tomasz Lis</li>
 * <li>Anonymous</li>
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class WadFile {

    private final File file;
    private final Map<String, WadFileEntry> wadFileEntries;
    private static final String WAD_HEADER_IDENTIFIER = "DWFB";
    private static final int WAD_HEADER_VERSION = 2;

    private static final Logger LOGGER = Logger.getLogger(WadFile.class.getName());

    /**
     * Constructs a new Wad file reader<br>
     * Reads the WAD file structure
     *
     * @param file the wad file to read
     */
    public WadFile(File file) {
        this.file = file;

        // Read the file
        try (IResourceReader rawWad = new ResourceReader(file)) {

            // Check the header
            String header = rawWad.readString(4);
            if (!WAD_HEADER_IDENTIFIER.equals(header)) {
                throw new RuntimeException("Header should be " + WAD_HEADER_IDENTIFIER + " and it was " + header + "! Cancelling!");
            }

            // See the version
            int version = rawWad.readUnsignedInteger();
            if (WAD_HEADER_VERSION != version) {
                throw new RuntimeException("Version header should be " + WAD_HEADER_VERSION + " and it was " + version + "! Cancelling!");
            }

            // Seek
            rawWad.seek(0x48);

            int files = rawWad.readUnsignedInteger();
            int nameOffset = rawWad.readUnsignedInteger();
            int nameSize = rawWad.readUnsignedInteger();
            int unknown = rawWad.readUnsignedInteger();

            // Loop through the file count
            List<WadFileEntry> entries = new ArrayList<>(files);
            for (int i = 0; i < files; i++) {
                WadFileEntry wadInfo = new WadFileEntry();
                wadInfo.setUnk1(rawWad.readUnsignedInteger());
                wadInfo.setNameOffset(rawWad.readUnsignedInteger());
                wadInfo.setNameSize(rawWad.readUnsignedInteger());
                wadInfo.setOffset(rawWad.readUnsignedInteger());
                wadInfo.setCompressedSize(rawWad.readUnsignedInteger());
                int typeIndex = rawWad.readUnsignedInteger();
                switch (typeIndex) {
                    case 0: {
                        wadInfo.setType(WadFileEntry.WadFileEntryType.NOT_COMPRESSED);
                        break;
                    }
                    case 4: {
                        wadInfo.setType(WadFileEntry.WadFileEntryType.COMPRESSED);
                        break;
                    }
                    default: {
                        wadInfo.setType(WadFileEntry.WadFileEntryType.UNKNOWN);
                    }
                }
                wadInfo.setSize(rawWad.readUnsignedInteger());
                int[] unknown2 = new int[3];
                unknown2[0] = rawWad.readUnsignedInteger();
                unknown2[1] = rawWad.readUnsignedInteger();
                unknown2[2] = rawWad.readUnsignedInteger();
                wadInfo.setUnknown2(unknown2);
                entries.add(wadInfo);
            }

            // Read the file names and put them to a hashmap
            // If the file has a path, carry that path all the way to next entry with path
            // The file names itself aren't unique, but with the path they are
            rawWad.seek(nameOffset);
            byte[] nameArray = rawWad.read(nameSize);
            wadFileEntries = new LinkedHashMap<>(files);
            String path = "";
            for (WadFileEntry entry : entries) {
                int offset = entry.getNameOffset() - nameOffset;
                String name = ConversionUtils.toString(Arrays.copyOfRange(nameArray, offset, offset + entry.getNameSize())).trim();

                // The path
                name = ConversionUtils.convertFileSeparators(name);
                int index = name.lastIndexOf(File.separator);
                if (index > -1) {
                    path = name.substring(0, index + 1);
                } else if (!path.isEmpty()) {
                    name = path + name;
                }

                wadFileEntries.put(name, entry);
            }
        } catch (IOException e) {

            //Fug
            throw new RuntimeException("Failed to open the file " + file + "!", e);
        }
    }

    /**
     * Get the individual file names as a list
     *
     * @return list of the file names
     */
    public List<String> getWadFileEntries() {
        return new ArrayList(wadFileEntries.keySet());
    }

    /**
     * Return the file count in this WAD archive
     *
     * @return file entries count
     */
    public int getWadFileEntryCount() {
        return wadFileEntries.size();
    }

    /**
     * Extract all the files to a given location
     *
     * @param destination destination directory
     */
    public void extractFileData(String destination) {

        // Open the WAD for extraction
        try (IResourceReader rawWad = new ResourceReader(file)) {

            for (String fileName : wadFileEntries.keySet()) {
                extractFileData(fileName, destination, rawWad);
            }
        } catch (Exception e) {

            // Fug
            throw new RuntimeException("Failed to read the WAD file!", e);
        }
    }

    /**
     * Extract a single file to a given location
     *
     * @param fileName    file to extract
     * @param destination destination directory
     * @param rawWad      the opened WAD file
     */
    private File extractFileData(String fileName, String destination, IResourceReader rawWad) {

        // See that the destination is formatted correctly and create it if it does not exist
        String dest = PathUtils.fixFilePath(destination);

        String mkdir = dest;
        if (fileName.contains(File.separator)) {
            mkdir += fileName.substring(0, fileName.lastIndexOf(File.separator) + 1);
        }

        File destinationFolder = new File(mkdir);
        destinationFolder.mkdirs();
        dest = dest.concat(fileName);

        // Write to the file
        try (OutputStream outputStream = new FileOutputStream(dest)) {
            getFileData(fileName, rawWad).writeTo(outputStream);
            return new File(dest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to " + dest + "!", e);
        }
    }

    /**
     * Extract a single file to a given location
     *
     * @param fileName    file to extract
     * @param destination destination directory
     * @return the file for the extracted contents
     */
    public File extractFileData(String fileName, String destination) {

        // Open the WAD for extraction
        try (IResourceReader rawWad = new ResourceReader(file)) {
            return extractFileData(fileName, destination, rawWad);
        } catch (Exception e) {

            // Fug
            throw new RuntimeException("Failed to read the WAD file!", e);
        }
    }

    /**
     * Extract a single file
     *
     * @param fileName the file to extract
     * @param rawWad   the opened WAD file
     * @return the file data
     */
    private ByteArrayOutputStream getFileData(String fileName, IResourceReader rawWad) {
        ByteArrayOutputStream result = null;

        // Get the file
        WadFileEntry fileEntry = wadFileEntries.get(fileName);
        if (fileEntry == null) {
            throw new RuntimeException("File " + fileName + " not found from the WAD archive!");
        }

        try {

            // Seek to the file we want and read it
            rawWad.seek(fileEntry.getOffset());
            byte[] bytes = rawWad.read(fileEntry.getCompressedSize());

            result = new ByteArrayOutputStream();

            // See if the file is compressed
            if (fileEntry.isCompressed()) {
                result.write(decompressFileData(bytes, fileName));
            } else {
                result.write(bytes);
            }

        } catch (Exception e) {

            // Fug
            throw new RuntimeException("Failed to read the WAD file!", e);
        }

        return result;
    }

    /**
     * Extract a single file
     *
     * @param fileName the file to extract
     * @return the file data
     */
    public ByteArrayOutputStream getFileData(String fileName) {

        // Open the WAD for extraction
        try (IResourceReader rawWad = new ResourceReader(file)) {
            return getFileData(fileName, rawWad);
        } catch (Exception e) {

            // Fug
            throw new RuntimeException("Failed to read the WAD file!", e);
        }
    }

    /**
     * Some file entries in the WAD are compressed (type 4?), this decompresses the file data
     *
     * @param src      the compressed bytes
     * @param fileName just for logging
     * @return the decompressed bytes
     */
    private byte[] decompressFileData(byte[] src, String fileName) {
        int i = 0, j = 0;
        if ((src[i++] & 1) != 0) {
            i += 3;
        }
        i++; // <<skip second byte
        // <decompressed size packed into 3 bytes

        int decsize = (ConversionUtils.toUnsignedByte(src[i]) << 16) + (ConversionUtils.toUnsignedByte(src[i + 1]) << 8) + ConversionUtils.toUnsignedByte(src[i + 2]);
        byte[] dest = new byte[decsize];
        i += 3;
        byte flag; // The flag byte read at the beginning of each main loop iteration
        int counter; // Counter for all loops
        boolean finished = false;
        while (!finished) {
            if (i >= src.length) {
                break;
            }
            flag = src[i++]; // Get flag byte
            if ((ConversionUtils.toUnsignedByte(flag) & 0x80) == 0) {
                byte tmp = src[i++];
                counter = ConversionUtils.toUnsignedByte(flag) & 3; // mod 4
                while (counter-- != 0) // Copy literally
                {
                    dest[j] = src[i++];
                    j++;
                }
                int k = j; // Get the destbuf position
                k -= (ConversionUtils.toUnsignedByte(flag) & 0x60) << 3;
                k -= ConversionUtils.toUnsignedByte(tmp);
                k--;

                counter = ((ConversionUtils.toUnsignedByte(flag) >> 2) & 7) + 2;
                do {
                    dest[j] = dest[k++];
                    j++;
                } while (counter-- != 0); // Correct decrement
            } else if ((ConversionUtils.toUnsignedByte(flag) & 0x40) == 0) {
                byte tmp = src[i++];
                byte tmp2 = src[i++];
                counter = (ConversionUtils.toUnsignedByte(tmp)) >> 6;
                while (counter-- != 0) // Copy literally
                {
                    dest[j] = src[i++];
                    j++;
                }
                int k = j;
                k -= (ConversionUtils.toUnsignedByte(tmp) & 0x3F) << 8;
                k -= ConversionUtils.toUnsignedByte(tmp2);
                k--;
                counter = (ConversionUtils.toUnsignedByte(flag) & 0x3F) + 3;
                do {
                    dest[j] = dest[k++];
                    j++;
                } while (counter-- != 0); // Correct postfix decrement
            } else if ((ConversionUtils.toUnsignedByte(flag) & 0x20) == 0) {
                byte localtemp = src[i++];
                byte tmp2 = src[i++];
                byte tmp3 = src[i++];
                counter = ConversionUtils.toUnsignedByte(flag) & 3;
                while (counter-- != 0) // Copy literally
                {
                    dest[j] = src[i++];
                    j++;
                }
                int k = j;
                k -= (ConversionUtils.toUnsignedByte(flag) & 0x10) << 12;
                k -= ConversionUtils.toUnsignedByte(localtemp) << 8;
                k -= ConversionUtils.toUnsignedByte(tmp2);
                k--;
                counter = ConversionUtils.toUnsignedByte(tmp3) + ((ConversionUtils.toUnsignedByte(flag) & 0x0C) << 6) + 4;
                do {
                    dest[j] = dest[k++];
                    j++;
                } while (counter-- != 0); // Correct
            } else {
                counter = (ConversionUtils.toUnsignedByte(flag) & 0x1F) * 4 + 4;
                if (ConversionUtils.toUnsignedByte(Integer.valueOf(counter).byteValue()) > 0x70) {
                    finished = true;

                    // Prepare to copy the last bytes
                    counter = ConversionUtils.toUnsignedByte(flag) & 3;
                }
                while (counter-- != 0) { // Copy literally
                    dest[j] = src[i++];
                    j++;
                }
            }
        } // Of while()
        if (!finished) {
            LOGGER.log(Level.WARNING, "File {0} might not be successfully extracted!", fileName);
        }
        return dest;
    }
}
