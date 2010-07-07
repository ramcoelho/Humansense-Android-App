//============================================================================
// Name        : ManifoldExperiment.cpp
// Author      :
// Version     :
// Copyright   : BSD
// Description : Constructs a time-delay embedding of the data, equipped with
//               a model of the dynamics using an approximation of the
//               one step transition matrix based on averaging the sample
//               dynamics of the k-nearest neighbours.
//============================================================================
/*
 * main.cpp
 *
 *  Created on: Jun 28, 2009
 *      Author: jordan
 */

#include <stdlib.h>
#include <time.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <ANN/ANN.h>
#include <Tisean/tsa.h>
#include <android/log.h>
#include "Utils.h"
#include "BuildTree.h"
#include "TDEModel.h"

void buildTree(const char* in_file, uint m, uint p, uint d) {
	srand((unsigned)time(NULL));
	TDEModel *tdeModel;
    Settings settings = { ULONG_MAX, 0, 0xff, 1, 1, 2, 2, NULL, NULL, NULL, 0, 0, 0, 0, 1 };
    char stin=0;
    uint i, j;

    settings.infile = (char*)in_file;
    check_alloc(settings.outfile=(char*)calloc(strlen(settings.infile)+5,sizeof(char)));
    strcpy(settings.outfile,settings.infile);
    strcat(settings.outfile,".dmp");
    // test_outfile(settings.outfile);

    settings.delay = d;
    settings.delayset = 1;
    settings.embdim = m;
    settings.embset = 1;
    settings.pcaembdim = p;
    settings.pcaembset = 1;
    settings.stdo = 0;

    tdeModel = new TDEModel(&settings);
    tdeModel->DumpTree(settings.outfile);
/*
    ANNpoint ap = tdeModel->getDataPoint(0);
    uint N = 1000;
    ANNpointArray pts = annAllocPts(N, settings.embdim);;
    tdeModel->simulateTrajectory(ap, pts, settings.embdim, N);

    // DUMP Manifold and Trajectory
    FILE* dump = fopen("/sdcard/hs/trajectory.csv","w");
    for (i = 0; i < N; i++) {
    	fprintf(dump, FLOAT_OUT, pts[i][0]);
    	for (j = 1; j < settings.embdim; j++) {
    		fprintf(dump, "\t");
    		fprintf(dump, FLOAT_OUT, pts[i][j]);
    	}
    	fprintf(dump, "\n");
    }
    fclose(dump);
    annDeallocPt(ap);
    delete [] pts;
*/
    delete tdeModel;
    annClose();

    if (settings.column != NULL) free(settings.column);
    if (settings.outfile != NULL) free(settings.outfile);
    return;
}
