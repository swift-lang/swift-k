# !/bin/sh

if [ ! -d "$GLOBUS_LOCATION" ] then
		echo "Error: GLOBUS_LOCATION invalid or not set: $GLOBUS_LOCATION" 1>&2
		exit 1
fi

if [ "X$JAVA_HOME" = "X" ] then
		_RUNJAVA=java
else
		_RUNJAVA="$JAVA_HOME"/bin/java
fi

exec $_RUNJAVA -Daxis.ClientConfigFile="$GLOBUS_LOCATION"\client-config.wsdd -jar gui.jar