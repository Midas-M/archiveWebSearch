package indexer;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author antska
 */
public class WarcIndexer {

	public static void main(String[] args) {
		// Get WARC files
		List<File> files = null;
		long startTime = System.currentTimeMillis();

		String warcFilePath = "/mnt/virtual_data/WARCS/";
		// String warcFilePath = "/Users/antska/Git/archive_aueb/warcs";
		File warcFile = new File(warcFilePath);
		IOFileFilter suffixFileFilter = FileFilterUtils.suffixFileFilter ("warc.gz");
		files = (List<File>) FileUtils.listFiles(warcFile, suffixFileFilter, TrueFileFilter.INSTANCE);

		int totalWarcs = files.size();
		System.out.println("total warcs " + totalWarcs);
		int capacity = 50;
		List<List<File>> subsets = Lists.partition(files, capacity);
		int counter = 1;
		for (List<File> partition : subsets) {
			System.out.println("Getting partition " + counter + "/" + subsets.size());
			long startPr = System.currentTimeMillis();
			partition.parallelStream().forEach(f -> {
				try {
					WarcTools.extract(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			counter++;
			long endPr = System.currentTimeMillis();
			long elapsedPr = endPr - startPr;
			System.out.println("Time elapsed for partition: " + elapsedPr / 1000 + "sec (" +
					(elapsedPr / 1000) / 60.0 + "min)");

		}
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Total time: " + elapsedTime / 1000 + "sec | " + (elapsedTime / 1000) / 60.0 + "min");
	}
}
