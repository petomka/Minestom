package concurrent;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.palette.Palette;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaletteConcurrentTest {

    @Test
    public void concurrentPalette() throws InterruptedException {
        final int numThreads = 5;
        final int iterations = 6_000;
        final int dimension = 16;
        Palette palette = Palette.newPalette(dimension, 8, 6, 1);

        Map<Point, Integer> map = new ConcurrentHashMap<>();
        class PaletteThread extends Thread {
            @Override
            public void run() {
                var random = new java.util.Random();
                for (int i = 0; i < iterations; i++) {
                    int x = random.nextInt(dimension);
                    int y = random.nextInt(dimension);
                    int z = random.nextInt(dimension);
                    int value = random.nextInt(124);
                    if (map.putIfAbsent(new Vec(x, y, z), value) != null) {
                        continue;
                    }
                    palette.set(x, y, z, value);
                }
            }
        }

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) threads.add(new PaletteThread());
        threads.forEach(Thread::start);
        for (Thread thread : threads) thread.join();

        map.forEach((point, value) ->
                assertEquals(value, palette.get((int) point.x(), (int) point.y(), (int) point.z())));
    }
}
