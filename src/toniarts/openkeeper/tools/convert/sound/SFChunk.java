/*
 * Copyright (C) 2014-2016 OpenKeeper
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

import toniarts.openkeeper.tools.convert.ConversionUtils;
import toniarts.openkeeper.tools.convert.IResourceReader;
import toniarts.openkeeper.tools.convert.IValueEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author ArchDemon
 */
public class SFChunk {

    public enum SFSampleLink implements IValueEnum {
        monoSample(1),
        rightSample(2),
        leftSample(4),
        linkedSample(8),
        RomMonoSample(0x8001),
        RomRightSample(0x8002),
        RomLeftSample(0x8004),
        RomLinkedSample(0x8008);

        @Override
        public int getValue() {
            return value;
        }

        SFSampleLink(int value) {
            this.value = value;
        }

        private final int value;
    }

    public enum Generators implements IValueEnum {
        startAddrsOffset(0),
        endAddrsOffset(1),
        startloopAddrsOffset(2),
        endloopAddrsOffset(3),
        startAddrsCoarseOffset(4),
        modLfoToPitch(5),
        vibLfoToPitch(6),
        modEnvToPitch(7),
        initialFilterFc(8),
        initialFilterQ(9),
        modLfoToFilterFc(10),
        modEnvToFilterFc(11),
        endAddrsCoarseOffset(12),
        modLfoToVolume(13),
        unused1(14),
        chorusEffectsSend(15),
        reverbEffectsSend(16),
        pan(17),
        unused2(18),
        unused3(19),
        unused4(20),
        delayModLFO(21),
        freqModLFO(22),
        delayVibLFO(23),
        freqVibLFO(24),
        delayModEnv(25),
        attackModEnv(26),
        holdModEnv(27),
        decayModEnv(28),
        sustainModEnv(29),
        releaseModEnv(30),
        keynumToModEnvHold(31),
        keynumToModEnvDecay(32),
        delayVolEnv(33),
        attackVolEnv(34),
        holdVolEnv(35),
        decayVolEnv(36),
        sustainVolEnv(37),
        releaseVolEnv(38),
        keynumToVolEnvHold(39),
        keynumToVolEnvDecay(40),
        instrument(41),
        reserved1(42),
        keyRange(43),
        velRange(44),
        startloopAddrsCoarseOffset(45),
        keynum(46),
        velocity(47),
        initialAttenuation(48),
        reserved2(49),
        endloopAddrsCoarseOffset(50),
        coarseTune(51),
        fineTune(52),
        sampleID(53),
        sampleModes(54),
        reserved3(55),
        scaleTuning(56),
        exclusiveClass(57),
        overridingRootKey(58),
        unused5(59),
        endOper(60);

        @Override
        public int getValue() {
            return value;
        }

        Generators(int value) {
            this.value = value;
        }

        private final int value;
    }

    public enum Modulators implements IValueEnum {
        NoController(0),
        NoteOnVelocity(2),
        NoteOnKeyNumber(3),
        PolyPressure(10),
        ChannelPressure(13),
        PitchWheel(14),
        PitchWheelSensitivity(16),
        Link(127);

        @Override
        public int getValue() {
            return value;
        }

        Modulators(int value) {
            this.value = value;
        }

        private final int value;
    }

    public enum ModulatorTypes implements IValueEnum {
        Linear(0),
        Concave(1), // output = log(sqrt(value^2)/(max value)^2)
        Convex(2),
        Switch(3);

        @Override
        public int getValue() {
            return value;
        }

        ModulatorTypes(int value) {
            this.value = value;
        }

        private final int value;
    }

    public enum Transforms implements IValueEnum {
        Linear(0),
        Absolute(2); // output = square root ((input value)^2) or output = output * sgn(output)

        @Override
        public int getValue() {
            return value;
        }

        Transforms(int value) {
            this.value = value;
        }

        private final int value;
    }

    public enum Type {

        RIFF, LIST
    }

    public enum SubType {

        sfbk, INFO, ifil, iver, INAM, isng, irom, IPRD, IENG, ISFT, ICRD, ICMT,
        ICOP, sdta, smpl, pdta, phdr, pbag, pmod, pgen, inst, ibag, imod, igen, shdr
    }

    protected Type type = null;
    protected SubType subType = null;
    protected long size; // in bytes
    protected final List data = new ArrayList<>();

    protected final HashMap<SubType, SFChunk> childs = new HashMap<>();

