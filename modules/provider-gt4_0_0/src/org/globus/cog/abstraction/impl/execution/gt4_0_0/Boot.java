/*
 * Created on Jul 23, 2004
 */
package org.globus.cog.abstraction.impl.execution.gt4_0_0;

import java.io.File;

import org.apache.axis.AxisProperties;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.BootUtil;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.common.CoGProperties;
import org.globus.wsrf.config.ContainerConfig;

public class Boot {

	private static Logger logger = Logger.getLogger(Boot.class);

	public static void boot() {
		logger.debug("Booting gt4.0.0");
		try {
			String configPath = AbstractionProperties.getProperties("gt4.0.0").getProperty(
					"config.path");
			String globusLocation = AbstractionProperties.getProperties("gt4.0.0").getProperty(
					"globus.location");
			System.setProperty("GLOBUS_LOCATION", globusLocation);
			System.setProperty("org.globus.wsrf.container.webroot", configPath);
			ContainerConfig.getConfig().setOption(ContainerConfig.WSRF_LOCATION, globusLocation);
			String ip = CoGProperties.getDefault().getIPAddress();
			if (ip != null) {
				ContainerConfig.getConfig().setOption(ContainerConfig.LOGICAL_HOST, ip);
				// ??
				CoGProperties.getDefault().setHostName(ip);
			}
			ContainerConfig.getConfig().setOption(ContainerConfig.INTERNAL_WEB_ROOT_PROPERTY,
					AbstractionProperties.getProperties("gt4.0.0").getProperty("server.webroot"));
			AxisProperties.setProperty(EngineConfigurationFactoryDefault.OPTION_CLIENT_CONFIG_FILE,
					configPath + File.separator + "client-config.wsdd");
			BootUtil.checkConfigDir(configPath, "config-gt4_0_0.index", Boot.class.getClassLoader());
		}
		catch (Exception e) {
			logger.error("Error booting gt4.0.0", e);
		}
		catch (Error e) {
			logger.fatal("Java Error caught; this is bad", e);
			throw e;
		}
	}
}