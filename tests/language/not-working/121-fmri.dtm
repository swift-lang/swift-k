type file {};
type warpfile {};

type inputimg {
  file img;
  file data;
};

inputimg ref[] <csv_mapper;name="reference.csv">;
inputimg inputs[] <csv_mapper;name="inputs.csv">;

(warpfile o) alignwarp(inputimg ref, inputimg sub) {
    app {
        align_warp @ref.img @sub.img @o "-m" "12" "-q"
    }
}

