package ch.bfh.blk2.bitcoin.producer;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Locale;
import static com.google.common.base.Preconditions.checkArgument;


/**
 * This class is a custom BlockFileLoader. With BlockFileLoader of Bitconj not all blocks
 * have been loaded from .dat files. In this BlockFileLoader it is allowed to be till 4Mb.
 * This is the maximal size of a block with witness together.
 *
 *
 * @author Anna
 *
 */
public class BlockFileLoader implements Iterable<Block>, Iterator<Block> {

    private NetworkParameters params;
    private Iterator<File> fileIt;
    private File file = null;
    private Block nextBlock = null;
    private FileInputStream currentFileStream = null;

    public BlockFileLoader(NetworkParameters params, List<File> files) {
        fileIt = files.iterator();
        this.params = params;
    }

    /**
     * Gets the list of files which contain blocks from Bitcoin Core.
     */
    public static List<File> getReferenceClientBlockFileList(File blocksDir) {
        checkArgument(blocksDir.isDirectory(), "%s is not a directory", blocksDir);
        List<File> list = new LinkedList<>();
        for (int i = 0; true; i++) {
            File file = new File(blocksDir, String.format(Locale.US, "blk%05d.dat", i));
            if (!file.exists())
                break;
            list.add(file);
        }
        return list;
    }

    public static List<File> getReferenceClientBlockFileList() {
        return getReferenceClientBlockFileList(defaultBlocksDir());
    }

    public static File defaultBlocksDir() {
        final File defaultBlocksDir;
        if (Utils.isWindows()) {
            defaultBlocksDir = new File(System.getenv("APPDATA") + "\\.bitcoin\\blocks\\");
        } else if (Utils.isMac()) {
            defaultBlocksDir = new File(System.getProperty("user.home") + "/Library/Application Support/Bitcoin/blocks/");
        } else if (Utils.isLinux()) {
            defaultBlocksDir = new File(System.getProperty("user.home") + "/.bitcoin/blocks/");
        } else {
            throw new RuntimeException("Unsupported system");
        }
        return defaultBlocksDir;
    }

    @Override
    public boolean hasNext() {
        if (nextBlock == null)
            loadNextBlock();
        return nextBlock != null;
    }

    @Override
    public Block next() throws NoSuchElementException {
        if (!hasNext())
            throw new NoSuchElementException();
        Block next = nextBlock;
        nextBlock = null;
        return next;
    }

    private void loadNextBlock() {
        while (true) {
            try {
                if (!fileIt.hasNext() && (currentFileStream == null || currentFileStream.available() < 1))
                    break;
            } catch (IOException e) {
                currentFileStream = null;
                if (!fileIt.hasNext())
                    break;
            }
            while (true) {
                try {
                    if (currentFileStream != null && currentFileStream.available() > 0)
                        break;
                } catch (IOException e1) {
                    currentFileStream = null;
                }
                if (!fileIt.hasNext()) {
                    nextBlock = null;
                    currentFileStream = null;
                    return;
                }
                file = fileIt.next();
                try {
                    currentFileStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    currentFileStream = null;
                }
            }
            try {
                int nextChar = currentFileStream.read();
                while (nextChar != -1) {
                    if (nextChar != ((params.getPacketMagic() >>> 24) & 0xff)) {
                        nextChar = currentFileStream.read();
                        continue;
                    }
                    nextChar = currentFileStream.read();
                    if (nextChar != ((params.getPacketMagic() >>> 16) & 0xff))
                        continue;
                    nextChar = currentFileStream.read();
                    if (nextChar != ((params.getPacketMagic() >>> 8) & 0xff))
                        continue;
                    nextChar = currentFileStream.read();
                    if (nextChar == (params.getPacketMagic() & 0xff))
                        break;
                }
                byte[] bytes = new byte[4];
                currentFileStream.read(bytes, 0, 4);
                long size = Utils.readUint32BE(Utils.reverseBytes(bytes), 0);
                if (size > 1024*1024*4 || size <= 0)
                    continue;
                bytes = new byte[(int) size];
                currentFileStream.read(bytes, 0, (int) size);
                try {
                    nextBlock = params.getDefaultSerializer().makeBlock(bytes);
                } catch (ProtocolException e) {
                    nextBlock = null;
                    continue;
                } catch (Exception e) {
                    throw new RuntimeException("unexpected problem with block in " + file, e);
                }
                break;
            } catch (IOException e) {
                currentFileStream = null;
                continue;
            }
        }
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Block> iterator() {
        return this;
    }
}
