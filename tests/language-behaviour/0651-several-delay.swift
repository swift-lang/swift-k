
delay() { 
    app {
        sleep "90s";
    }
}

foreach i in [1:10] {
delay();
}
