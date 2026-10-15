package dev.vercim.handycam.rig;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Reads the recorded camera output: per config variant, per frame, {@link #FIELDS} floats. The fixture was captured
 * from the layer stack as it was before the rig refactor (commit 7ff43cf, test {@code GoldenCaptureTest}) and is
 * the contract the refactored rig has to keep.
 */
final class Golden {

    static final String RESOURCE = "/dev/vercim/handycam/rig/camera-golden.bin.gz";
    static final String[] FIELDS = {
        "pitch", "yaw", "roll", "y",
        "crosshairOffsetX", "crosshairOffsetY", "drawCompX", "drawCompY", "eatCompX", "eatCompY", "bowDrawProgress"
    };

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