    public SFChunk(IResourceReader file) throws IOException {
        String code = file.readString(4);
        size = file.readUnsignedIntegerAsLong();
        long pointer = file.getFilePointer();
        //data = new byte[(int)size];
        //file.read(data);
        try {
            type = Type.valueOf(code);
            code = file.readString(4);
            subType = SubType.valueOf(code);

            while (!isEOC(file, pointer)) {
                // this is container for other types
                SFChunk child = new SFChunk(file);
                childs.put(child.getSubType(), child);
            }

            if (file.getFilePointer() != pointer + size) {
                throw new RuntimeException("File pointer out of range. Expect "
                        + (pointer + size) + ", take " + file.getFilePointer());
            }

        } catch (IllegalArgumentException e) {
            try {
                subType = SubType.valueOf(code);
                switch (subType) {
                    case isng: // szSoundEngine : e.g. "EMU8000"
                    case irom: // szROM : e.g. "1MGM"
                    case INAM: // szName : e.g. "General MIDI"
                    case ICRD: // szDate : e.g. "July 15, 1997"
                    case IENG: // szName : e.g. "John Q. Sounddesigner"
                    case IPRD: // szProduct : e.g. "SBAWE64 Gold"
                    case ICOP: // szCopyright : e.g. "Copyright (c) 1997 E-mu Systems, Inc."
                    case ICMT: // szComment : e.g. "This is a comment"
                    case ISFT: // szTools : e.g. ":Preditor 2.00a:Vienna SF Studio 2.0:"
                        //data = new ArrayList<>();
                        while (!isEOC(file, pointer)) {
                            data.add(file.readString((int) size));
                        }
                        break;

                    case smpl:
                        while (!isEOC(file, pointer)) {
                            data.add(file.readRealShort());
                        }
                        //byte d = new byte[(int) size];
                        //file.read((byte[])data);
                        break;

                    case ifil:
                    case iver:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfVersionTag(file));
                        }
                        break;

                    case phdr:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfPresetHeader(file));
                        }
                        break;

