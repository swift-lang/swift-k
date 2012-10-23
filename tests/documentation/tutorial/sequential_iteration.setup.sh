#!/bin/bash
echo 'echo -n $(wc -c < $1) > $2' > /tmp/wcl
chmod +x /tmp/wcl
