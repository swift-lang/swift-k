#!/usr/bin/env ruby

require 'erb'
require 'ostruct'

# ports lists for the templates

#coaster_service[] = `cat service-*.sport;`
#worker_service[]  = `cat service-*.wport`;

coaster_service = 12345;
worker_service = 67890;

hostname="communicado.ci.uchicago.edu"; 
hostip="128.135.125.17";

coaster_sites = %q[
<config>
<% ctr = 0
   sites.each_key do |name|
     jm       = sites[name].jm
     url      = sites[name].url
     app_dir  = sites[name].app_dir
     data_dir = sites[name].data_dir
     throttle = sites[name].throttle %>

  <pool handle="<%=name%>">
    <execution provider="coaster-persistent" url="https://<%= hostname %>:<%= coaster_service + ctr %>"
        jobmanager="local:local" />

    <profile namespace="globus" key="workerManager">passive</profile>

    <profile namespace="karajan" key="initialScore">200.0</profile>
    <profile namespace="karajan" key="jobThrottle"><%=throttle%></profile>

    <gridftp  url="gsiftp://<%=url%>"/>
    <workdirectory><%=data_dir%>/swift_scratch</workdirectory>
  </pool>
<%   ctr += 1
   end %>
</config>
]

def OLDress_query(class_ads)
  cmd = "condor_status -pool engage-submit.renci.org"
  class_ads[0..-2].each do |class_ad|
    cmd << " -format \"%s|\" #{class_ad}"
  end
  cmd << " -format \"%s\\n\" #{class_ads[-1]}"
  `#{cmd}`
end

def ress_query(class_ads)
  cmd = "./ressfields.sh"
  class_ads[0..-1].each do |class_ad|
    cmd << " #{class_ad}"
  end
  `#{cmd}`
end

def ress_parse(app_name)
  dir_suffix = "/engage/#{app_name}"
  class_ads  = [
    "GlueSiteUniqueID", "GlueCEInfoHostName", "GlueCEInfoJobManager",
    "GlueCEInfoGatekeeperPort", "GlueCEInfoApplicationDir", "GlueCEInfoDataDir",
    "GlueCEInfoTotalCPUs"
  ]
  ress_query(class_ads).each_line do |line|
    line.chomp!
puts "ress_query: line is:"
puts "$"<<line<<"$"
puts "---"
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

    value.app_dir += dir_suffix
    value.data_dir += dir_suffix

    # Hard-wired exceptions
    value.app_dir  = "/osg/app"                     if name =~ /GridUNESP_CENTRAL/
    value.data_dir = "/osg/data"                    if name =~ /GridUNESP_CENTRAL/
    value.app_dir.sub!(dir_suffix, "/engage-#{app_name}")  if name =~ /BNL-ATLAS/
    value.data_dir.sub!(dir_suffix, "/engage-#{app_name}") if name =~ /BNL-ATLAS/

    yield name, value
  end
end

if __FILE__ == $0 then
  raise "No whitelist file" if !ARGV[0]

  # Blacklist of non-working sites
  blacklist = []
  ARGV[1]   = "scec" if !ARGV[1]
  whitelist = IO.readlines(ARGV[0]).map { |line| line.chomp! }

  # Removes duplicate site entries (i.e. multilpe GRAM endpoints)
  sites = {}
  ress_parse(ARGV[1]) do |name, value|
    next if blacklist.index(name) and not blacklist.empty?
    next if not whitelist.index(name) and not whitelist.empty?
    sites[name] = value if sites[name] == nil
  end

  # condor_out = File.open("condor_osg.xml", "w")
  # gt2_out = File.open("gt2_osg.xml", "w")
  coaster_out = File.open("coaster_osg.xml", "w")

  # tc_out     = File.open("tc.data", "w")
  # workflow_out = File.open("worker.swift", "w")
  # slave_out = File.open("slave.swift", "w")

  # condor = ERB.new(condor_sites, 0, "%<>")
  # gt2 = ERB.new(gt2_sites, 0, "%<>")
  coaster = ERB.new(coaster_sites, 0, "%<>")

  # tc     = ERB.new(swift_tc, 0, "%<>")
  # workflow = ERB.new(swift_workflow, 0, "%<>")
  # slave = ERB.new(slave_workflow, 0, "%<>")

  # condor_out.puts condor.result(binding)
  # gt2_out.puts gt2.result(binding)
  coaster_out.puts coaster.result(binding)

  # tc_out.puts tc.result(binding)
  # workflow_out.puts workflow.result(binding)
  # slave_out.puts slave.result(binding)

  # condor_out.close
  # gt2_out.close
  coaster_out.close

  # tc_out.close
  # workflow_out.close
  # slave_out.close
end
