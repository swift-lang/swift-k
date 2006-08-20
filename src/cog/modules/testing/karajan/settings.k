import("sys.k")

global(COLOR:FAILED, "#ff4000")
global(COLOR:PASSED, "#ffffff")
global(COLOR:TIMEOUT, "#ffba00")

global(OUTPUT_DIR, "output")
	
global(PUBLISH, false)

global(PUBLISH_HOST, "wiggum.mcs.anl.gov")
global(PUBLISH_DIR, "public_html/testing2")

//global(TEST_TIMEOUT, 300000)
global(TEST_TIMEOUT, 30*1000)
	
global(TEST_FILE, "testfile")
global(TEST_FILE_DIR, user.home)
	
global(MAX_HISTORY_SIZE, 365)
	
global(COG_DIR, "{user.home}/work/cog-new/src/cog/modules/karajan/dist/karajan-0.31")
