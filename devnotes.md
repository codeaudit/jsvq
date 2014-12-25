
## Dev Notes

- Make centroids moving averages of its training?
- Threshold to add new centroids?
- Save and load centroids
- Refactor BMPLoader.java, it's getting messy

## Similarity measures
- Dot product between positive images (in [0,1]) matches only whiteness
- A completely white centroid would always be the best match for everything
- In ECDL12 we used images coded in [-1,1], meaning that both whites and blacks are matched
- Still I should look into histogram matching techniques instead
- I could quickly move to the [-1,1] and test a better match, just to make sure the rest is ok, then switch to histogram-based techniques, possibly spacially aware
- The histogram-based techniques could stay in Byte so I don't need discretization
- I should just test the reconstruction error function and close the branch, do to the next based on the [-1,1] space

### Matrix
- Could be useful to have space awareness in picture
- [Commons Math](http://commons.apache.org/proper/commons-math/apidocs/overview-summary.html)
- [JBLAS](http://mikiobraun.github.io/jblas/javadoc/index.html)