                    case pbag:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfPresetBag(file));
                        }
                        break;

                    case pmod:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfModList(file));
                        }
                        break;

                    case pgen:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfGenList(file));
                        }
                        break;

                    case inst:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfInst(file));
                        }
                        break;

                    case ibag:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfInstBag(file));
                        }
                        break;

                    case imod:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfInstModList(file));
                        }
                        break;

                    case igen:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfInstGenList(file));
                        }
                        break;

                    case shdr:
                        while (!isEOC(file, pointer)) {
                            data.add(new sfSample(file));
                        }
                        break;
                }
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Chunk code " + code + " not supported");
            }
        }
    }

    public Type getType() {
        return type;
    }

    public SubType getSubType() {
        return subType;
    }

    public HashMap<SubType, SFChunk> getChilds() {
        return childs;
    }

    /**
     * Check end of chunk
     *
     * @param file file to read
     * @param chunkPointer current chunk start pointer
     * @return true if file pointer >= chunk end pointer
     * @throws IOException
     */
    private boolean isEOC(IResourceReader file, long chunkPointer) throws IOException {
        return file.getFilePointer() >= (size + chunkPointer);
    }

    @Override
    public String toString() {
        String result = "Sf2Chunk{" + "size=" + size;
        if (type != null) {
            result += ", type=" + type;
        }
        if (subType != null) {
            result += ", subType=" + subType;
        }
        if (!data.isEmpty()) {
            result += ", data=" + data;
        }
        if (!childs.isEmpty()) {
            result += ", childs=" + childs;
        }
        result += '}';

        return result;
    }

    protected static class sfVersionTag { // <iver-rec>

        protected int major;
        protected int minor;

        protected sfVersionTag(IResourceReader file) throws IOException {
            major = file.readShort();
            minor = file.readShort();
        }

        @Override
        public String toString() {
            return "sfVersionTag{" + "major=" + major + ", minor=" + minor + '}';
        }
    }

    protected static class sfPresetHeader { // <phdr-rec>

        protected String achPresetName;
        protected int wPreset;
        protected int wBank;
        protected int wPresetBagNdx;
        protected long dwLibrary;
        protected long dwGenre;
        protected long dwMorphology;

        protected sfPresetHeader(IResourceReader file) throws IOException {
            achPresetName = file.readVaryingLengthString(20);
            wPreset = file.readUnsignedShort();
            wBank = file.readUnsignedShort();
            wPresetBagNdx = file.readUnsignedShort();
            dwLibrary = file.readUnsignedIntegerAsLong();
            dwGenre = file.readUnsignedIntegerAsLong();
            dwMorphology = file.readUnsignedIntegerAsLong();
        }

        @Override
        public String toString() {
            return "sfPresetHeader{" + "achPresetName=" + achPresetName
                    + ", wPreset=" + wPreset + ", wBank=" + wBank
                    + ", wPresetBagNdx=" + wPresetBagNdx + ", dwLibrary=" + dwLibrary
                    + ", dwGenre=" + dwGenre + ", dwMorphology=" + dwMorphology + '}';
        }
    }

    protected static class sfPresetBag { // <pbag-rec>

        protected int wGenNdx;
        protected int wModNdx;

        protected sfPresetBag(IResourceReader file) throws IOException {
            wGenNdx = file.readUnsignedShort();
            wModNdx = file.readUnsignedShort();
        }

        @Override
        public String toString() {
            return "sfPresetBag{" + "wGenNdx=" + wGenNdx + ", wModNdx=" + wModNdx + '}';
        }
    }

    protected static class sfInst { // <inst-rec>

        protected String achInstName;
        protected int wInstBagNdx;

        protected sfInst(IResourceReader file) throws IOException {
            achInstName = file.readVaryingLengthString(20);
            wInstBagNdx = file.readUnsignedShort();
        }

        @Override
        public String toString() {
            return "sfInst{" + "achInstName=" + achInstName + ", wInstBagNdx=" + wInstBagNdx + '}';
        }
    }

    protected static class sfInstBag { // <ibag-rec>

        protected int wInstGenNdx;
        protected int wInstModNdx;

        protected sfInstBag(IResourceReader file) throws IOException {
            wInstGenNdx = file.readUnsignedShort();
            wInstModNdx = file.readUnsignedShort();
        }

        @Override
        public String toString() {
            return "sfInstBag{" + "wInstGenNdx=" + wInstGenNdx + ", wInstModNdx=" + wInstModNdx + '}';
        }
    }

    protected static class sfSample { // <shdr-rec>

        protected String achSampleName;
        protected long dwStart;
        protected long dwEnd;
        protected long dwStartloop;
        protected long dwEndloop;
        protected long dwSampleRate;
        protected short byOriginalKey;
        protected byte chCorrection; // CHAR
        protected int wSampleLink;
        protected SFSampleLink sfSampleType;

        protected sfSample(IResourceReader file) throws IOException {
            achSampleName = file.readVaryingLengthString(20);
            dwStart = file.readUnsignedIntegerAsLong();
            dwEnd = file.readUnsignedIntegerAsLong();
            dwStartloop = file.readUnsignedIntegerAsLong();
            dwEndloop = file.readUnsignedIntegerAsLong();
            dwSampleRate = file.readUnsignedIntegerAsLong();
            byOriginalKey = file.readUnsignedByte();
            chCorrection = file.readByte();
            wSampleLink = file.readUnsignedShort();
            sfSampleType = file.readShortAsEnum(SFSampleLink.class);
        }

        @Override
        public String toString() {
            return "sfSample{" + "achSampleName=" + achSampleName + ", dwStart=" + dwStart
                    + ", dwEnd=" + dwEnd + ", dwStartloop=" + dwStartloop
                    + ", dwEndloop=" + dwEndloop + ", dwSampleRate=" + dwSampleRate
                    + ", byOriginalKey=" + byOriginalKey + ", chCorrection=" + chCorrection
                    + ", wSampleLink=" + wSampleLink + ", sfSampleType=" + sfSampleType + '}';
        }
    }

    protected static class rangesType {

        protected short byLo;
        protected short byHi;

        protected rangesType(IResourceReader file) throws IOException {
            byLo = file.readUnsignedByte();
            byHi = file.readUnsignedByte();
        }
    }

    protected static class genAmountType {

        protected rangesType ranges;
        protected short shAmount;
        protected int wAmount;

        protected genAmountType(IResourceReader file) throws IOException {
            ranges = new rangesType(file);
            shAmount = file.readRealShort();
            wAmount = file.readUnsignedShort();
        }
    }

    protected static class SFModulator {

        protected Modulators bIndex = null; // A 7 bit value specifying the controller source
        protected boolean cc; // MIDI Continuous Controller Flag
        protected boolean d; // Direction
        protected boolean p; // Polarity
        protected ModulatorTypes bType = null; // A 6 bit value specifying the continuity of the controller

        protected SFModulator(IResourceReader file) throws IOException {
            int bits = file.readUnsignedShort();

            bIndex = ConversionUtils.parseEnum(ConversionUtils.bits(bits, 0, 7), Modulators.class);
            cc = ConversionUtils.bits(bits, 7, 1) == 1;
            d = ConversionUtils.bits(bits, 8, 1) == 1;
            p = ConversionUtils.bits(bits, 9, 1) == 1;
            bType = ConversionUtils.parseEnum(ConversionUtils.bits(bits, 10, 6), ModulatorTypes.class);
        }
    }

    protected static class SFGenerator {

        protected Generators bIndex = null;
        protected boolean cc;
        protected boolean d;
        protected boolean p;
        protected byte bType;

        protected SFGenerator(IResourceReader file) throws IOException {
            int bits = file.readUnsignedShort();

            bIndex = ConversionUtils.parseEnum(ConversionUtils.bits(bits, 0, 7), Generators.class);
            cc = ConversionUtils.bits(bits, 7, 1) == 1;
            d = ConversionUtils.bits(bits, 8, 1) == 1;
            p = ConversionUtils.bits(bits, 9, 1) == 1;
            bType = (byte) ConversionUtils.bits(bits, 10, 6);
        }
    }

    protected static class SFTransform {

        protected Transforms bIndex = null;
        protected boolean cc;
        protected boolean d;
        protected boolean p;
        protected byte bType;

        protected SFTransform(IResourceReader file) throws IOException {
            int bits = file.readUnsignedShort();

            bIndex = ConversionUtils.parseEnum(ConversionUtils.bits(bits, 0, 7), Transforms.class);
            cc = ConversionUtils.bits(bits, 7, 1) == 1;
            d = ConversionUtils.bits(bits, 8, 1) == 1;
            p = ConversionUtils.bits(bits, 9, 1) == 1;
            bType = (byte) ConversionUtils.bits(bits, 10, 6);
        }
    }

    protected static class sfModList { // <pmod-rec>

        protected SFModulator sfModSrcOper;
        protected SFGenerator sfModDestOper;
        short modAmount;
        protected SFModulator sfModAmtSrcOper;
        protected SFTransform sfModTransOper;

        protected sfModList(IResourceReader file) throws IOException {
            sfModSrcOper = new SFModulator(file);
            sfModDestOper = new SFGenerator(file);
            modAmount = file.readRealShort();
            sfModAmtSrcOper = new SFModulator(file);
            sfModTransOper = new SFTransform(file);
        }

        @Override
        public String toString() {
            return "sfModList{" + "sfModSrcOper=" + sfModSrcOper
                    + ", sfModDestOper=" + sfModDestOper + ", modAmount=" + modAmount
                    + ", sfModAmtSrcOper=" + sfModAmtSrcOper + ", sfModTransOper=" + sfModTransOper + '}';
        }
    }

    protected class sfGenList { // <pgen-rec>

        protected SFGenerator sfGenOper;
        protected genAmountType genAmount;

        protected sfGenList(IResourceReader file) throws IOException {
            sfGenOper = new SFGenerator(file);
            genAmount = new genAmountType(file);
        }

        @Override
        public String toString() {
            return "sfGenList{" + "sfGenOper=" + sfGenOper + ", genAmount=" + genAmount + '}';
        }
    }

    protected static class sfInstModList { // <imod-rec>

        protected SFModulator sfModSrcOper;
        protected SFGenerator sfModDestOper;
        protected short modAmount;
        protected SFModulator sfModAmtSrcOper;
        protected SFTransform sfModTransOper;

        protected sfInstModList(IResourceReader file) throws IOException {
            sfModSrcOper = new SFModulator(file);
            sfModDestOper = new SFGenerator(file);
            modAmount = file.readRealShort();
            sfModAmtSrcOper = new SFModulator(file);
            sfModTransOper = new SFTransform(file);
        }

        @Override
        public String toString() {
            return "sfInstModList{" + "sfModSrcOper=" + sfModSrcOper
                    + ", sfModDestOper=" + sfModDestOper + ", modAmount=" + modAmount
                    + ", sfModAmtSrcOper=" + sfModAmtSrcOper + ", sfModTransOper=" + sfModTransOper + '}';
        }
    }

    protected class sfInstGenList { // <igen-rec>

        protected SFGenerator sfGenOper;
        protected genAmountType genAmount;

        protected sfInstGenList(IResourceReader file) throws IOException {
            sfGenOper = new SFGenerator(file);
            genAmount = new genAmountType(file);
        }

        @Override
        public String toString() {
            return "sfInstGenList{" + "sfGenOper=" + sfGenOper + ", genAmount=" + genAmount + '}';
        }
    }
}
