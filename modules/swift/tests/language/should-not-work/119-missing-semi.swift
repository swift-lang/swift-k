
// THIS-SCRIPT-SHOULD-FAIL

type file {};
type student {
  file name;
  file age;
  file gpa;
}
app (file t) getname(string n) {
        echo n stdout=@filename(t);
}

file results <single_file_mapper; file="sinfo.txt">;
student fnames[] <csv_mapper;file="stu_list.txt">
results = getname(@filename(fnames[0]));

