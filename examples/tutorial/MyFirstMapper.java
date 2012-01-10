/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
