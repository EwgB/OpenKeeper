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
package toniarts.openkeeper.tools.convert;

import com.jme3.math.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * Contains static helper methods
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class ConversionUtils {

    private static final Logger LOGGER = Logger.getLogger(ConversionUtils.class.getName());
    private static final Map<String, String> FILENAME_CACHE = new HashMap<>();
    private static final PathTree PATH_CACHE = new PathTree();
    private static final Object FILENAME_LOCK = new Object();
    private static final String QUOTED_FILE_SEPARATOR = Matcher.quoteReplacement(File.separator);

    public static final float FLOAT = 4096f; // or DIVIDER_FLOAT Fixed Point Single Precision Divider
    public static final float DOUBLE = 65536f; // or DIVIDER_DOUBLE Fixed Point Double Precision Divider

    /**
     * Converts 4 bytes to JAVA int from LITTLE ENDIAN unsigned int presented by
     * a byte array
     *
     * @param unsignedInt the byte array
     * @return JAVA native int
     * @see toniarts.openkeeper.tools.convert.IResourceReader#readUnsignedIntegerAsLong()
     */
    public static int toUnsignedInteger(byte[] unsignedInt) {
        int result = toInteger(unsignedInt);
        if (result < 0) {

            // Yes, this should be long, however, in our purpose this might be sufficient as int
            // Safety measure
            LOGGER.warning("This unsigned integer doesn't fit to JAVA integer! Use a different method!");
        }
        return result;
    }

    /**
     * Converts 4 bytes to JAVA int from LITTLE ENDIAN int presented by a byte
     * array
     *
     * @param signedInt the byte array
     * @return JAVA native int
     */
    public static int toInteger(byte[] signedInt) {
        ByteBuffer buffer = ByteBuffer.wrap(signedInt);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
    }

    /**
     * Converts 2 bytes to JAVA short from LITTLE ENDIAN unsigned short
     * presented by a byte array (needs to be int in JAVA)
     *
     * @param unsignedShort the byte array
     * @return JAVA native int
     */
    public static int toUnsignedShort(byte[] unsignedShort) {
        return toShort(unsignedShort) & 0xFFFF;
    }

    /**
     * Converts 2 bytes to JAVA short from LITTLE ENDIAN signed short presented
     * by a byte array
     *
     * @param signedShort the byte array
     * @return JAVA native short
     */
    public static short toShort(byte[] signedShort) {
        ByteBuffer buffer = ByteBuffer.wrap(signedShort);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getShort();
    }

    /**
     * Converts 4 bytes to JAVA float from LITTLE ENDIAN float presented by a
     * byte array
     *
     * @param f the byte array
     * @return JAVA native float
     */
    public static float toFloat(byte[] f) {
        ByteBuffer buffer = ByteBuffer.wrap(f);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getFloat();
    }

    /**
     * Converts a byte array to a JAVA String
     *
     * @param bytes the bytearray to convert
     * @see toniarts.openkeeper.tools.convert.IResourceReader#readString(int)
     * @return fresh String
     */
    public static String toString(byte[] bytes) {
        return new String(bytes, Charset.forName("windows-1252"));
    }

    /**
     * Converts a byte array to a JAVA String<br>
     * The byte array string is assumed UTF16 (wide strings in C), LITTLE ENDIAN
     *
     * @param bytes the bytearray to convert
     * @return fresh String
     */
    public static String toStringUtf16(byte[] bytes) {
        return new String(bytes, Charset.forName("UTF_16LE"));
    }

    /**
     * Converts JAVAX 3f vector to JME vector (also converts the coordinate
     * system)
     *
     * @param v vector
     * @return JME vector
     */
    public static Vector3f convertVector(javax.vecmath.Vector3f v) {
        return new Vector3f(v.x, -v.z, v.y);
    }

    /**
     * Convert a byte to unsigned byte
     *
     * @param b byte
     * @return unsigned byte (needs to be short in JAVA)
     */
    public static short toUnsignedByte(byte b) {
        return Integer.valueOf(b & 0xFF).shortValue();
    }

    /**
     * Converts a list of bytes to an array of bytes
     *
     * @param bytes the list of bytes
     * @return the byte array
     */
    public static byte[] toByteArray(List<Byte> bytes) {
        byte[] byteArray = new byte[bytes.size()];
        int i = 0;
        for (Byte b : bytes) {
            byteArray[i] = b;
            i++;
        }
        return byteArray;
    }

    /**
     * Bit play<br>
     * http://stackoverflow.com/questions/11419501/converting-bits-in-to-integer
     *
     * @param n bytes converted to int
     * @param offset from where to read (bits index)
     * @param length how many bits to read
     * @return integer represented by the bits
     */
    public static int bits(int n, int offset, int length) {

        //Shift the bits rightward, so that the desired chunk is at the right end
        n = n >> (31 - offset - length);

        //Prepare a mask where only the rightmost `length`  bits are 1's
        int mask = ~(-1 << length);

        //Zero out all bits but the right chunk
        return n & mask;
    }

    /**
     * Strip file name clean from any illegal characters, replaces the illegal
     * characters with an underscore
     *
     * @param fileName the file name to be stripped
     * @return returns stripped down file name
     */
    public static String stripFileName(String fileName) {
        return fileName.replaceAll("[[^a-zA-Z0-9][\\.]]", "_");
    }

    /**
     * Returns case sensitive and valid asset key for loading the given asset
     *
     * @param asset the asset key, i.e. Textures\GUI/wrongCase.png
     * @return fully qualified and working asset key
     */
    public static String getCanonicalAssetKey(String asset) {
        return getCanonicalRelativePath(AssetsConverter.getAssetsFolder(), asset).replaceAll(QUOTED_FILE_SEPARATOR, "/");
    }

    /**
     * Returns case sensitive and valid relative path
     *
     * @param rootPath the working start path, used to relativize the path
     * @param path the unknown path to fix
     * @return fully qualified and working relative path
     */
    public static String getCanonicalRelativePath(String rootPath, String path) {
        try {
            return getRealFileName(rootPath, path).substring(rootPath.length());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not locate path " + path + " from " + rootPath + "!", e);
            return path;
        }
    }

    /**
     * Converts all the file separators to current system separators
     *
     * @param fileName the file name to convert
     * @return the file name with native file separators
     */
    public static String convertFileSeparators(String fileName) {
        return fileName.replaceAll("[/\\\\]", QUOTED_FILE_SEPARATOR);
    }

    /**
     * Gets real file name for a file, this is to ignore file system case
     * sensitivity<br>
     * Does a recursive search
     *
     * @param realPath the real path that surely exists (<strong>case
     * sensitive!!</strong>), serves as a root for the searching
     * @param uncertainPath the file (and/or directory) to find from the real
     * path
     * @return the case sensitive fully working file name
     * @throws IOException if file is not found
     */
    public static String getRealFileName(final String realPath, String uncertainPath) throws IOException {

        // Make sure that the uncertain path's separators are system separators
        uncertainPath = convertFileSeparators(uncertainPath);

        String fileName = realPath.concat(uncertainPath);
        String fileKey = fileName.toLowerCase();

        // See cache
        String cachedName = FILENAME_CACHE.get(fileKey);
        if (cachedName == null) {
            synchronized (FILENAME_LOCK) {

                // If it exists as such, that is super!
                Path testFile = Paths.get(fileName);
                if (Files.exists(testFile)) {
                    cachedName = testFile.toRealPath().toString();
                    FILENAME_CACHE.put(fileKey, cachedName);
                } else {

                    // Otherwise we need to do a recursive search
                    String certainPath = PATH_CACHE.getCertainPath(fileName, realPath);
                    final String[] path = fileName.substring(certainPath.length()).split(QUOTED_FILE_SEPARATOR);

                    // If the path length is 1, lets try, maybe it was just the file name
                    if (path.length == 1 && !certainPath.equalsIgnoreCase(realPath)) {
                        Path p = Paths.get(certainPath, path[0]);
                        if (Files.exists(p)) {
                            cachedName = p.toRealPath().toString();
                            FILENAME_CACHE.put(fileKey, cachedName);
                            return cachedName;
                        }
                    }

                    // Find the file
                    final Path realPathAsPath = Paths.get(certainPath);
                    FileFinder fileFinder = new FileFinder(realPathAsPath, path);
                    Files.walkFileTree(realPathAsPath, fileFinder);
                    FILENAME_CACHE.put(fileKey, fileFinder.file);
                    cachedName = fileFinder.file;
                    if (fileFinder.file == null) {
                        throw new IOException("File not found " + testFile + "!");
                    }

                    // Cache the known path
                    PATH_CACHE.setPathToCache(fileFinder.file);
                }
            }
        }
        return cachedName;
    }

    /**
     * Parse a flag to enumeration set of given class
     *
     * @param flag the flag value
     * @param <E> enumeration class
     * @param enumeration the enumeration class
     * @return the set
     */
    public static <E extends Enum<E> & IFlagEnum> EnumSet<E> parseFlagValue(long flag, Class<E> enumeration) {
        long leftOver = flag;
        EnumSet<E> set = EnumSet.noneOf(enumeration);
        for (E e : enumeration.getEnumConstants()) {
            long flagValue = e.getFlagValue();
            if ((flagValue & flag) == flagValue) {
                set.add(e);
                leftOver -= flagValue;
            }
        }
        if (leftOver > 0) {

            // Check the values not defined (there must be a better way to do this but me and numbers...)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 64; i++) {
                long val = (long) Math.pow(2, i);
                if (val > leftOver) {
                    break;
                } else if ((val & leftOver) == val) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(val);
                }
            }
            LOGGER.log(Level.WARNING, "Value(s) {0} not specified for enum set class {1}!", new java.lang.Object[]{sb.toString(), enumeration.getName()});
        }
        return set;
    }

    /**
     * Parses a value to a enum of a wanted enum class
     *
     * @param <E> The enumeration class
     * @param value the id value
     * @param enumeration the enumeration class
     * @return Enum value, returns null if no enum is found with given value
     */
    public static <E extends Enum & IValueEnum> E parseEnum(int value, Class<E> enumeration) {
        for (E e : enumeration.getEnumConstants()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        LOGGER.log(Level.WARNING, "Value {0} not specified for enum class {1}!", new java.lang.Object[]{value, enumeration.getName()});
        return null;

    }

    /**
     * File finder, recursively tries to find a file ignoring case
     */
    private static class FileFinder extends SimpleFileVisitor<Path> {

        private int level = 0;
        private String file;
        private final Path startingPath;
        private final String[] path;

        private FileFinder(Path startingPath, String[] path) {
            this.startingPath = startingPath;
            this.path = path;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (startingPath.equals(dir)) {
                return FileVisitResult.CONTINUE; // Just the root
            } else if (startingPath.relativize(dir).getName(level).toString().equalsIgnoreCase(path[level])) {
                if (level < path.length - 1) {
                    level++;
                    return FileVisitResult.CONTINUE; // Go to dir
                } else {

                    // We are looking for a directory and we found it
                    this.file = dir.toRealPath().toString().concat(File.separator);
                    return FileVisitResult.TERMINATE;
                }
            }
            return FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

            // See if this is the file we are looking for
            if (level == path.length - 1 && file.getName(file.getNameCount() - 1).toString().equalsIgnoreCase(path[level])) {
                this.file = file.toRealPath().toString();
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.TERMINATE; // We already missed our window here
        }
    }

    /**
     * Represents a simple path tree cache, with unlimited number of roots.
     * Offers some methods to manage the tree.
     */
    private static class PathTree extends HashMap<String, PathNode> {

        /**
         * Add the path to cache from KNOWN file
         *
         * @param file the known and existing file
         */
        public void setPathToCache(String file) {
            List<String> paths = new ArrayList<>(Arrays.asList(file.split(QUOTED_FILE_SEPARATOR)));
            if (!paths.isEmpty()) {
                if (!file.endsWith(File.separator)) {
                    paths.remove(paths.size() - 1);
                }
                PathNode node = null;
                for (String folder : paths) {
                    node = getPath(folder, node, true);
                }
            }
        }

        private PathNode getPath(String folder, PathNode node, boolean add) {
            String key = folder.toLowerCase();
            Map<String, PathNode> leaf;
            if (node != null) {
                leaf = node.children;
            } else {
                leaf = this;
            }
            PathNode result = leaf.get(key);
            if (result == null && add) {
                result = new PathNode(folder, (node != null ? node.level + 1 : 0), node);
                leaf.put(key, result);
            }
            return result;
        }

        /**
         * Get certain path from cache
         *
         * @param fileName the file name we aim to find, if folder, we expect
         * path separator at the end
         * @param defaultPath the default path we know that exists, we'll return
         * it if no cached path found
         * @return the cached known path, quaranteed to be exactly the default
         * path or deeper
         */
        public String getCertainPath(String fileName, String defaultPath) {
            List<String> paths = new ArrayList<>(Arrays.asList(fileName.split(QUOTED_FILE_SEPARATOR)));
            if (!paths.isEmpty()) {
                if (!fileName.endsWith(File.separator)) {
                    paths.remove(paths.size() - 1);
                }
                PathNode node = null;
                for (String folder : paths) {
                    PathNode nextNode = getPath(folder, node, false);
                    if (nextNode != null) {
                        node = nextNode;
                    } else {
                        break;
                    }
                }

                // Return if we have longer path
                if (node != null && node.path.length() > defaultPath.length()) {
                    return node.path;
                }
            }
            return defaultPath;
        }

    }

    /**
     * Path node that represents a single folder
     */
    private static class PathNode {

        private final String path;
        private final String name;
        private final int level;
        private final PathNode parent;
        private final Map<String, PathNode> children = new HashMap<>();

        public PathNode(String name, int level, PathNode parent) {
            this.name = name;
            this.level = level;
            this.parent = parent;

            StringBuilder sb = new StringBuilder();
            if (parent != null) {
                sb.append(parent.path);
            }
            sb.append(name);
            sb.append(File.separator);
            path = sb.toString();
        }

        public String getName() {
            return name;
        }

        public int getLevel() {
            return level;
        }

        public PathNode getParent() {
            return parent;
        }

        public Map<String, PathNode> getChildren() {
            return children;
        }

        public String getPath() {
            return path;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.name);
            hash = 67 * hash + this.level;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PathNode other = (PathNode) obj;
            if (this.level != other.level) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }

    }
}
