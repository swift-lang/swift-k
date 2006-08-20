This version of the CoG Kit works with GT4.0.1 and GT 4.0.2. As GT4.0.0 contains
some limitations we do not recommend that you use GT4.0.0.

The purpose of this readme is to list the dependencies on the string "GT4.0.0" 
and GT4_0_0 to simplify the transition to a more appropriate naming.

To make this more apparent, we will rename GT4_0_0 in near future either to GT4 or GT4.0.2. We have not yet decided what is better.



BUGS.txt:PLEASE NOTE ALTHOUGH THE PROVIDER FOR GT4_0_0 IS UNDER DEVELOPMENT.
BUGS.txt:IN THE GT4_0_0 DIRECTORY YOU STILL FIND THE 3.9.5 PROVIDER which is a

Comment: This needs to be changed as we supprt GT4.0.1 and GT4.0.2

bin/release/Makefile:PROVIDERS=gt3-common gt3_2_1 gt4_0_0 local webdav ssh condor gt2ft
man/cog-job-submit.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0,
man/cog-task2xml.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0, 
man/cogrun.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0,
modules/abstraction/dependencies.xml: <property name="module" value="provider-gt4_0_0"/>
modules/abstraction-examples/meta/cog-file-operation/usage.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0, 
modules/abstraction-examples/meta/cog-job-submit/usage.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0, 
modules/abstraction-examples/meta/cog-task2xml/usage.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0, 
modules/abstraction-examples/meta/cogrun/usage.txt: Provider; available providers: [gt2ft, gsiftp, file, gt4.0.0, 
modules/provider-gt3-common/meta/description.txt:basis wihtout any support. Please use the GT4.0.0 provider instead. 
modules/provider-gt3_0_2/meta/description.txt:basis wihtout any support. Please use the GT4.0.0 provider instead. 
modules/provider-gt3_2_0/meta/description.txt:basis wihtout any support. Please use the GT4.0.0 provider instead. 
modules/provider-gt3_2_1/meta/description.txt:basis wihtout any support. Please use the GT4.0.0 provider instead. 
modules/provider-gt4_0_0/build.xml: <mkdir dir="${dist.dir}/lib-gt4_0_0"/>
modules/provider-gt4_0_0/build.xml: <copy todir="${dist.dir}/lib-gt4_0_0">
modules/provider-gt4_0_0/build.xml:  <fileset dir="lib-gt4_0_0" includes="*.*"/>
modules/provider-gt4_0_0/build.xml: <mkdir dir="${dist.dir}/lib-gt4_0_0"/>
modules/provider-gt4_0_0/build.xml: <copy todir="${dist.dir}/lib-gt4_0_0">
modules/provider-gt4_0_0/build.xml:  <fileset dir="lib-gt4_0_0" includes="*.*"/>
modules/provider-gt4_0_0/CHANGES.txt: org.globus.cog.abstraction.impl.execution.gt4_0_0ft
modules/provider-gt4_0_0/CHANGES.txt: org.globus.cog.abstraction.impl.execution.gt4_0_0ft
modules/provider-gt4_0_0/CHANGES.txt:(04/19/2005) Renamed this module from "core-provider-gt4.0.0" to 
modules/provider-gt4_0_0/CHANGES.txt: "provider-gt4.0.0"
modules/provider-gt4_0_0/meta/description.txt:The provider-gt4.0.0 module is a component of the abstractions framework.
modules/provider-gt4_0_0/project.properties:module.name = provider-gt4_0_0
modules/provider-gt4_0_0/project.properties:long.name = GT4.0.0 provider for abstractions
modules/provider-gt4_0_0/project.properties:extra.lib.dir = lib-gt4_0_0
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:relative=cog-provider-clref-gt4_0_0
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/addressing-1.0.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/axis-url.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/axis.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/cog-axis.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/cog-url.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/commonj.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/commons-beanutils.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/commons-collections-3.0.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/commons-digester.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/commons-discovery.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/globus_delegation_service.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/globus_delegation_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/globus_wsrf_mds_aggregator_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/globus_wsrf_rft_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/gram-client.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/gram-stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/gram-utils.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/jaxrpc.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/naming-common.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/naming-factory.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/naming-java.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/naming-resources.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/opensaml.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/saaj.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsdl4j.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsrf_core.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsrf_core_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsrf_mds_index_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsrf_mds_usefulrp_schema_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsrf_provider_jce.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wsrf_tools.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/wss4j.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/xalan.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/xercesImpl.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/xml-apis.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/xmlrpc-1.1.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/xmlsec.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/globus_wsrf_rendezvous_stubs.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:rjar=../lib-gt4_0_0/globus_wsrf_rendezvous_service.jar
modules/provider-gt4_0_0/resources/classloader-gt4_0_0.properties:package=org.globus.cog.abstraction.impl.execution.gt4_0_0
modules/provider-gt4_0_0/resources/cog-provider.properties:classloader.properties=classloader-gt4_0_0.properties
modules/provider-gt4_0_0/resources/cog-provider.properties:#classloader.boot=org.globus.cog.abstraction.impl.execution.gt4_0_0.Boot
modules/provider-gt4_0_0/resources/cog-provider.properties:sandbox.boot=org.globus.cog.abstraction.impl.execution.gt4_0_0.Boot
modules/provider-gt4_0_0/resources/cog-provider.properties:config.path=${user.home}/.globus/cog-provider-gt4_0_0/
modules/provider-gt4_0_0/resources/cog-provider.properties:globus.location=${user.home}/.globus/cog-provider-gt4_0_0/
modules/provider-gt4_0_0/resources/cog-provider.properties:server.webroot=${user.home}/.globus/cog-provider-gt4_0_0/
modules/provider-gt4_0_0/resources/cog-provider.properties:schema.location=${user.home}/.globus/cog-provider-gt4_0_0/share/schema/
modules/provider-gt4_0_0/resources/cog-provider.properties:SCHEMA_LOCATION=${user.home}/.globus/cog-provider-gt4_0_0/share/schema/
modules/provider-gt4_0_0/resources/cog-provider.properties:executionTaskHandler=org.globus.cog.abstraction.impl.execution.gt4_0_0.TaskHandlerImpl
modules/provider-gt4_0_0/resources/cog-provider.properties:securityContext=org.globus.cog.abstraction.impl.execution.gt4_0_0.GlobusSecurityContextImpl
modules/provider-gt4_0_0/resources/cog-provider.properties:alias=gt4.0.0:gt4
modules/provider-gt4_0_0/resources/cog-provider.properties:classloader.properties=classloader-gt4_0_0.properties
modules/provider-gt4_0_0/resources/cog-provider.properties:#classloader.boot=org.globus.cog.abstraction.impl.execution.gt4_0_0.Boot
modules/provider-gt4_0_0/resources/cog-provider.properties:sandbox.boot=org.globus.cog.abstraction.impl.execution.gt4_0_0.Boot
modules/provider-gt4_0_0/resources/cog-provider.properties:config.path=${user.home}/.globus/cog-provider-gt4_0_0/
modules/provider-gt4_0_0/resources/cog-provider.properties:globus.location=${user.home}/.globus/cog-provider-gt4_0_0/
modules/provider-gt4_0_0/resources/cog-provider.properties:server.webroot=${user.home}/.globus/cog-provider-gt4_0_0/
modules/provider-gt4_0_0/resources/cog-provider.properties:schema.location=${user.home}/.globus/cog-provider-gt4_0_0/share/schema/
modules/provider-gt4_0_0/resources/cog-provider.properties:SCHEMA_LOCATION=${user.home}/.globus/cog-provider-gt4_0_0/share/schema/
modules/provider-gt4_0_0/resources/cog-provider.properties:executionTaskHandler=org.globus.cog.abstraction.impl.execution.gt4_0_0ft.TaskHandlerImpl
modules/provider-gt4_0_0/resources/cog-provider.properties:securityContext=org.globus.cog.abstraction.impl.execution.gt4_0_0.GlobusSecurityContextImpl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/management/shutdown_bindings.wsdl share/schema/core/management/shutdown_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/management/shutdown_port_type.wsdl share/schema/core/management/shutdown_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/management/shutdown_service.wsdl share/schema/core/management/shutdown_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/notification/notification_consumer_bindings.wsdl share/schema/core/notification/notification_consumer_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/notification/notification_consumer_flattened.wsdl share/schema/core/notification/notification_consumer_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/notification/notification_consumer_service.wsdl share/schema/core/notification/notification_consumer_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/notification/subscription_manager_bindings.wsdl share/schema/core/notification/subscription_manager_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/notification/subscription_manager_flattened.wsdl share/schema/core/notification/subscription_manager_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/notification/subscription_manager_service.wsdl share/schema/core/notification/subscription_manager_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/registry/registry_bindings.wsdl share/schema/core/registry/registry_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/registry/registry_entry_bindings.wsdl share/schema/core/registry/registry_entry_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/registry/registry_entry_flattened.wsdl share/schema/core/registry/registry_entry_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/registry/registry_entry_service.wsdl share/schema/core/registry/registry_entry_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/registry/registry_flattened.wsdl share/schema/core/registry/registry_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/registry/registry_service.wsdl share/schema/core/registry/registry_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/authzService/authzService_bindings.wsdl share/schema/core/samples/authzService/authzService_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/authzService/authzService_flattened.wsdl share/schema/core/samples/authzService/authzService_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/authzService/authzService_service.wsdl share/schema/core/samples/authzService/authzService_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/counter/counter_bindings.wsdl share/schema/core/samples/counter/counter_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/counter/counter_port_type.wsdl share/schema/core/samples/counter/counter_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/counter/counter_service.wsdl share/schema/core/samples/counter/counter_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/management/management_bindings.wsdl share/schema/core/samples/management/management_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/management/management_flattened.wsdl share/schema/core/samples/management/management_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/samples/management/management_service.wsdl share/schema/core/samples/management/management_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/security/authorization/authz_bindings.wsdl share/schema/core/security/authorization/authz_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/security/authorization/authz_port_type.wsdl share/schema/core/security/authorization/authz_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/security/authorization/authz_service.wsdl share/schema/core/security/authorization/authz_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/security/secconv/secure_conversation_bindings.wsdl share/schema/core/security/secconv/secure_conversation_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/security/secconv/secure_conversation_port_type.wsdl share/schema/core/security/secconv/secure_conversation_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/security/secconv/secure_conversation_service.wsdl share/schema/core/security/secconv/secure_conversation_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/types/timestamp.wsdl share/schema/core/types/timestamp.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/core/types/timestamp.xsd share/schema/core/types/timestamp.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/delegationService/delegation_bindings.wsdl share/schema/delegationService/delegation_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/delegationService/delegation_factory_bindings.wsdl share/schema/delegationService/delegation_factory_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/delegationService/delegation_factory_flattened.wsdl share/schema/delegationService/delegation_factory_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/delegationService/delegation_factory_service.wsdl share/schema/delegationService/delegation_factory_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/delegationService/delegation_flattened.wsdl share/schema/delegationService/delegation_flattened.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/delegationService/delegation_service.wsdl share/schema/delegationService/delegation_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/file_system_map_config.wsdl share/schema/gram/file_system_map_config.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/file_system_map_config.xsd share/schema/gram/file_system_map_config.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/job_description.xsd share/schema/gram/job_description.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_executable_job_bindings.wsdl share/schema/gram/managed_executable_job_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_executable_job_data.xsd share/schema/gram/managed_executable_job_data.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_executable_job_port_type.wsdl share/schema/gram/managed_executable_job_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_executable_job_service.wsdl share/schema/gram/managed_executable_job_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_executable_job_state.wsdl share/schema/gram/managed_executable_job_state.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_bindings.wsdl share/schema/gram/managed_job_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_data.xsd share/schema/gram/managed_job_data.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_factory_bindings.wsdl share/schema/gram/managed_job_factory_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_factory_port_type.wsdl share/schema/gram/managed_job_factory_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_factory_service.wsdl share/schema/gram/managed_job_factory_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_faults.xsd share/schema/gram/managed_job_faults.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_port_type.wsdl share/schema/gram/managed_job_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_service.wsdl share/schema/gram/managed_job_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_types.wsdl share/schema/gram/managed_job_types.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_job_types.xsd share/schema/gram/managed_job_types.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_multi_job_bindings.wsdl share/schema/gram/managed_multi_job_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_multi_job_data.xsd share/schema/gram/managed_multi_job_data.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_multi_job_port_type.wsdl share/schema/gram/managed_multi_job_port_type.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_multi_job_service.wsdl share/schema/gram/managed_multi_job_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/gram/managed_multi_job_state.wsdl share/schema/gram/managed_multi_job_state.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/addressing/WS-Addressing.xsd share/schema/ws/addressing/WS-Addressing.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/xml.xsd share/schema/ws/xml.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/policy/policy.xsd share/schema/ws/policy/policy.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/policy/utility.xsd share/schema/ws/policy/utility.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/secconv/ws-secureconversation.xsd share/schema/ws/secconv/ws-secureconversation.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/security/oasis-200401-wss-wssecurity-secext-1.0.xsd share/schema/ws/security/oasis-200401-wss-wssecurity-secext-1.0.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/security/oasis-200401-wss-wssecurity-utility-1.0.xsd share/schema/ws/security/oasis-200401-wss-wssecurity-utility-1.0.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/security/xmldsig-core-schema.xsd share/schema/ws/security/xmldsig-core-schema.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/trust/ws-trust.wsdl share/schema/ws/trust/ws-trust.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/ws/trust/ws-trust.xsd share/schema/ws/trust/ws-trust.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/faults/WS-BaseFaults.wsdl share/schema/wsrf/faults/WS-BaseFaults.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/faults/WS-BaseFaults.xsd share/schema/wsrf/faults/WS-BaseFaults.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/lifetime/WS-ResourceLifetime.wsdl share/schema/wsrf/lifetime/WS-ResourceLifetime.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/lifetime/WS-ResourceLifetime.xsd share/schema/wsrf/lifetime/WS-ResourceLifetime.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/lifetime/WS-ResourceLifetime_bindings.wsdl share/schema/wsrf/lifetime/WS-ResourceLifetime_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/lifetime/WS-ResourceLifetime_service.wsdl share/schema/wsrf/lifetime/WS-ResourceLifetime_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/notification/WS-BaseN.wsdl share/schema/wsrf/notification/WS-BaseN.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/notification/WS-BaseN.xsd share/schema/wsrf/notification/WS-BaseN.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/notification/WS-BaseN_bindings.wsdl share/schema/wsrf/notification/WS-BaseN_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/notification/WS-BaseN_service.wsdl share/schema/wsrf/notification/WS-BaseN_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/notification/WS-Topics.xsd share/schema/wsrf/notification/WS-Topics.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/properties/WS-ResourceProperties.wsdl share/schema/wsrf/properties/WS-ResourceProperties.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/properties/WS-ResourceProperties.xsd share/schema/wsrf/properties/WS-ResourceProperties.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/properties/WS-ResourceProperties_bindings.wsdl share/schema/wsrf/properties/WS-ResourceProperties_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/properties/WS-ResourceProperties_service.wsdl share/schema/wsrf/properties/WS-ResourceProperties_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/servicegroup/WS-ServiceGroup.wsdl share/schema/wsrf/servicegroup/WS-ServiceGroup.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/servicegroup/WS-ServiceGroup.xsd share/schema/wsrf/servicegroup/WS-ServiceGroup.xsd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/servicegroup/WS-ServiceGroup_bindings.wsdl share/schema/wsrf/servicegroup/WS-ServiceGroup_bindings.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/share/schema/wsrf/servicegroup/WS-ServiceGroup_service.wsdl share/schema/wsrf/servicegroup/WS-ServiceGroup_service.wsdl
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/client-config.wsdd client-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_cas_service/registration.xml etc/globus_cas_service/registration.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_cas_service/casDbSchema/cas_pgsql_database_schema.sql etc/globus_cas_service/casDbSchema/cas_pgsql_database_schema.sql
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_cas_service/casDbSchema/cas_mysql_database_schema.sql etc/globus_cas_service/casDbSchema/cas_mysql_database_schema.sql
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_cas_service/security-config.xml etc/globus_cas_service/security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_cas_service/server-config.wsdd etc/globus_cas_service/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_cas_service/jndi-config.xml etc/globus_cas_service/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_delegation_service/service-security-config.xml etc/globus_delegation_service/service-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_delegation_service/factory-security-config.xml etc/globus_delegation_service/factory-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_delegation_service/server-config.wsdd etc/globus_delegation_service/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_delegation_service/jndi-config.xml etc/globus_delegation_service/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core/global_security_descriptor.xml etc/globus_wsrf_core/global_security_descriptor.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core/client-server-config.wsdd etc/globus_wsrf_core/client-server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core/client-jndi-config.xml etc/globus_wsrf_core/client-jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core/server-config.wsdd etc/globus_wsrf_core/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core/jndi-config.xml etc/globus_wsrf_core/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core_registry/server-config.wsdd etc/globus_wsrf_core_registry/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_core_registry/jndi-config.xml etc/globus_wsrf_core_registry/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_aggregator/example-aggregator-registration.xml etc/globus_wsrf_mds_aggregator/example-aggregator-registration.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_aggregator/client-server-config.wsdd etc/globus_wsrf_mds_aggregator/client-server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_aggregator/server-config.wsdd etc/globus_wsrf_mds_aggregator/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/hierarchy.xml etc/globus_wsrf_mds_index/hierarchy.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/entry-security-config.xml etc/globus_wsrf_mds_index/entry-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/index-security-config.xml etc/globus_wsrf_mds_index/index-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/factory-security-config.xml etc/globus_wsrf_mds_index/factory-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/downstream.xml etc/globus_wsrf_mds_index/downstream.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/upstream.xml etc/globus_wsrf_mds_index/upstream.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/server-config.wsdd etc/globus_wsrf_mds_index/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_index/jndi-config.xml etc/globus_wsrf_mds_index/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_trigger/server-config.wsdd etc/globus_wsrf_mds_trigger/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_mds_trigger/jndi-config.xml etc/globus_wsrf_mds_trigger/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_rft/registration.xml etc/globus_wsrf_rft/registration.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_rft/security-config.xml etc/globus_wsrf_rft/security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_rft/factory-security-config.xml etc/globus_wsrf_rft/factory-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_rft/server-config.wsdd etc/globus_wsrf_rft/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_rft/jndi-config.xml etc/globus_wsrf_rft/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_servicegroup/server-config.wsdd etc/globus_wsrf_servicegroup/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/globus_wsrf_servicegroup/jndi-config.xml etc/globus_wsrf_servicegroup/jndi-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/gram-service/managed-job-factory-security-config.xml etc/gram-service/managed-job-factory-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/gram-service/managed-job-security-config.xml etc/gram-service/managed-job-security-config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/gram-service/registration.xml etc/gram-service/registration.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/gram-service/server-config.wsdd etc/gram-service/server-config.wsdd
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/gram-service/globus_gram_fs_map_config.xml etc/gram-service/globus_gram_fs_map_config.xml
modules/provider-gt4_0_0/resources/config-gt4_0_0.index:config/gt4_0_0/etc/gram-service/jndi-config.xml etc/gram-service/jndi-config.xml
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/Boot.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/Boot.java: private static final String ProviderVersion = "gt4.0.0";
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/Boot.java: private static final String ProviderVersionU = "gt4_0_0";
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/DelegatedTaskHandlerFactory.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/GlobusSecurityContextImpl.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/JobSubmissionTaskHandler.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/JobSubmissionTaskHandler.java:// package org.globus.cog.abstraction.impl.execution.gt4_0_0;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/JobSubmissionTaskHandler.java: throw new IllegalSpecException("The gt4.0.0 provider does not support redirection");
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/JobSubmissionTaskHandler.java:  "The gt4.0.0 provider does not support local executables");
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0/TaskHandlerImpl.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/DelegatedTaskHandlerFactory.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0ft;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/JobSubmissionTaskHandler.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0ft;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/JobSubmissionTaskHandler.java:import org.globus.cog.abstraction.impl.execution.gt4_0_0.GlobusSecurityContextImpl;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/JobSubmissionTaskHandler.java: "The gt4.0.0ft provider does not support redirection");
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/JobSubmissionTaskHandler.java: "The gt4.0.0ft provider does not support file staging");
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/PollThread.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0ft;
modules/provider-gt4_0_0/src/org/globus/cog/abstraction/impl/execution/gt4_0_0ft/TaskHandlerImpl.java:package org.globus.cog.abstraction.impl.execution.gt4_0_0ft;
modules/testing/karajan/execution/00-direct.k:test("Execution: direct", ["gt4.0.0", "gt2", "gt3.0.2", "gt3.2.0", "gt3.2.1"], []
modules/testing/karajan/hosts.k: service(provider="gt4.0.0", type="execution", url="plussed.mcs.anl.gov:4012")
modules/testing/karajan/hosts.k: service(provider="gt4.0.0", type="execution", url="plussed.mcs.anl.gov:4022")
