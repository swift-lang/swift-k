
type Image {}
type Header {}
type Table {}
type DiffStruct {
	int cntr1;
	int cntr2;
	Image plus;
	Image minus;
	Image diff;
}

type TxtFile {}
type JPEG {};


( Image projectedImage, Image projectedArea ) mProjectPP ( Image rawImage, Header template ) {
	app {
		mProjectPP "-X" @rawImage @projectedImage @template;
	}
}

( Image projectedImages[], Image projectedAreas[] ) mProjectPPBatch ( Image rawImages[], Header template ) {
	foreach img, i in rawImages {
		Image projImg<regexp_mapper;source=@img,match=".*\\/(.*)",transform="proj_\\1">;
		Image areaImg<regexp_mapper;source=@projImg,match="(.*)\\.(.*)",transform="\\1_area.\\2">;
		( projImg, areaImg ) = mProjectPP ( img, template );
		projectedImages[i] = projImg;
		projectedAreas[i] = areaImg;
	}
}


( Table diffsTbl ) mOverlaps ( Table imagesTbl ) {
	app {
		mOverlaps @imagesTbl @diffsTbl;
	}
}


( Image diffImage, TxtFile statusFile ) mDiffFit ( Image projectedImage1, Image projectedArea1, Image projectedImage2, Image projectedArea2, Header template) {
	app {
		mDiffFit "-s" @statusFile @projectedImage1 @projectedImage2 @diffImage @template; 
	}
}

( Image diffImages[], TxtFile statusFiles[] ) mDiffFitBatch ( Table diffsTbl, Header template) {
	//read overlap image pairs from diffsTbl
	DiffStruct diffs[]<csv_mapper;file=@diffsTbl,skip=1,hdelim=" |">;

	foreach d, i in diffs {
		Image image1 = d.plus;
		Image area1<regexp_mapper;source=@image1,match="(.*)\\.(.*)",transform="\\1_area.\\2">;
		Image image2 = d.minus;
		Image area2<regexp_mapper;source=@image2,match="(.*)\\.(.*)",transform="\\1_area.\\2">;
		Image diffImg<fixed_mapper;file=@(d.diff)>;
		TxtFile statusFile<regexp_mapper;source=@diffImg,match="diff(.*)fits",transform="fit\\1txt">;

		( diffImg, statusFile ) = mDiffFit ( image1, area1, image2, area2, template );
		diffImages[i] = diffImg;
		statusFiles[i] = statusFile;
	}
}

( Table statusFilesTbl ) mStatTbl ( Table diffsTbl ) {
	app {
		mStatTbl @diffsTbl @statusFilesTbl;
	}
}

( Table fitsTbl ) mConcatFit ( Table statusFilesTbl, TxtFile statusFiles[], string statusDir ) {
	app {
		mConcatFit @statusFilesTbl @fitsTbl statusDir;
	}
}

( Table correctionsTbl ) mBgModel ( Table projectedImagesTbl, Table fitsTbl ) {
	app {
		mBgModel @projectedImagesTbl @fitsTbl @correctionsTbl;
	}
}

( Image correctedImage, Image correctedArea ) mBackground ( Image projectedImage, Image projectedArea, Table projectedImagesTbl, Table correctionsTbl ) {
	app {
		mBackground "-t" @projectedImage @correctedImage @projectedImagesTbl @correctionsTbl;
	}
}

( Image correctedImages[], Image correctedAreas[] ) mBackgroundBatch ( Image projectedImages[], Image projectedAreas[], Table projectedImagesTbl, Table correctionsTbl ) {
	foreach projImg, i in projectedImages {
		Image projArea = projectedAreas[i];
		Image corrImg<regexp_mapper;source=@projImg,match="proj_(.*)",transform="corr_\\1">;
		Image corrArea<regexp_mapper;source=@corrImg,match="(.*)\\.(.*)",transform="\\1_area.\\2">;
		( corrImg, corrArea ) = mBackground ( projImg, projArea, projectedImagesTbl, correctionsTbl );
		correctedImages[i] = corrImg;
		correctedAreas[i] = corrArea;
	}
}

( Table imagesTbl ) mImgtbl ( string imageDir, Image images[] ) {
	app {
		mImgtbl imageDir @imagesTbl;
	}
}

( Table newImagesTbl ) mImgtbl_t ( string imageDir, Image images[], Table oldImagesTbl ) {
	app {
		mImgtbl imageDir "-t" @oldImagesTbl @newImagesTbl;
	}
}

( Image mosaic, Image mosaicArea ) mAdd ( Table imagesTbl, Header template, Image images[], Image imageAreas[] ) {
	app {
		mAdd "-e" @imagesTbl @template @mosaic;
	}
}

( Image shrunkImage ) mShrink ( Image image, float factor ) {
	app {
		mShrink @image @shrunkImage factor;
	}
}

( JPEG jpeg ) mJPEG ( Image image ) {
	app {
		mJPEG "-ct" 1 
		      "-gray" @image 
		      "-1.5s" "60s" "gaussian"
		      "-out" @jpeg;
	}
}

// get raw images
Image rawImages[]<dir_mapper;location="rawdir", suffix=".fits">;

// template header file
Header template<"template.hdr">;

// fast project raw images
Image projectedImages[], projectedAreas[];
( projectedImages, projectedAreas ) = mProjectPPBatch ( rawImages, template );

// table of projected images
Table projImgTbl<"projImg.tbl">;
projImgTbl = mImgtbl ( ".", projectedImages );

// table of overlapping images
Table diffsTbl<"diffs.tbl">;
diffsTbl = mOverlaps ( projImgTbl );

Image diffImgs[];
TxtFile statusFiles[];
( diffImgs, statusFiles ) = mDiffFitBatch ( diffsTbl, template );

Table statusFilesTbl<"statfile.tbl">;
statusFilesTbl = mStatTbl ( diffsTbl );

// fit to plane
Table fitsTbl<"fits.tbl">;
fitsTbl = mConcatFit ( statusFilesTbl, statusFiles, "." );

// corrections
Table correctionsTbl<"corrections.tbl">;
correctionsTbl = mBgModel ( projImgTbl, fitsTbl );

// background adjustment
Image correctedImages[], correctedAreas[];
( correctedImages, correctedAreas ) = mBackgroundBatch ( projectedImages, projectedAreas, projImgTbl, correctionsTbl );

// table of corrected images
Table corrImgTbl<"corrImg.tbl">;
corrImgTbl = mImgtbl ( ".", correctedImages );

// generate mosaic
Image mosaic<"mosaic.fits">;
Image mosaicArea<"mosaic_area.fits">;
( mosaic, mosaicArea ) = mAdd ( corrImgTbl, template, correctedImages, correctedAreas ); 

// shrink the image
Image smallMosaic<"smallMosaic.fits">;
smallMosaic = mShrink ( mosaic, 3.0 );

// convert to jpeg
JPEG jpeg<"mosaic.jpg">;
jpeg = mJPEG( smallMosaic );

