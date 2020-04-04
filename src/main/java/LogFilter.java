import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/* ImageFilter.java is a 1.4 example used by FileChooserDemo2.java. */
class LogFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals("slf")) 
                 {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Log Files";
    }
}
