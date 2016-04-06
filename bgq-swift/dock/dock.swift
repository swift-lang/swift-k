type file;

// Dock
app (file _dockoutfile, file out, file err) dock (file _dockinfile, file _mol2, file _defn, file _flexdefn, file _tbl, file _nrg, file _sph, file _bmp)
{
   bgsh "/home/ketan/dock6/bin/dock6" "-i" @_dockinfile "-o" @_dockoutfile stdout=@out stderr=@err; 
}

// Files
file infiles[] <filesys_mapper; location="/home/ketan/SwiftApps/subjobs/dock", suffix=".in">;
file logs[];

file mol2<"zinc_nci_0010.mol2">;
file defn<"vdw_AMBER_parm99.defn">;
file flexdefn<"flex.defn">;
file tbl<"flex_drive.tbl">;
file nrg<"1VRT.rec.nrg">;
file sph<"rec.10A.sph">;
file bmp<"1VRT.rec.bmp">;

// Run scaling tests
foreach infile,i in infiles {
   file dock_output <single_file_mapper; file=strcat("logs/dock.", i, ".stdout")>;
   file dock_error <single_file_mapper; file=strcat("logs/dock.", i, ".stderr")>;
   file dockoutfile <single_file_mapper; file=strcat("dock_", i, ".out")>;

   (dockoutfile, dock_output, dock_error) = dock(infile, mol2, defn, flexdefn, tbl, nrg, sph, bmp);
   logs[i] = dock_output;
}

