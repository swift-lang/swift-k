type voxelfile;
type headerfile;

type pgmfile;
type imagefile;

type warpfile;

type volume {
    voxelfile img;
    headerfile hdr;
};

(warpfile warp) align_warp(volume reference, volume subject, string model, string quick) {
    app {
        align_warp @reference.img @subject.img @warp "-m " model quick;
    }
}

(volume sliced) reslice(warpfile warp, volume subject)
{
    app {
        reslice @warp @sliced.img;
    }
}

(volume sliced) align_and_reslice(volume reference, volume subject, string model, string quick) {
    warpfile warp;
    warp = align_warp(reference, subject, model, quick);
    sliced = reslice(warp, subject);
}


(volume atlas) softmean(volume sliced[])
{
    app {
        softmean @atlas.img "y" "null" @filenames(sliced[*].img);
    }
}


(pgmfile outslice) slicer(volume input, string axis, string position)
{
    app {
        slicer @input.img axis position @outslice;
    }
}

(imagefile outimg) convert(pgmfile inpgm)
{
    app {
        convert @inpgm @outimg;
    }
}

(imagefile outimg) slice_to_jpeg(volume inp, string axis, string position)
{
    pgmfile outslice;
    outslice = slicer(inp, axis, position);
    outimg = convert(outslice);
}

(volume s[]) all_align_reslices(volume reference, volume subjects[]) {

    foreach subject, i in subjects {
        s[i] = align_and_reslice(reference, subjects[i], "12", "-q");
    }

}


volume references[] <csv_mapper;file="reference.csv">;
volume reference=references[0];

volume subjects[] <csv_mapper;file="subjects.csv">;

volume slices[] <csv_mapper;file="slices.csv">;
slices = all_align_reslices(reference, subjects);

volume atlas <simple_mapper;prefix="atlas">;
atlas = softmean(slices);

string directions[] = [ "x", "y", "z"];

foreach direction in directions {
    imagefile o <single_file_mapper;file=@strcat("atlas-",direction,".jpeg")>;
    string option = @strcat("-",direction);
    o = slice_to_jpeg(atlas, option, ".5");
}

