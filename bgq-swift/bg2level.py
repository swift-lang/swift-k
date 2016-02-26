#!/usr/bin/env python

import socket
import os
import subprocess
import sys
import time
import random

import logging

def bootable(block_size, args):

    blocks=subprocess.check_output(["get-bootable-blocks", "--size", block_size, os.environ["COBALT_PARTNAME"]]).split()
    logging.info("blocks is %s", blocks)

    swift_job_slot=os.environ["SWIFT_JOB_SLOT"]
    logging.info("swift_job_slot is %s", swift_job_slot)

    block=blocks[int(swift_job_slot)]
    logging.info("block is %s", block)
    
    subprocess.call(["boot-block", "--block", block])

    sparg="runjob -p 16 --block " + block + " : " + args
    subprocess.call(sparg, shell=True)


def prebooted(subblock_size, args):
    
    shape=getshape()
    logging.info("shape is %s", shape)

    corners=subprocess.check_output(["get-corners.py", os.environ["COBALT_PARTNAME"], shape]).split()
    logging.info("corners is %s", corners)
    
    swift_job_slot=os.environ["SWIFT_JOB_SLOT"]
    logging.info("swift_job_slot is %s", swift_job_slot)

    corner=corners[int(swift_job_slot)]
    logging.info("corner is %s", corner)
    
    sparg="runjob --block " + os.environ["COBALT_PARTNAME"] + " --corner " + corner + " --shape " + shape + " -p 16 --np " + str(16*int(subblock_size)) + " : " + args
    logging.info("sparg is %s", sparg)
    subprocess.call(sparg, shell=True)
    

def mixed(subblock_size, args):
    
    shape=getshape()
    corners=[]

    swift_job_slot=os.environ["SWIFT_JOB_SLOT"]
    logging.info("swift_job_slot is %s", swift_job_slot)

    #step 0. decide weather to invoke the initiate process or not
    if not os.path.exists("/home/ketan/corners.txt"):
        with open("/home/ketan/corners.txt", "w") as cornersfile:
            logging.info("initiation not done, do it!")

            #step 1. get bootable blocks
            blocks=subprocess.check_output(["get-bootable-blocks", "--size", "128", os.environ["COBALT_PARTNAME"]]).split()
            logging.info("blocks are %s", blocks)

            #step 2. boot each of them
            for block in blocks:
                subprocess.call(["boot-block", "--block", block])
                logging.info ("Booted block %s", block)

            #step 3. partition each booted block to prebooted partitions
            for block in blocks:
                c=subprocess.check_output(["get-corners.py", block, shape]).split()
                logging.info("c is %s", c)
                for i in c:
                    corners.append((block,i))
            logging.info("corners are %s", corners)
    
            #write corners to file
            cornersfile.write("\n".join('%s %s' % x for x in corners))
    
        logging.info("corners is %s", corners)
        dibs=open("/home/ketan/dibs.txt", "w")
        dibs.close()

    else:

        while not os.path.exists("/home/ketan/dibs.txt"):
            time.sleep(random.randint(1, 9))
        #check if file is closed
        logging.info("corners exist, load'em up!")
        #load corners from cornersfile
        cornersfile=open("/home/ketan/corners.txt","r")

        for line in cornersfile:
            corners.append((line.split()[0], line.split()[1]))
    
        cornersfile.close()
        logging.info("corners is %s", corners)


    
    block=corners[int(swift_job_slot)][0]
    logging.info("block is %s", block)

    corner=corners[int(swift_job_slot)][1]
    logging.info("corner is %s", corner)

    sparg="runjob --block " + block + " --corner " + corner + " --shape " + shape + " -p 16 --np " + str(16*int(subblock_size)) + " : " + args
    logging.info("sparg is %s", sparg)

    subprocess.call(sparg, shell=True)


def setsysenv():
    # vesta and mira has different path than cetus
    mname=socket.gethostname()
    if "vesta" in mname or "mira" in mname:
        os.environ["PATH"] = os.environ["PATH"] + ":/soft/cobalt/bgq_hardware_mapper"
    else:
        os.environ["PATH"] = os.environ["PATH"] + ":/soft/cobalt/cetus/bgq_hardware_mapper"


def getshape():
    # Prepare shape based on subblock size provided by user in sites environment
    subblock_size = os.environ["SUBBLOCK_SIZE"]
    logging.info("subblock_size is %s", subblock_size)

    if subblock_size == "1":
        return "1x1x1x1x1"
    elif subblock_size == "2":
        return "1x1x2x1x1"
    elif subblock_size == "4":
        return "1x2x2x1x1"
    elif subblock_size == "8":
        return "1x2x2x2x1"
    elif subblock_size == "16":
        return "1x2x2x2x2"
    elif subblock_size == "32":
        return "2x2x2x2x2"
    elif subblock_size == "64":
        return "2x2x4x2x2"
    elif subblock_size == "128":
        return "2x4x4x2x2"
    elif subblock_size == "256":
        return "2x4x4x4x2"
    elif subblock_size == "512":
        return "2x4x4x4x4"
    else:
        return "err"


def main():
    
    logging.basicConfig(filename='log.log', level=logging.INFO)
    
    args=' '.join(sys.argv[1:])
    logging.info("args is %s", args)

    setsysenv()
    #prebooted(os.environ["SUBBLOCK_SIZE"], args)
    mixed(os.environ["SUBBLOCK_SIZE"], args)
    logging.info("value of SUBBLOCK_SIZE is %s", os.environ["SUBBLOCK_SIZE"])
            
    print "Runjob finished."


if __name__=="__main__":
    main()
