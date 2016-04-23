import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTextReplacementInFiles {

	private String startingDir;
	private String regexPattern;
	private String replacement;
	private String fileAcceptPattern;
	private boolean isPatternFound = false;
	private int countOfProcessedFiles = 0;
	private Map<String, Integer> processedReport = new HashMap<String, Integer>();

	public static void process(String startingDir, String regexPattern,
			String replacement, String fileAcceptPattern) {

		RegexTextReplacementInFiles ob = new RegexTextReplacementInFiles();

		ob.startingDir = startingDir;

		if (!ob.isValidRegexPattern(regexPattern))
			return;

		if (!ob.isSingleGroupRegexPattern(regexPattern))
			return;

		ob.regexPattern = regexPattern;
		ob.replacement = replacement;

		if (fileAcceptPattern.startsWith("*")
				|| fileAcceptPattern.startsWith("+")
				|| fileAcceptPattern.startsWith("?"))
			fileAcceptPattern = "." + fileAcceptPattern;

		if (!ob.isValidRegexPattern(fileAcceptPattern))
			return;

		ob.fileAcceptPattern = fileAcceptPattern;

		ob.processFiles(ob.startingDir);

		ob.printProcessedReport();
	}

	private boolean isValidRegexPattern(String regexPattern1) {
		try {
			Pattern p = Pattern.compile(regexPattern1);

		} catch (Exception e) {
			System.out.println("ErrorMsg: Not a valid regex Pattern");
			return false;
		}
		return true;
	}

	private boolean isSingleGroupRegexPattern(String regexPattern2) {
	Pattern pattern = Pattern.compile(regexPattern2);
        Matcher matcher = pattern.matcher("");

        int groupCount = matcher.groupCount();
        
		if (groupCount > 1) {
			System.out.println("ErrorMsg: more than 1 group in regexPattern");
			return false;
		} else
			return true;
	}

	private void processFiles(String sDir) {

		File f = new File(sDir);
		if (f.isFile()) {

			boolean fileAccepted = isFileAccepted(f.getName());

			if (fileAccepted) {

				String content = getContentsOfFile(f.getAbsolutePath());
				content = content.trim(); // handling unwanted whitespaces in
											// file

				if (content == null || content.equals(""))
					return;
				else {
					processSingleFile(f, content);
				}

			} else
				return;

		} else {
			File[] faFiles = new File(sDir).listFiles();

			for (File file : faFiles) {
				if (file.isDirectory()) {
					processFiles(file.getAbsolutePath());
				} else if (file.isFile()) {

					boolean fileAccepted = isFileAccepted(file.getName());
					if (fileAccepted) {

						String content = getContentsOfFile(file
								.getAbsolutePath());
						content = content.trim(); // handling unwanted
													// whitespaces in file

						if (content == null || content.equals(""))
							continue;
						else
							processSingleFile(file, content);

					} else
						continue;
				}

			}// for

		}// else
	}

	private void processSingleFile(File file, String content) {

		String processedContent = updateProcessedReport(content);
		if (this.isPatternFound) {
			countOfProcessedFiles++;
			writeContentToNewFile(processedContent, file.getAbsolutePath());

		}

	}

	private boolean isFileAccepted(String name) {

		if (fileAcceptPattern == null || fileAcceptPattern == "")
			return true;

		String filePattern = fileAcceptPattern;
		Pattern p = Pattern.compile(filePattern);

		Matcher m = p.matcher(name);
		if (m.find())
			return true;
		else
			return false;

	}

	private String getContentsOfFile(String absolutePath) {

		String content = null;
		try {
			File file = new File(absolutePath);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			content = new String(data, "UTF-8");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private String updateProcessedReport(String content) {

		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(content);

		isPatternFound = false;
		while (matcher.find()) {

			isPatternFound = true;

			String patternMatched = matcher.group(1);

			updateCount(patternMatched);

			content = content.replaceFirst(patternMatched, replacement);
		}
		return content;
	}

	private void updateCount(String patternMatched) {
		if (processedReport.containsKey(patternMatched)) {
			int c = processedReport.get(patternMatched.trim());
			c = c + 1;
			processedReport.put(patternMatched, c);
		} else {
			processedReport.put(patternMatched, 1);
		}
	}

	private void writeContentToNewFile(String processedContent,
			String absolutePath) {

		File file = new File(absolutePath + ".processed");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(processedContent);
			bw.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void printProcessedReport() {
		System.out.println("Processed " + countOfProcessedFiles
				+ " files. Replaced to \"" + replacement + "\":");
		for (String s : processedReport.keySet()) {
			System.out.println("* " + s + " : " + processedReport.get(s)
					+ " occurrances");
		}

	}

	public static void main(String[] args) {
		String startingDir = null, regexPattern = null, replacement = null, fileAcceptPattern = null;

		if (args.length >= 3) {
			startingDir = args[0];
			regexPattern = args[1];
			replacement = args[2];
		}
		if (args.length >= 4) {
			fileAcceptPattern = args[3];
		}
		if (startingDir != null) {
			process(startingDir, regexPattern, replacement, fileAcceptPattern);
		} else {
			System.out.println("Expected at least 3 parameters but got "
					+ args.length);
		}
	}

}
