import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Used to copy all TEXT files ONLY from the OANC Corpus to the "Words for Days" folder. Replace the initial file and targetPath String in the main method for use
 * with any other corpus.
 */
public class CopyTextFiles {
	public static void main(String[] args) throws IOException {
		File initial = new File("C:/Users/vip/Documents/Files/Other/linguistics/Test/WeightedTrie/OANC Corpus");
		String targetPath = "C:/Users/vip/Documents/Files/Other/linguistics/Test/WeightedTrie/Words for days/";
		copyTextFiles(initial, targetPath);
	}

	public static void copyTextFiles(File currFile, String targetPath) throws IOException {
		System.out.println("Reading " + currFile.getPath() + "... ");
		if (!currFile.isDirectory()) {
			if (isTextFile(currFile)) {
				System.out.println("\tCopying... ");
				Files.copy(Paths.get(currFile.getAbsolutePath()), Paths.get(targetPath + currFile.getParentFile().getName() + " - " + currFile.getName()));
			}
		} else {
			for (File f : currFile.listFiles()) {
				copyTextFiles(f, targetPath);
			}
		}
	}

	public static boolean isTextFile(File f) {
		return f.getAbsolutePath().substring(f.getAbsolutePath().length() - 4).equals(".txt");
	}
}