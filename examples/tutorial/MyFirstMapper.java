package tutorial;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class MyFirstMapper extends AbstractMapper {

  AbsFile myfile = new AbsFile("myfile.txt");

  public boolean isStatic() {
    return false;
  }

  public Collection existing() {
    if (myfile.exists())
      return Arrays.asList(new Path[] {Path.EMPTY_PATH});
    else
      return Collections.EMPTY_LIST;
  }

  public PhysicalFormat map(Path p) {
    if(p.equals(Path.EMPTY_PATH))
      return myfile;
    else
      return null;
  }
}
