gnetconfig eth2
group -aiu add rob gsiftp://wiggum.mcs.anl.gov:2811/
group -aiu add rob gsiftp://arbat.mcs.anl.gov:2811/
open gsiftp://wiggum.mcs.anl.gov:2811/
mkdir testDir
cd testDir
pwd
put GoWorld.java GoWorld.java
ls -l
get GoWorld.java GoWorld-received.java
putdir testDir testDir
cd testDir
pwd
cd ..
rmdir -f testDir
ls
rm GoWorld.java
cd ..
rmdir testDir
put GoWorld.java GoWorld.java
close
exec --command "/homes/dsl/javapkgs/linux/j2sdk1.4.2_05/bin/javac GoWorld.java" --service wiggum.mcs.anl.gov
exec --command "/homes/dsl/javapkgs/linux/j2sdk1.4.2_05/bin/java GoWorld -i 4 -s 1" --service rob --group &
view file:///goWorld.gsh
ps