package indexer;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author antska
 */
public class WarcTools {
	private static String urlString = "http://localhost:8983/solr/aueb/";
	private static SolrClient solr = new HttpSolrClient.Builder(urlString).build();

	private static Set<String> STANDARD_ENCODINGS = new HashSet<>(Arrays.asList(new String[]{"UTF-8", "ISO-8859-7",
			"WINDOWS-1253", "ISO-8859-1", "ISO-8859-2", "UTF-16", "WINDOWS-1252", "UTF-32", "CP1252", "CP1253"}));

	public static boolean extract(Path f) throws IOException {
		File file = f.toFile();
		InputStream in = new FileInputStream(file);
		WarcReader reader = WarcReaderFactory.getReader(in);
		WarcRecord record;
		try {
			while ((record = reader.getNextRecord()) != null) {
				String type = record.getHeader("WARC-type").value;

				if (type.equals("response")) {
					String url = record.getHeader("WARC-Target-URI").value;
					if (url.contains("dns:") || url.contains("robots.txt")){
						continue;
					}
					String contentType = record.getHttpHeader().contentType;
					String date = record.getHeader("WARC-Date").value;
					String encoding;
					if (contentType != null && contentType.contains("charset")) {
						encoding = contentType.split("charset")[1];
					} else {
						encoding = "";
					}
					String cleanDomain = getDomainName(record.getHeader("WARC-Target-URI").value);
					String[] contents = getContent(record.getPayloadContent(), encoding, url);

					if (cleanDomain != null) {
						SolrInputDocument document = new SolrInputDocument();
						document.addField("url", cleanDomain);
						document.addField("date", date);
						document.addField("title", contents[0]);
						document.addField("content", contents[1]);

						UpdateResponse response = solr.add(document);

						solr.commit();
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			reader.close();
			in.close();
		}
	}

	private static String getDomainName(String url) {
		URI uri = null;
		String domain = null;
		String res = null;
		try {
			uri = new URI(url);
			domain = uri.getHost();
			if (domain != null)
				res = domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return res;
	}

	private static String[] getContent(InputStream input, String enc, String url) throws IOException {
		// long start = System.currentTimeMillis();
		String cleanEnc;
		String title = "";
		String body = "";

		byte[] byteArray = IOUtils.toByteArray(input);

		if (enc.isEmpty()) {
			String html = new String(byteArray, "UTF-8");
			Elements metaTags = Jsoup.parse(html).head().select("meta");

			elements:
			for (Element metaTag : metaTags) {
				String content = metaTag.attr("content");
				if (content.contains("charset")) {
					enc = content.split("charset")[1];
					break elements;
				}
			}
			if (enc.isEmpty()) {
				enc = "UTF-8";
			}
		}
		try {
			cleanEnc = enc.replaceAll("[^a-zA-Z0-9 -]", "").trim().toUpperCase();
			cleanEnc = cleanEnc.split(" ")[0];

			switch (cleanEnc) {
				case "UTF8":
					cleanEnc = "UTF-8";
					break;
				case "ISO8859-7":
					cleanEnc = "ISO-8859-7";
					break;
				case "ISO8859-1":
					cleanEnc = "ISO-8859-1";
					break;
				case "ISO8859-2":
					cleanEnc = "ISO-8859-2";
					break;
			}

			if (STANDARD_ENCODINGS.contains(cleanEnc)) {
				String html_text = new String(byteArray, cleanEnc);
				title = Jsoup.parse(html_text).head().select("title").text();
				body = Jsoup.parse(html_text).body().text().replaceAll("\\P{L}", " ");
			} else {
				body = "";
				title = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new String[]{title, body};
	}
}
