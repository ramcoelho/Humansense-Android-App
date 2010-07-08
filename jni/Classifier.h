/*
 * Classifier.h
 *
 *  Created on: 2009-06-30
 *      Author: jfrank8
 */

#include <stdlib.h>
#include "ClassifyTrajectory.h"
#include <vector>
#include <ANN/ANN.h>

#ifndef CLASSIFIER_H_
#define CLASSIFIER_H_

class Classifier {

private:
	int numModels, windowSize;

public:
	Classifier(std::vector<NamedModel*> *models);
	virtual ~Classifier();

	void classifyAndSave(ANNcoord** data, ulong length, FILE *fout);
	void go(ANNcoord** data, ulong length, FILE *fout);
	CvMat* classify(ANNcoord** data, ulong length);

	// Computes a time delay embedding for the specified model.
	// length should be the number of "rows" that are expected,
	// not the length of the input.
	ANNcoord* getProjectedData(int model, ANNcoord* input, int length);
	int getNumModels();
	int getWindowSize();
	char* getModelNames();

	std::vector<NamedModel*> *models;

	CvMat **navg, **navg_next, **proj_next, **nn, **nnn;
};

#define MATCH_STEPS 32
#define NEIGHBOURS 4

#endif /* CLASSIFIER_H_ */