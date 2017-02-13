package som.vmobjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SFile {
  private final File file;
  private FileOutputStream outputStream;
  private FileInputStream inputStream;
  private long position;

  public SFile(File fileParam, boolean writable) {
    file = fileParam;
    if (writable) {
      setInputStream(null);
      try {
        setOutputStream(new FileOutputStream(file));
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        setOutputStream(null);
      }
    } else {
      setOutputStream(null);
      try {
        setInputStream(new FileInputStream(file));
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        setInputStream(null);
      }
    }
    setPosition(1);
  }

  public FileOutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(FileOutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public FileInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(FileInputStream inputStream) {
    this.inputStream = inputStream;
  }

  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    try {
      this.getInputStream().getChannel().position(position);
      this.position = position;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public File getFile() {
    return file;
  }

}
