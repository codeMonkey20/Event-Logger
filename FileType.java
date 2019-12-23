import java.io.File;
import javax.swing.filechooser.*;

public class FileType extends FileFilter {
	private String format = "txt";
	private char dotIndex = '.';

	public boolean accept(File f) {
		if (f.isDirectory()) return true;

		if (extension(f).equalsIgnoreCase(format)) return true;
		else return false;
	}

	public String getDescription() {
		return "Text File (.txt)";
	}

	public String extension (File f) {
		String filename = f.getName();
		int indexfile = filename.lastIndexOf(dotIndex);
		if (indexfile > 0 && indexfile < filename.length() - 1) {
			return filename.substring(indexfile + 1);
		} else {
			return "";
		}
	}
}