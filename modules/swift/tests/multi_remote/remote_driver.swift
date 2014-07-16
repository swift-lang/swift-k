type file;
type script;

file swift_package <"swift.tar">;
script wrapper     <"wrapper.sh">;
script publish     <"publish.sh">;

file out[] <simple_mapper;prefix="out/driver", suffix=".out">;
file err[] <simple_mapper;prefix="out/driver", suffix=".err">;
file log[] <simple_mapper;prefix="out/Logs_",  suffix=".tar">;

string sites[] = ["uc3", "beagle", "midway", "crush", "frisbee", "blues", "fusion", "bridled", "communicado"];

app (file out, file err, file log) remote_uc3	  (script run, file tar)
{
    uc3 @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_beagle  (script run, file tar)
{
    bgl @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_midway  (script run, file tar)
{
    mid @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_mcs   (script run, file tar)
{
    mcs @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_frisbee (script run, file tar)
{
    fsb @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_blues   (script run, file tar)
{
    blu @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_fusion  (script run, file tar)
{
    fus @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_communicado  (script run, file tar)
{
    com @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}

app (file out, file err, file log) remote_bridled  (script run, file tar)
{
    bri @run @filename(tar) @log stdout=@filename(out) stderr=@filename(err);
}


app (file out, file err) publish_results (script publish, file tar, file out, file err)
{
    pub @publish stdout=@out stderr=@err;
}


tracef("Filename of the wraper  : %s \n", @filename(wrapper));
tracef("Filename of the package : %s \n", @filename(swift_package));

foreach site, i in sites {
    //tracef("Site : %s \n", site);

    switch(i)
    {
	case 0:
        tracef("Calling uc3\n");
  		(out[i], err[i], log[i]) = remote_uc3     (wrapper, swift_package);
	case 1:
        tracef("Calling beagle \n");
  		(out[i], err[i], log[i]) = remote_beagle  (wrapper, swift_package);
	case 2:
        tracef("Skipping Midway : Tests will be run locally\n");
		(out[i], err[i], log[i]) = remote_midway  (wrapper, swift_package);
	case 3:
        tracef("Calling thwomp on MCS\n");
        (out[i], err[i], log[i]) = remote_mcs     (wrapper, swift_package);
	case 4:
        tracef("Skipping Frisbee on MCS (Will fail due to BUG:1030)\n");
		//(out[i], err[i], log[i]) = remote_frisbee (wrapper, swift_package);
	case 5:
        tracef("Calling Blues\n");
    	(out[i], err[i], log[i]) = remote_blues   (wrapper, swift_package);
	case 6:
        tracef("Calling Fusion\n");
   		(out[i], err[i], log[i]) = remote_fusion  (wrapper, swift_package);
	case 7:
        tracef("Calling Communicado\n");
		(out[i], err[i], log[i]) = remote_communicado  (wrapper, swift_package);
	case 8:
        tracef("Calling Bridled\n");
		(out[i], err[i], log[i]) = remote_bridled  (wrapper, swift_package);


	default:
		tracef("Fail: Unknown site %s : %i \n", site, i);
    }
}
