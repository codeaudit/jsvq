#!/bin/bash

mainfolder="${HOME}/torcs_imgs"
minfiles=5


echo "Deleting all folders in '${mainfolder}' with less than ${minfiles} files."
echo

ls -d ${mainfolder}/*/ |
while read dir; do
  echo "$(basename ${dir}/)"
  nfiles=$(ls $dir | wc -w)
  echo -en "\t${nfiles} files: "
  if [[ $nfiles -lt $minfiles ]]; then
    echo "DELETE"
    rm -rf $dir
  else
    echo "KEEP"
  fi
done
