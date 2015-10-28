type file;

app (file o1, file e1, file o2, file e2) remote_driver ()
{
    date stdout=@o1 stderr=@e1;
    ls stdout=@o2 stderr=@e2;
}

file d1_o <"sanity1.out">;
file d1_e <"sanity1.err">;
file d2_o <"sanity2.out">;
file d2_e <"sanity2.err">;

(d1_o, d1_e, d2_o, d2_e) = remote_driver();
