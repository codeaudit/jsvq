# Java implementation of Sparse Vector Quantization

Just a quick version of VQ in Java tested in isolation, to be imported in another project.
I want it to use Sparse Code rules to train the centroids and to code inputs.
The objective is Compressed Sensing, with online training to retain trace of what passes through the compressor.

Stay tuned.

### SVQ Autotrain notes
USE CASE: an individual codes images with SVQ. SVQ holds a unique TrainingSet, which in turn differenciates the images coming from different individuals, even in parallel. Each time an image gets coded, the SVQ also tries to add it to the TS. Only the `SVQ.nImgsPerImport` images with poorest reconstruction (lowest best-similarity against the set of centroids) per each individual are kept. When a population evaluation finishes, a call to `flush()` returns all the images added so far by all individuals (i.e. up to `nImgsPerImport*popsize` images per generation), and resets the state of the training set for the next generation. At this point, the SVQ will be trained on these images, and is ready for the next generation.
