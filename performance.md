
## Note

I'm trying to try different algorithm and see how they fare.
Of course that's no objective comparison, given the present conditions, but still it gives me an idea of what's going on (and whether a method is working at all).

## Measure

I'm reporting here the total average reconstruction error on 4 images, always the same, after training the SVQ once on every image, always in the same order.

- simpleDotProduct:
    - no untrain, dot:     112
    - untraining on least: 117
- shiftedDotProduct:
    - no untrain, dot:     188
    - untraining on least: 188
- squareError:
    - no untrain, dot:     139
    - untraining on least: 154
- simpleHistogram:
    - no untrain, dot:     126
    - untraining on least: 111
    - hik + untrain least: 120
    - hik, no untrain:     124
- pyramidMatching:
    - no untrain, dot:     159
    - untraining on least: 94
    - hik + untrain least: 111
    - hik, no untrain:     115
- spacialPyramidMatching:
    - no untrain, dot:     121
    - untraining on least: 119
    - hik + untrain least: 118
    - hik, no untrain:     115
