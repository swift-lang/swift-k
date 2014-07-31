type imagefile;
type landuse;
type perlscript;

perlscript getlanduse_pl <"getlanduse.pl">;

app (landuse output) getLandUse (imagefile input, perlscript ps)
{
  perl @ps @filename(input) stdout=@filename(output);
}

# Constants and command line arguments
int nFiles       = @toInt(@arg("nfiles", "1000"));
string MODISdir  = @arg("modisdir", "../data/modis/2002");

# Input Dataset
imagefile geos[] <filesys_mapper; location=MODISdir, suffix=".rgb">;

# Compute the land use summary of each MODIS tile
landuse land[] <structured_regexp_mapper; source=geos, match="(h..v..)",
                transform=@strcat("landuse/\\1.landuse.byfreq")>;

foreach g,i in geos {
    land[i] = getLandUse(g, getlanduse_pl);
}
