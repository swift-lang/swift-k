import(sys)
import(task)
import('swift-lib')

sites := swift:configProperty("sites.file")
TCFile := swift:configProperty("tc.file")

log(LOG:INFO, "Using sites file: {sites}")
if (!file:exists(sites)) {
	throw("Could not find sites file: {sites}")
}

log(LOG:INFO, "Using tc.data: {TCFile}")

scheduler("vds-adaptive", shareID = "swift:scheduler:{sites}"
	property("transformationCatalogFile", TCFile)
	property("clusteringEnabled", swift:configProperty("clustering.enabled"))
	property("clusteringQueueDelay", swift:configProperty("clustering.queue.delay"))
	property("clusteringMinTime", swift:configProperty("clustering.min.time"))
	
	property("hostSubmitThrottle", swift:configProperty("throttle.host.submit"))
	property("submitThrottle", swift:configProperty("throttle.submit"))
	property("jobsPerCpu", "off")
	property("maxTransfers", swift:configProperty("throttle.transfers"))
	property("maxFileOperations", swift:configProperty("throttle.file.operations"))
	property("jobThrottle", swift:configProperty("throttle.score.job.factor"))
	
	task:availableHandlers(type = "execution", includeAliases = true)
	task:availableHandlers(type = "file", includeAliases = true)
	
	resources = swift:siteCatalog(sites)
)
