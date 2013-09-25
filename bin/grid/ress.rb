require 'ostruct'

def ress_query(class_ads)
  cmd = "condor_status -pool engage-submit.renci.org"
  class_ads[0..-2].each do |class_ad|
    cmd << " -format \"%s|\" #{class_ad}"
  end
  cmd << " -format \"%s\\n\" #{class_ads[-1]}"
  `#{cmd}`
end

def ress_parse
  dir_suffix = "/engage/swift"
  class_ads  = [
    "GlueSiteUniqueID", "GlueCEInfoHostName", "GlueCEInfoJobManager",
    "GlueCEInfoGatekeeperPort", "GlueCEInfoApplicationDir", "GlueCEInfoDataDir",
    "GlueCEInfoTotalCPUs"
  ]
  ress_query(class_ads).each_line do |line|
    line.chomp!
    set = line.split("|")
    next if not set.size > 0

    value = OpenStruct.new

    value.jm       = set[class_ads.index("GlueCEInfoJobManager")]
    value.url      = set[class_ads.index("GlueCEInfoHostName")]
    value.throttle = (set[class_ads.index("GlueCEInfoTotalCPUs")].to_f - 2.0) / 100.0
    name           = set[class_ads.index("GlueSiteUniqueID")] + "__" +  value.url
    value.name     = set[class_ads.index("GlueSiteUniqueID")]

    value.app_dir = set[class_ads.index("GlueCEInfoApplicationDir")]
    value.app_dir.sub!(/\/$/, "")
    value.data_dir = set[class_ads.index("GlueCEInfoDataDir")]
    value.data_dir.sub!(/\/$/, "")

    value.app_dir = "/osg/app" if name =~ /GridUNESP_CENTRAL/
    value.data_dir = "/osg/data" if name =~ /GridUNESP_CENTRAL/

    if name =~ /BNL-ATLAS/
      value.app_dir += "/engage-scec"
      value.data_dir += "/engage-scec"
    #elsif name == "LIGO_UWM_NEMO" or name == "SMU_PHY" or name == "UFlorida-HPC" or name == "RENCI-Engagement" or name == "RENCI-Blueridge"
      #value.app_dir += "/osg/scec"
      #value.data_dir += "/osg/scec"
    else
      value.app_dir += dir_suffix
      value.data_dir += dir_suffix
    end
    yield name, value
  end
end

