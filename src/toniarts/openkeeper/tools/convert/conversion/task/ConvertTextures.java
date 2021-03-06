/*
 * Copyright (C) 2014-2020 OpenKeeper
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
package toniarts.openkeeper.tools.convert.conversion.task;

import org.jetbrains.annotations.NotNull;
import toniarts.openkeeper.tools.convert.AssetsConverter;
import toniarts.openkeeper.tools.convert.ConversionUtils;
import toniarts.openkeeper.tools.convert.textures.enginetextures.EngineTexturesFile;
import toniarts.openkeeper.tools.convert.textures.loadingscreens.LoadingScreenFile;
import toniarts.openkeeper.tools.convert.wad.WadFile;
import toniarts.openkeeper.utils.AssetUtils;
import toniarts.openkeeper.utils.PathUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dungeon Keeper II textures conversion. Converts textures to PNG.
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class ConvertTextures extends ConversionTask {

    private static final Logger LOGGER = Logger.getLogger(ConvertTextures.class.getName());
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executorService;

    public ConvertTextures(String dungeonKeeperFolder, String destination, boolean overwriteData) {
        super(dungeonKeeperFolder, destination, overwriteData);

        this.executorService = Executors.newFixedThreadPool(MAX_THREADS, new ThreadFactory() {

            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "TexturesConverter_" + threadIndex.incrementAndGet());
            }

        });
    }

    @Override
    public void internalExecuteTask() {
        convertTextures(dungeonKeeperFolder, destination);
    }

    /**
     * Extract and copy DK II textures
     *
     * @param dungeonKeeperFolder DK II main folder
     * @param destination         Destination folder
     */
    private void convertTextures(String dungeonKeeperFolder, String destination) {
        LOGGER.log(Level.INFO, "Extracting textures to: {0}", destination);
        updateStatus(null, null);
        AssetUtils.deleteFolder(new File(destination));
        EngineTexturesFile etFile = getEngineTexturesFile(dungeonKeeperFolder);
        WadFile frontEnd;
        WadFile engineTextures;
        try {
            frontEnd = new WadFile(new File(ConversionUtils.getRealFileName(dungeonKeeperFolder, PathUtils.DKII_DATA_FOLDER + "FrontEnd.WAD")));
            engineTextures = new WadFile(new File(ConversionUtils.getRealFileName(dungeonKeeperFolder, PathUtils.DKII_DATA_FOLDER + "EngineTextures.WAD")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open a WAD file!", e);
        }

        AtomicInteger progress = new AtomicInteger(0);
        int total = etFile.getFileCount() + frontEnd.getWadFileEntries().size() + engineTextures.getWadFileEntries().size();

        // Process each container in its own thread
        executorService.submit(() -> extractEngineTextureContainer(progress, total, etFile, destination));
        executorService.submit(() -> extractTextureContainer(progress, total, frontEnd, destination));
        executorService.submit(() -> extractTextureContainer(progress, total, engineTextures, destination));

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Failed to wait textures conversion complete!", ex);
        }
    }

    /**
     * Loads up an instance of the engine textures catalog
     *
     * @param dungeonKeeperFolder DK II folder
     * @return EngineTextures catalog
     */
    public static EngineTexturesFile getEngineTexturesFile(String dungeonKeeperFolder) {

        // Get the engine textures file
        try {
            return new EngineTexturesFile(new File(ConversionUtils.getRealFileName(dungeonKeeperFolder, "DK2TextureCache".concat(File.separator).concat("EngineTextures.dat"))));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open the EngineTextures file!", e);
        }
    }

    private void extractEngineTextureContainer(AtomicInteger progress, int total, EngineTexturesFile etFile, String destination) throws NumberFormatException {
        Pattern pattern = Pattern.compile("(?<name>\\w+)MM(?<mipmaplevel>\\d)");
        for (String textureFile : etFile) {

            // All are PNG files, and MipMap levels are present, we need only the
            // highest quality one, so don't bother extracting the other mipmap levels
            Matcher matcher = pattern.matcher(textureFile);
            boolean found = matcher.find();
            if (found && Integer.parseInt(matcher.group("mipmaplevel")) == 0) {
                // Highest resolution, extract and rename
                File f = etFile.extractFileData(textureFile, destination, overwriteData);
                File newFile = new File(f.toString().replaceFirst("MM" + matcher.group("mipmaplevel"), ""));
                if (overwriteData && newFile.exists()) {
                    newFile.delete();
                } else if (!overwriteData && newFile.exists()) {

                    // Delete the extracted file
                    LOGGER.log(Level.INFO, "File {0} already exists, skipping!", newFile);
                    f.delete();
                    updateStatus(progress.incrementAndGet(), total);
                    return;
                }
                f.renameTo(newFile);
            } else if (!found) {

                // No mipmap levels, just extract
                etFile.extractFileData(textureFile, destination, overwriteData);
            }
            updateStatus(progress.incrementAndGet(), total);
        }
    }

    /**
     * Extracts the wad files and updates the progress bar
     *
     * @param progress    current entry number
     * @param total       total entry number
     * @param wad         wad file
     * @param destination destination directory
     */
    private void extractTextureContainer(AtomicInteger progress, int total, WadFile wad, String destination) {
        for (final String entry : wad.getWadFileEntries()) {

            // Some of these archives contain .444 files, convert these to PNGs
            if (entry.endsWith(".444")) {
                LoadingScreenFile lsf = new LoadingScreenFile(wad.getFileData(entry));
                try {
                    File destFile = new File(destination + entry);
                    String destFilename = destFile.getCanonicalPath();
                    destFile.getParentFile().mkdirs();
                    ImageIO.write(lsf.getImage(), "png", new File(destFilename.substring(0, destFilename.length() - 3).concat("png")));
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to save the wad entry " + entry + "!", ex);
                }
            } else {
                wad.extractFileData(entry, destination);
            }

            updateStatus(progress.incrementAndGet(), total);
        }
    }

    @Override
    public AssetsConverter.ConvertProcess getConvertProcess() {
        return AssetsConverter.ConvertProcess.TEXTURES;
    }

}
