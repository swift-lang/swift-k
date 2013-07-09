#!/bin/bash

export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu

swift -tc.file tc.template.data \
      -sites.file sites.template.xml  \
      -config swift.properties  \
      sanity.swift
