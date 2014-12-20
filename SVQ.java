// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `for f in $(ls *.tiff); do convert $f $f.bmp; done`
// package image_test;

public class SVQ {
    public static void main(String[] args) {
        // get image
        double[] pixels = BMPInterface.readBMP("jaffe/KA.AN1.39.tiff.bmp");
        int width = BMPInterface.WIDTH;
        int height = BMPInterface.HEIGHT;

        // edit
        for (int i=0; i<pixels.length; i++) {
            // salt and pepper mask
            if ((i/width)%2==0) {
                pixels[i] = i%2;
            }
        }

        // save
        System.out.println(height + "x" + width + " - " + pixels.length);
        BMPInterface.writeBMP(pixels, "out.bmp");

        System.out.println("Done!");
    }
}

