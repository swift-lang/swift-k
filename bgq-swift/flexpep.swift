type file;

app (file _scorefile, file sout, file serr) flexpep (file _pdb)
{
  runjob "/projects/ExM/ketan/openmp-gnu-july16-mini/bin/FlexPepDocking.staticmpi.linuxxlcdebug" 
         "-database /home/ketan/minirosetta_database" 
         "-pep_refine" "-s" @_pdb "-ex1" "-ex2aro"    
         "-use_input_sc" "-nstruct" "1" "-overwrite"  
         "-scorefile" @_scorefile                     
         stdout=@sout stderr=@serr;
}

file scores[] <simple_mapper; location="outdir", prefix="f.",suffix=".sc">;
file pdb_inputs[] <filesys_mapper; location="/home/ketan/hlac-97-D", pattern="*complex*">;

foreach pdb, j in pdb_inputs {
  file sout<single_file_mapper; file=strcat("outdir/f.",j,".stdout")>;
  file serr<single_file_mapper; file=strcat("outdir/f.",j,".stderr")>;
  (scores[j], sout, serr) = flexpep(pdb);
}

/*
qsub --env BG_SHAREDMEMSIZE=32MB -t 20 -n 2 --proccount 2  \
  --mode c1 \
  /projects/ExM/ketan/openmp-gnu-july16-mini/bin/FlexPepDocking.staticmpi.linuxxlcdebug \
  -database /home/ketan/minirosetta_database/ \
  -pep_refine \
  -s /home/ketan/hlac-97-D/hlac-97-D-AAADAAAAL_complex_0001.pdb \
  -ex1 \
  -ex2aro \
  -use_input_sc \
  -nstruct 1 -overwrite \
  -scorefile AAADAAAAL_complex_91R.sc
*/
