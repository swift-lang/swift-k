#!/usr/bin/env ruby

# File: mk_test.rb
# Date: 2010-10-06
# Author: Allan Espinosa
# Email: aespinosa@cs.uchicago.edu
# Description: A Swift workflow generator to test OSG sites through the Engage
#              VO. Generates the accompanying tc.data and sites.xml as well.
#              Run with "swift -sites.file sites.xml -tc.file tc.data
#              test_osg.swift"

require 'erb'
require 'ostruct'
require 'ress'

swift_tc = %q[
localhost            echo   /bin/echo                                       INSTALLED INTEL32::LINUX GLOBUS::maxwalltime="00:05:00"
<% ctr = 0
   sites.each_key do |name| %>
<%   jm       = sites[name].jm
     url      = sites[name].url
     app_dir  = sites[name].app_dir
     data_dir = sites[name].data_dir
     throttle = sites[name].throttle %>
<%=name%>  cat<%=ctr%>    /bin/cat      INSTALLED INTEL32::LINUX GLOBUS::maxwalltime="00:01:30"
<%   ctr += 1
   end %>
]

swift_workflow = %q[
type file;

app (file t) echo(string i) {
  echo i stdout=@filename(t);
}

<% ctr = 0
   sites.each_key do |name| %>
app (file t) cat<%= ctr %>(file input ) { 
  cat<%= ctr %> @filename(input) stdout=@filename(t);
}
<%   ctr += 1
   end %>

<% ctr = 0
   sites.each_key do |name| %>
file input<%= ctr %><"cat<%= ctr %>.in">;
input<%= ctr %> = echo("<%= name %>");
file out<%= ctr %><"cat<%= ctr %>.out">;
out<%= ctr %> = cat<%= ctr %>(input<%= ctr %>);
<%   ctr += 1
   end %>

]

condor_sites = %q[
<config>
  <pool handle="localhost">
    <filesystem provider="local" />
    <execution provider="local" />
    <workdirectory >/var/tmp</workdirectory>
    <profile namespace="karajan" key="jobThrottle">0</profile>
  </pool>
<% sites.each_key do |name| %>
<%   jm       = sites[name].jm
     url      = sites[name].url
     app_dir  = sites[name].app_dir
     data_dir = sites[name].data_dir
     throttle = sites[name].throttle %>

  <pool handle="<%=name%>">
    <execution provider="condor" url="none"/>

    <profile namespace="globus" key="jobType">grid</profile>
    <profile namespace="globus" key="gridResource">gt2 <%=url%>/jobmanager-<%=jm%></profile>

    <profile namespace="karajan" key="initialScore">20.0</profile>
    <profile namespace="karajan" key="jobThrottle"><%=throttle%></profile>

    <gridftp  url="gsiftp://<%=url%>"/>
    <workdirectory><%=data_dir%>/swift_scratch</workdirectory>
  </pool>
<% end %>
</config>
]

# Redlist of non-working sites
redlist = [ ]

puts("mk_test starting")

# Removes duplicate site entries (i.e. multilpe GRAM endpoints)
sites = {}
ress_parse do |name, value|
  next if redlist.index(name)
  sites[name] = value if sites[name] == nil
print("site: ")
puts(name)
#puts(name,value)
end

condor_out = File.open("sites.xml", "w")
tc_out     = File.open("tc.data", "w")
swift_out = File.open("test_osg.swift", "w")

condor = ERB.new(condor_sites, 0, "%<>")
tc     = ERB.new(swift_tc, 3, "%<>")
swift     = ERB.new(swift_workflow, 0, "%<>")

condor_out.puts condor.result(binding)
tc_out.puts tc.result(binding)
swift_out.puts swift.result(binding)

condor_out.close
tc_out.close
swift_out.close
