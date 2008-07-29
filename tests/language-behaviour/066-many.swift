
p() { 
    app {
        touch "nop";
    }
}

foreach i in [1:1000] {
    p();
}
