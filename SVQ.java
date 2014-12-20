// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `for f in $(ls *.tiff); do convert $f $f.bmp; done`
// package image_test;

import java.io.File;

public class SVQ {
    public static void main(String[] args) {
        // get images
        double[][] images = BMPInterface.readAllBMPInDir("jaffe");
        int imglen = images[0].length;

            }
        }

        // save
        System.out.println(height + "x" + width + " - " + pixels.length);
        BMPInterface.writeBMP(pixels, "out.bmp");

        System.out.println("Done!");
    }
}

