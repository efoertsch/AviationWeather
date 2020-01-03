package org.soaringforecast.rasp.turnpoints.messages;

import java.io.File;

public final class ImportFile {

    private File file;

    public ImportFile(File file) {
        this.file  = file;
    }

    public File getFile() {
        return file;
    }

}
