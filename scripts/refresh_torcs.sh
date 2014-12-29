#!/bin/bash

cd ${HOME}/git/SVQ
scripts/delete_bad.sh
echo
rm torcs/*.bmp
echo
scripts/pull_images.sh
echo
ant
