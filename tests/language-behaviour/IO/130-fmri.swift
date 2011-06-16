
# attempt to emulate the workflow patterns of the fMRI workflow that
# I often fiddle with.

type voxels;
type header;

type volume {
    voxels v;
    header h;
  }

type slice;

type jpeg;


// inputs: 4 volumes and a reference volume

volume inputs[] <simple_mapper;prefix="130-fmri.",suffix=".in">;
volume template <simple_mapper;prefix="130-fmri.",suffix=".template">;

// align 4 volumes to reference volume

volume aligned[];

foreach inp, i in inputs {
  aligned[i] = align(inputs[i], template);
}

(volume o) align(volume candidate, volume template) {
  app {
    touch @o.v @o.h;
  }
}

// take average of all 4 (or 5?) volumes

(volume avg) average(volume candidates[]) {
  app {
    touch @avg.v @avg.h;
  }
}

volume brainatlas = average(aligned);

// make 3 slices of the average volume, along x, y, z axes

(slice s) slicer(volume atlas, string axis) {
  app {
    touch @s;
  }
}

slice slices[];


string axes[] = ["x", "y", "z"];
foreach axis, i in axes {
 slices[i] = slicer(brainatlas, axis);
}

// convert each slice into a jpeg

jpeg final[] <simple_mapper;prefix="130-fmri.",suffix=".jpeg">;

(jpeg j) convert(slice s) {
  app {
    touch @j;
  }
}

foreach j,i in slices {
  final[i] = convert(slices[i]);
}

// outputs: 3 jpegs

