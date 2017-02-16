package indexer;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author antska
 */
public class WarcIndexer {

	public static void main(String[] args) {
		// Get WARC files
		List<Path> files = null;
		long startTime = System.currentTimeMillis();

		try {
			String warcFilePath = "warcs";
			files = Files.walk(Paths.get(warcFilePath))
					.filter(Files::isRegularFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (files != null) {
			int totalWarcs = files.size();
			System.out.println("total warcs " + totalWarcs);
			int capacity = 200;
			List<List<Path>> subsets = Lists.partition(files, capacity);
			int counter = 1;
			for (List<Path> partition : subsets) {
				System.out.println("Getting partition " + counter + "/" + subsets.size());
				long startPr = System.currentTimeMillis();
				partition.parallelStream().forEach(f -> {
					try {
						if (f.toString().contains("WEB") && !f.toString().endsWith(".open")) {
							WarcTools.extract(f);
						}
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
}
