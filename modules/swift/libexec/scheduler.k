import(sys)
import(task)
import('swift-lib')

config := contextAttribute("SWIFT:CONFIG")

scheduler("vds-adaptive", shareID = "swift:scheduler"
	property("config", config)
	
	property("hostSubmitThrottle", swift:configProperty("hostJobSubmitThrottle"))
	property("submitThrottle", swift:configProperty("jobSubmitThrottle"))
	property("jobsPerCpu", "off")
	property("maxTransfers", swift:configProperty("fileTransfersThrottle"))
	property("maxFileOperations", swift:configProperty("fileOperationsThrottle"))
	property("jobThrottle", swift:configProperty("siteScoreThrottlingFactor"))
	
	task:availableHandlers(type = "execution", includeAliases = true)
	task:availableHandlers(type = "file", includeAliases = true)
	
	resources = swift:siteCatalog(config)
)
