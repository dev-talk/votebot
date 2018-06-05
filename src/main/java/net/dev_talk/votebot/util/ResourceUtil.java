package net.dev_talk.votebot.util;

import java.io.*;

public class ResourceUtil {
    private static final int BUFFER_SIZE = 4096;

    /**
     * Saves the resource from the provided classloader located in the provided resource-path to the destination file.
     * If the destination file exists, the method won't overwrite it.
     * If it doesn't exist, parent directories and the destination file are created by the method.
     *
     * @param classLoader  Classloader to get the resource from
     * @param resourcePath Relative path of the resource
     * @param destination  Relative path of the destination file
     *
     * @return {@code True} if the resource was saved
     *
     * @throws IOException Exception thrown while saving the resource
     */
    public static boolean saveDefaultResource(final ClassLoader classLoader, final String resourcePath,
                                              final File destination) throws IOException {
        if (destination.exists()) {
            return false;
        }

        try (final InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException(String.format("Resource %s not found!", resourcePath));
            }

            final File outDir = new File(destination.getParent());
            outDir.mkdirs();

            int readBytes;
            final byte[] buffer = new byte[BUFFER_SIZE];
            try (final OutputStream out = new FileOutputStream(destination)) {
                while ((readBytes = inputStream.read(buffer)) > 0) {
                    out.write(buffer, 0, readBytes);
                }
            }
        }
        return true;
    }
}
