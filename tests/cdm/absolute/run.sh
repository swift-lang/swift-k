swift -config cf.absolute -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid ff_config_abs  ff.swift

swift -config cf.absolute -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid fr_config_abs  fr.swift

swift -config cf.absolute -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid rf_config_abs rf.swift

swift -config cf.absolute -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid rr_config_abs rr.swift

swift -config cf.relative -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid ff_config_rel ff.swift

swift -config cf.relative -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid fr_config_rel fr.swift

swift -config cf.relative -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid rf_config_rel rf.swift

swift -config cf.relative -sites.file sites.xml -tc.file tc -cdm.file fs.data -runid rr_config_rel rr.swift

