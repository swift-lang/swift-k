type file;
int arr[];

type messagefile;

app (messagefile t) greeting() {
    echo "Hello, world!" stdout=@filename(t);
}

foreach a,i in [0:2] {
 arr[i] = i;
}

string s[] = [ "0out.txt", "1out.txt", "2out.txt" ];

messagefile f[] <array_mapper;files=s>;

trace("arr",arr);

foreach a,i in arr {
f[a]=greeting();
trace(@filename(f[a]));
 trace("for", a,i);
}
