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

volume inputs[] <simple_mapper;prefix="130-fmri.",suffix=".input.in",separator=".">;
volume template <simple_mapper;prefix="130-fmri.",suffix=".template.in",separator=".">;

// align 4 volumes to reference volume

volume aligned[];

foreach inp, i in inputs {
  aligned[i] = align(inputs[i], template);
}

app (volume o) align(volume candidate, volume template) {
    touch @o.v @o.h;
}

// take average of all 4 (or 5?) volumes

app (volume avg) average(volume candidates[]) {
    touch @avg.v @avg.h;
}

volume brainatlas = average(aligned);

// make 3 slices of the average volume, along x, y, z axes

app (slice s) slicer(volume atlas, string axis) {
    touch @s;
}

slice slices[];


string axes[] = ["x", "y", "z"];
foreach axis, i in axes {
 slices[i] = slicer(brainatlas, axis);
}

// convert each slice into a jpeg

jpeg final[] <simple_mapper;prefix="130-fmri.",suffix=".jpeg",separator=".">;

app (jpeg j) convert(slice s) {
    touch @j;
}

foreach j,i in slices {
  final[i] = convert(slices[i]);
}

// outputs: 3 jpegs

