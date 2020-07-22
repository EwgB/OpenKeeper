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
package toniarts.openkeeper.tools.convert.hiscores;

import toniarts.openkeeper.tools.convert.IResourceReader;
import toniarts.openkeeper.tools.convert.ResourceReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the HiScores file entries<br>
 * Actual format reverse engineered by ArchDemon
 *
 * @author ArchDemon
 */
public class HiScoresFile {

    private final List<HiScoresEntry> hiScoresEntries;

    /**
     * Constructs a new HiScores file reader<br>
     * Reads the HiScores.dat file
     *
     * @param file the HiScores file to read
     */
    public HiScoresFile(File file) {

        //Read the file
        try (IResourceReader data = new ResourceReader(file)) {

            //Read the entries, no header, just entries till the end
            hiScoresEntries = new ArrayList<>();
            while (data.getFilePointer() < data.length()) {
                HiScoresEntry entry = new HiScoresEntry();
                entry.setScore(data.readUnsignedInteger());
                entry.setName(data.readVaryingLengthStringUtf16(32).trim());
                entry.setLevel(data.readVaryingLengthStringUtf16(32).trim());

                hiScoresEntries.add(entry);
            }
        } catch (IOException e) {

            //Fug
            throw new RuntimeException("Failed to open the file " + file + "!", e);
        }
    }

    public List<HiScoresEntry> getHiScoresEntries() {
        return hiScoresEntries;
    }
}
