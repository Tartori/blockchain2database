package ch.bfh.blk2.bitcoin.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Sha256Hash;

import ch.bfh.blk2.bitcoin.producer.FileMapSerializer;

/**
 * This class is used to represent blockchain files (.blk). It can used the fileMap
 * to prune the list from files that do not need to be searched.
 * 
 * @author niklaus
 *
 */
public class BlockFileList implements Iterable<File> {
	private static final Logger logger = LogManager.getLogger("BlockFileList");

	private List<File> fileList;
	private int startHeight = -1;
	// Only needet for the sorter, not so for the producer
	private Sha256Hash rootHash = Sha256Hash.ZERO_HASH;

	/**
	 * Creates a file list from the .blk files found in the default location
	 */
	public BlockFileList() {
		fileList = getDefaultFileList();
	}

	/**
	 * Create a list of all files that can potentially contain blocks higher than height.
	 * This will first create a list of all .blk files from the default location, then
	 * read in the fileMap and then remove all elements from the file list that, according
	 * to the fileMap, do only contain blocks that are less high than height. The hash of
	 * the block is needed for the blocksorter to successfully identify the next higher block.
	 * 
	 * Note that you could, in theory, provide the wrong height, but I'm not too sure about how
	 * much fun that would actually be?
	 * 
	 * If no fileMap can be found, instead the list will be generated from the beginning.
	 * 
	 * @param height Height of the block from where on you want to generate the blockchain
	 * @param hash The hash of that same block
	 */
	public BlockFileList(int height, Sha256Hash hash) {
		List<File> compleatList = getDefaultFileList();
		Map<String, Integer> fileMap = FileMapSerializer.read();

		// Use default values and entire list if no fileMap was found
		if (fileMap == null) {
			logger.warn("Have not found a copy of the fileMap. Will recreate the entire blockchain from scratch");
			fileList = compleatList;
			return;
		}

		List<File> prunedList = new ArrayList<>(compleatList);
		// Newest file. Since the compleatlist is sorted, this works easy enough
		File newestFile = compleatList.get(compleatList.size() - 1);

		for (File f : compleatList)
			// Don't remove the newest file. It is always needet
			if (fileMap.containsKey(f.getName()) && fileMap.get(f.getName()) < height && !f.equals(newestFile)) {
				prunedList.remove(f);
				logger.debug("Ignoring file: " + f.getName());
			}

		if (prunedList.size() >= 42)
			logger.warn(
					"The list of blockfiles to readin is very large!\nIt may or may not be faster to delete the fileMap and create the entire blockchain from scratch");

		this.fileList = prunedList;
		this.startHeight = height;
		this.rootHash = hash;
	}

	/**
	 * Alternate constructor that allows passing the block hash as String instead of as Sha256Hash.
	 * It's mostly just here for convenience.
	 * 
	 * @param height Height of the block from where on you want to generate the blockchain
	 * @param hash The hash of that same block
	 */
	public BlockFileList(int height, String hash) {
		this(height, Sha256Hash.wrap(hash));
	}

	private List<File> getDefaultFileList() {

		String blockChainPath = PropertiesLoader.getInstance().getProperty("directory");

		File dir = new File(blockChainPath);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("blk\\d{5}.dat");
			}
		});

		List<File> blockChainFiles = new ArrayList<>(Arrays.asList(files));

		Collections.sort(blockChainFiles);

		return blockChainFiles;
	}

	@Override
	public Iterator<File> iterator() {
		return fileList.iterator();
	}

	public int getStartHeight() {
		return startHeight;
	}

	public Sha256Hash getRootHash() {
		return rootHash;
	}

	public List<File> getFileList() {
		return fileList;
	}

}
