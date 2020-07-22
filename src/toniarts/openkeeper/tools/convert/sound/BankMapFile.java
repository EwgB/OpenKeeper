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
package toniarts.openkeeper.tools.convert.sound;

import toniarts.openkeeper.tools.convert.IResourceReader;
import toniarts.openkeeper.tools.convert.ResourceReader;

import java.io.File;
import java.io.IOException;

/**
 * Dungeon Keeper II *Bank.map files. The map files contain sound playback events of some sorts<br>
 * The file is LITTLE ENDIAN I might say<br>
 * File structure specifications by Tomasz Lis
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class BankMapFile {

    private final static int[] HEADER_ID = new int[]{
            0xE9612C01, // dword_674048
            0x11D231D0, // dword_67404C
            0xA00009B4, // dword_674050
            0x03F293C9 // dword_674054
    };
    // Header
    private final int unknown1; // 0 or 1 // not used
    private final long unknown2; // 0, 32769, 0xFFFFFFFF // not used
    //
    private final File file;
    private final BankMapFileEntry[] entries;

    /**
     * Reads the *Bank.map file structure
     *
     * @param file the *Bank.map file to read
     */
    public BankMapFile(File file) {
        this.file = file;

        //Read the file
        try (IResourceReader rawMap = new ResourceReader(file)) {
            //Header
            int[] check = new int[]{
                rawMap.readInteger(),
                rawMap.readInteger(),
                rawMap.readInteger(),
                rawMap.readInteger()
            };
            for (int i = 0; i < HEADER_ID.length; i++) {
                if (check[i] != HEADER_ID[i]) {
                    throw new RuntimeException(file.getName() + ": The file header is not valid");
                }
            }

            unknown1 = rawMap.readUnsignedInteger();
            unknown2 = rawMap.readUnsignedIntegerAsLong();
            int count = rawMap.readUnsignedInteger();

            //Read the entries
            entries = new BankMapFileEntry[count];
            for (int i = 0; i < entries.length; i++) {

                //Entries are 11 bytes of size
                BankMapFileEntry entry = new BankMapFileEntry();
                entry.setUnknown1(rawMap.readUnsignedIntegerAsLong());
                entry.setUnknown2(rawMap.readInteger());
                entry.setUnknown3(rawMap.readUnsignedShort());
                entry.setUnknown4(rawMap.readUnsignedByte());

                entries[i] = entry;
            }

            // After the entries there are names (that point to the SDT archives it seems)
            // It seems the amount is the same as entries
            for (BankMapFileEntry entry : entries) {
                // 4 bytes = length of the name (including the null terminator)
                int length = rawMap.readUnsignedInteger();
                entry.setName(rawMap.readString(length).trim());
            }

        } catch (IOException e) {

            //Fug
            throw new RuntimeException("Failed to open the file " + file + "!", e);
        }
    }

    public BankMapFileEntry[] getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
