gsh gnet.gsh
gsh connection.gsh
open --group example
put input/GoWorld.java GoWorld.java
close
exec --command "/homes/dsl/javapkgs/linux/j2sdk1.4.2_05/bin/javac GoWorld.java" --group --service example
exec --command "/homes/dsl/javapkgs/linux/j2sdk1.4.2_05/bin/java GoWorld -i 4 -s 1" --group --service example