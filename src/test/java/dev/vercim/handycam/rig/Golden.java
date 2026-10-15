package dev.vercim.handycam.rig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Reads and writes the recorded camera output: per variant, per frame, {@link #FIELDS} floats. */
final class Golden {

    static final String RESOURCE = "/dev/vercim/handycam/rig/camera-golden.bin.gz";
    static final String[] FIELDS = {
        "pitch", "yaw", "roll", "y",
        "crosshairOffsetX", "crosshairOffsetY", "drawCompX", "drawCompY", "eatCompX", "eatCompY", "bowDrawProgress"
    };

    static void write(Path file, List<float[][]> variants) throws IOException {
        Files.createDirectories(file.getParent());
        try (OutputStream os = Files.newOutputStream(file);
             DataOutputStream out = new DataOutputStream(new GZIPOutputStream(os))) {
            out.writeInt(variants.size());
            for (float[][] frames : variants) {
                out.writeInt(frames.length);
                for (float[] frame : frames) {
                    for (float v : frame) out.writeFloat(v);
                }
            }
        }
    }

    static List<float[][]> read() throws IOException {
        try (InputStream is = Golden.class.getResourceAsStream(RESOURCE)) {
            if (is == null) throw new IOException("Missing golden resource " + RESOURCE);
            DataInputStream in = new DataInputStream(new GZIPInputStream(is));
            int variants = in.readInt();
            List<float[][]> result = new ArrayList<>();
            for (int v = 0; v < variants; v++) {
                float[][] frames = new float[in.readInt()][FIELDS.length];
                for (float[] frame : frames) {
                    for (int i = 0; i < frame.length; i++) frame[i] = in.readFloat();
                }
                result.add(frames);
            }
            return result;
        }
    }

    private Golden() {}
}
