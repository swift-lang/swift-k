type file {};

(file gauge) stageIn (file input, int config) {
	app {
		stageIn "-v" "-i" @input "-c" config "-o" @gauge;
	}
}

(file output[]) stagSolve (file gauge, string mass, string source) {
	app {
		stagSolve "-v" "-g" @gauge "-m" mass "-s" source "-o" @output[*];
	}
}

(file output) cloverSolve ( float kappa, float cSW, file gauge, string source) {
	app {
		cloverSolve "-v" "-k" kappa "-c" cSW "-g" @gauge "-s" source "-o" @output;
	}
}

(file output) cvt12x12 (file input) {
	app {
		CVT12x12 "-v" "-i" @input "-o" @output;
	}
}

(file output) archive (file input) {
	app {
		Archive "-v" "-i" @input "-o" @output;
	}
}

(file output) archiveStag (string mass, file input[]) {
	app {
		ArchiveStag "-v" "-m" mass "-i" @input[*] "-o" @output;
	} 
}

(file sdo) twoPtHH (file gauge, file antiQ, file Q0, file Q1, file Q2) {
	app {
		TwoPtHH "-v" "-g" @gauge "-a" @antiQ "-0" @Q0 "-1" @Q1 "-2" @Q2 stdout=@sdo;
	}
}

(file sdo) twoPtSH (file gauge, file stag, file antiQ, file Q0, file Q1, file Q2) {
	app {
		TwoPtSH "-v" "-g" @gauge "-s" @stag "-a" @antiQ "-0" @Q0 "-1" @Q1 "-2" @Q2 stdout=@sdo;
	}
}

string confList[]=["000102","000108","000114"];

#string ensemble = "l4096f21b708m0031m031";
float kappaQ = 0.127; 
float cSW = 1.75;

string mass = "0.005,0.007,0.01,0.02,0.03";
string fn[] = [ "m0.005_000102 m0.007_000102 m0.01_000102 m0.02_000102 m0.03_000102",
		"m0.005_000108 m0.007_000108 m0.01_000108 m0.02_000108 m0.03_000108",
		"m0.005_000114 m0.007_000114 m0.01_000114 m0.02_000114 m0.03_000114" ];

int conflist[];

foreach config,i in conflist {
	string source = "local,0,0,0,0";

	# gauge template name 
	file template<"foo">;

	file gauge = stageIn (template, config);

	# need config to be put into filenames too
	#string fn = "m0.005 m0.007 m0.01 m0.02 m0.03";
	file stags[]<fixed_array_mapper; files=fn[i]>; 
	
	stags = stagSolve(gauge, mass, source);

	file stagTar<simple_mapper; suffix=".tar">;
	stagTar = archiveStag(mass, stags);

	file clover0 = cloverSolve(kappaQ, cSW, gauge, source);
	file q0 = cvt12x12(clover0);
	file cvtArch0 = archive(q0);

	string source1 = "wavefunction,0,1S";
	file clover1 = cloverSolve(kappaQ, cSW, gauge, source1);
	file q1 = cvt12x12(clover1);
	file cvtArch1 = archive(q1);

	string source2 = "wavefunction,0,2S";
	file clover2 = cloverSolve(kappaQ, cSW, gauge, source2);
	file q2 = cvt12x12(clover2);
	file cvtArch2 = archive(q2);

	file antiQ = q0;
	file pStdout = twoPtHH(gauge, antiQ, q0, q1, q2);
	foreach stag in stags {
		file sStdout = twoPtSH(gauge, stag, antiQ, q0, q1, q2);
	}
}
