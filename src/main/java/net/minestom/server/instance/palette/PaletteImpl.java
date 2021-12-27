package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class PaletteImpl implements Palette, Cloneable {
    private static final int[] MAGIC_MASKS;
    private static final int[] VALUES_PER_LONG;

    static {
        final int entries = 16;
        MAGIC_MASKS = new int[entries];
        VALUES_PER_LONG = new int[entries];
        for (int i = 1; i < entries; i++) {
            MAGIC_MASKS[i] = Integer.MAX_VALUE >> (31 - i);
            VALUES_PER_LONG[i] = Long.SIZE / i;
        }
    }

    private static final AtomicReferenceFieldUpdater<PaletteImpl, Content> CONTENT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(PaletteImpl.class, Content.class, "content");
    private static final VarHandle INT_ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(int[].class);

    // Specific to this palette type
    private final int dimension;
    private final int maxBitsPerEntry;
    private final int defaultBitsPerEntry;
    private final int bitsIncrement;
    private volatile Content content;

    private final class Content {
        final long[] values;
        final int bitsPerEntry;
        final boolean hasPalette;

        int lastPaletteIndex = 1; // First index is air
        // palette index = value
        final IntArrayList paletteToValueList;
        // value = palette index
        final Int2IntOpenHashMap valueToPaletteMap;

        final AtomicInteger count = new AtomicInteger();

        Content(int bitsPerEntry) {
            this.values = new long[valueLength(dimension, bitsPerEntry)];
            this.bitsPerEntry = bitsPerEntry;
            this.hasPalette = bitsPerEntry <= maxBitsPerEntry;
            this.paletteToValueList = new IntArrayList(1);
            this.paletteToValueList.add(0);
            this.valueToPaletteMap = new Int2IntOpenHashMap(1);
            this.valueToPaletteMap.put(0, 0);
        }
    }

    PaletteImpl(int dimension, int maxBitsPerEntry, int bitsPerEntry, int bitsIncrement) {
        if (dimension < 1 || dimension % 2 != 0)
            throw new IllegalArgumentException("Dimension must be positive and power of 2");
        this.dimension = dimension;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.defaultBitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;
    }

    @Override
    public int get(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        final Content content = this.content;
        if (content == null) {
            // Section is not loaded, return default value
            return 0;
        }
        final long[] values = content.values;
        final int bitsPerEntry = content.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        final int sectionIdentifier = getSectionIndex(x, y, z);
        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;

        final short value = (short) (values[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry]);

        if (content.hasPalette) {
            final int[] elements = content.paletteToValueList.elements();
            return (int) INT_ARRAY_HANDLE.getVolatile(elements, value);
        } else {
            return value;
        }
    }

    @Override
    public synchronized void set(int x, int y, int z, int value) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        final boolean placedAir = value == 0;

        Content content = this.content;
        if (content == null) {
            if (placedAir) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }
            CONTENT_UPDATER.compareAndSet(this, null, new Content(defaultBitsPerEntry));
            set(x, y, z, value);
            return;
        }

        // Change to palette value
        int bpe = content.bitsPerEntry;
        if (content.lastPaletteIndex >= maxPaletteSize(bpe)) {
            // Palette is full, must resize
            content = resize(bpe + bitsIncrement);
        }
        value = getPaletteIndex(content, value);

        long[] values = content.values;
        bpe = content.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bpe];

        final int sectionIndex = getSectionIndex(x, y, z);
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex % valuesPerLong) * bpe;

        long block = values[index];
        {
            final long clear = MAGIC_MASKS[bpe];

            final long oldBlock = block >> bitIndex & clear;
            if (oldBlock == value)
                return; // Trying to place the same block
            final boolean currentAir = oldBlock == 0;

            final long indexClear = clear << bitIndex;
            block |= indexClear;
            block ^= indexClear;
            block |= (long) value << bitIndex;

            if (currentAir != placedAir) {
                // Block count changed
                content.count.addAndGet(currentAir ? 1 : -1);
            }
            values[index] = block;
        }
    }

    @Override
    public int size() {
        var content = this.content;
        return content == null ? 0 : content.count.get();
    }

    @Override
    public int bitsPerEntry() {
        var content = this.content;
        return content == null ? defaultBitsPerEntry : content.bitsPerEntry;
    }

    @Override
    public int maxBitsPerEntry() {
        return maxBitsPerEntry;
    }

    @Override
    public int maxSize() {
        return dimension * dimension * dimension;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public long[] data() {
        var content = this.content;
        return content == null ? new long[0] : content.values;
    }

    @Override
    public @NotNull Palette clone() {
        try {
            PaletteImpl palette = (PaletteImpl) super.clone();
            //palette.values = values.clone();
            //palette.paletteToValueList = paletteToValueList.clone();
            //palette.valueToPaletteMap = valueToPaletteMap.clone();
            //palette.count = count;
            return palette;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        final Content content = this.content;
        if (content == null) {
            writer.writeByte((byte) defaultBitsPerEntry);
            {
                // Register air
                writer.writeVarInt(1);
                writer.writeVarInt(0);
            }
            // No block
            writer.writeVarInt(0);
            return;
        }
        var bpe = content.bitsPerEntry;
        writer.writeByte((byte) bpe);
        // Palette
        if (bpe < 9) {
            // Palette has to exist
            writer.writeVarIntList(content.paletteToValueList, BinaryWriter::writeVarInt);
        }
        // Raw
        writer.writeLongArray(content.values);
    }

    private int fixBitsPerEntry(int bitsPerEntry) {
        return bitsPerEntry > maxBitsPerEntry ? 15 : bitsPerEntry;
    }

    private Content resize(int newBitsPerEntry) {
        newBitsPerEntry = fixBitsPerEntry(newBitsPerEntry);
        PaletteImpl palette = new PaletteImpl(dimension, maxBitsPerEntry, newBitsPerEntry, bitsIncrement);
        for (int y = 0; y < dimension; y++) {
            for (int x = 0; x < dimension; x++) {
                for (int z = 0; z < dimension; z++) {
                    palette.set(x, y, z, get(x, y, z));
                }
            }
        }
        return (this.content = palette.content);
    }

    private int getPaletteIndex(Content content, int value) {
        if (!content.hasPalette) return value;
        final int lookup = content.valueToPaletteMap.getOrDefault(value, (short) -1);
        if (lookup != -1) return lookup;
        final int paletteIndex = content.lastPaletteIndex++;
        content.paletteToValueList.add(value);
        content.valueToPaletteMap.put(value, paletteIndex);
        return paletteIndex;
    }

    int getSectionIndex(int x, int y, int z) {
        x %= dimension;
        y %= dimension;
        z %= dimension;
        return y << (dimension / 2) | z << (dimension / 4) | x;
    }

    static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
    }

    static int valueLength(int dimension, int bpe) {
        int size = dimension * dimension * dimension;
        var valuesPerLong = VALUES_PER_LONG[bpe];
        return (size + valuesPerLong - 1) / valuesPerLong;
    }
}
