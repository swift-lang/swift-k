// this is from Tibi's message
// on 8th feb 2007777777

type file {}

//define the wavelet procedure
(file wavelets) waveletTransf (file waveletScript, int subjNo,
string trialType, file dataFiles) {
    app {
        cwtsmall @filename(waveletScript) subjNo trialType;
    }
}

(file outputs[]) batchTrials ( string trialTypes[] ){
    file waveletScript<single_file_mapper;
file="scripts/runTrialSubjectWavelet.R">;
    //file dataFiles[]<simple_mapper; prefix="101_", suffix="-epochs.Rdata">;

    foreach s,i in trialTypes {
        //file output<simple_mapper;prefix=s,suffix="101.tgz">;
        file dataFiles<simple_mapper; prefix="101_", suffix=s>;
        outputs[i] = waveletTransf(waveletScript,101,s,dataFiles);
    }
}

//string trialTypes[] = ["FB", "FC", "FI", "SB", "SC", "SI" ];
string trialTypes[] = [ "FB" ];

file allOutputs[];
file namedOutputs[]<fixed_array_mapper;
files="101-FBchannel10_cwt-results.Rdata, 101-FBchannel11_cwt-results.Rdata, 101-FBchannel12_cwt-results.Rdata, 101-FBchannel13_cwt-results.Rdata, 101-FBchannel14_cwt-results.Rdata, 101-FBchannel15_cwt-results.Rdata, 101-FBchannel16_cwt-results.Rdata, 101-FBchannel17_cwt-results.Rdata, 101-FBchannel18_cwt-results.Rdata, 101-FBchannel19_cwt-results.Rdata, 101-FBchannel1_cwt-results.Rdata, 101-FBchannel20_cwt-results.Rdata, 101-FBchannel21_cwt-results.Rdata, 101-FBchannel22_cwt-results.Rdata, 101-FBchannel23_cwt-results.Rdata, 101-FBchannel24_cwt-results.Rdata, 101-FBchannel25_cwt-results.Rdata, 101-FBchannel26_cwt-results.Rdata, 101-FBchannel27_cwt-results.Rdata, 101-FBchannel28_cwt-results.Rdata, 101-FBchannel2_cwt-results.Rdata, 101-FBchannel3_cwt-results.Rdata, 101-FBchannel4_cwt-results.Rdata, 101-FBchannel5_cwt-results.Rdata, 101-FBchannel6_cwt-results.Rdata, 101-FBchannel7_cwt-results.Rdata, 101-FBchannel8_cwt-results.Rdata, 101-FBchannel9_cwt-results.Rdata">;


//the MAIN program
//file waveletScript<single_file_mapper;
string file="scripts/runTrialSubjectWavelet.R";
//namedOutputs = waveletTransf(waveletScript, 101, "FB");
namedOutputs = batchTrials(trialTypes);

