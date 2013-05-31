package esgf.node.stager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class MiscUtils {

    /**
     * Read a file and dump it to the passed log.
     *
     * @param f file to read from
     * @param log log to write to
     * @throws IOException If reading fails
     */
    public static void showFile(File f, Logger log) throws IOException {
        FileInputStream in = new FileInputStream(f);

        byte[] buff = new byte[1024];
        int read = 0;
        while ((read = in.read(buff)) > 0) {
            log.info(new String(buff, 0, read));
        }

        in.close();
    }

    /**
     * Empties a directory (i.e. recursive deletion of all files and sub
     * directories within). The here given directory will not be deleted.
     *
     * @param dir directory to empty
     * @param LOG logger to where we should write. (there is no point in
     *            defining a logger here)
     */
    public static void emptyDir(File dir, Logger LOG) {
        if (dir.isDirectory()) {
            File[] content = dir.listFiles();
            for (int i = 0; i < content.length; i++) {
                deleteAll(content[i], LOG);
            }
        }
    }

    /**
     * Used for recursive deletion of a directory structure.
     *
     * @param file directory/file being currently handled
     * @param LOG logger to where we should write. (there is no point in
     *            defining a logger here)
     */
    public static void deleteAll(File file, Logger LOG) {
        if (!file.isDirectory()) {
            boolean result = file.delete();
                LOG.debug("Deleting file: " + file.getAbsolutePath() + " result: "
                    + result);
        } else {
            File[] content = file.listFiles();
            for (int i = 0; i < content.length; i++) {
                deleteAll(content[i], LOG);
            }
            if (!file.delete()) {
                LOG.warn("Could not delete directory: "
                        + file.getAbsolutePath());
            }
        }
    }
}
