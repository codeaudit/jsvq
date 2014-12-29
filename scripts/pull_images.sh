#!/bin/bash

orig="${HOME}/torcs_imgs"
dest="${HOME}/git/SVQ/torcs"

echo -e "Copying all images in '${orig}' to ${dest}.\n"

mkdir -p ${dest}

ls -d ${orig}/*/ |
while read dir; do
  ls ${dir}*.bmp |
    while read img; do
      cp -av ${img} ${dest}/$(basename ${img%.*}).$(basename ${dir}).bmp
    done
done
