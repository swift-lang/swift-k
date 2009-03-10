
p() { 
    app {
        touch "nop";
    }
}

foreach i in [1:3000] {
    p();
}
