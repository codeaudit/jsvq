import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BMPLoader {
    public int height=0;
    public int width=0;
    public String inputDir;
    public String outputDir;

    public BMPLoader(String in, String out) {
        this.inputDir = in;
        this.outputDir = out;
    }

    public short[] toShort(byte[] vec) {
        short[] ret = new short[vec.length];
        for (int i=0; i<vec.length; i++) {
            ret[i] = (short)(vec[i] & 0xFF);
        }
        return ret;
    }

    public byte[] toByte(short[] vec) {
        byte[] ret = new byte[vec.length];
        for (int i=0; i<vec.length; i++) {
            ret[i] = (byte)vec[i];
        }
        return ret;
    }

    public short[] readBMP(String path){
        return readBMP(new File(path));
    }

    // extract height/width initialization
    // extract ensureGrayscale
    // extract BufferedImage.TYPE_BYTE_GRAY
    public short[] readBMP(File file){
        // read image
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (height == 0 && width == 0) { // avoid multiple setting
            height = img.getHeight();
            width  = img.getWidth();
        }
        // make sure it's grayscale (one byte per pixel)
        if(img.getType()!=BufferedImage.TYPE_BYTE_GRAY) {
            // System.err.println("Converting to grayscale..."); // too verbose
            BufferedImage tmp = new BufferedImage(
                width, height, BufferedImage.TYPE_BYTE_GRAY);
            tmp.getGraphics().drawImage(img,0,0,null);
            img = tmp;
        }

        // return array of pixels
        byte[] pixels = new byte[width*height];
        img.getRaster().getDataElements(0,0,width,height,pixels);
        return toShort(pixels);
    }

    public void writeBMP(short[] pixels, String path) {
        // make image
        BufferedImage image = new BufferedImage(
            height, width,BufferedImage.TYPE_BYTE_GRAY);

        //set pixels
        image.getRaster().setDataElements(0,0,width,height,toByte(pixels));

        // write file
        try {
            ImageIO.write(image, "BMP", new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // The saving part is getting wild, refactor it

    public void saveAll(short[][] imgs, String basename, boolean rescale) {
        if (rescale) {
            for (int i=0; i<imgs.length; i++) {
                imgs[i] = rescale(imgs[i]);
            }
        }
        saveAll(imgs, basename);
    }

    public void saveAll(short[][] imgs, String basename) {
        System.out.println("Saving '"+basename+"'");
        writeBMPs(imgs, outputDir+"/"+basename);
    }

    public void save(short[] img, String basename) {
        writeBMP(img, outputDir+"/"+basename+".bmp");
    }

    public void writeBMPs(short[][] imgs, String basename) {
        for (int i=0; i<imgs.length; i++) {
            writeBMP(imgs[i], basename+"_"+(i+1)+".bmp");
        }
    }

    public File[] listBMP(){
        return listBMPInDir(inputDir);
    }

    public File[] listBMPInDir(String path){
        return new File(path).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bmp");
                }
            });
    }

    public short[][] readAll(int limit) {
        return readAllBMPInDir(inputDir, limit);
    }

    public short[][] readAll() {
        return readAllBMPInDir(inputDir);
    }

    public short[][] readAllBMPInDir(String path) {
        return readAllBMPInDir(path, 0);
    }

    public short[][] readAllBMPInDir(String path, int limit) {
        File[] files = listBMPInDir(path);
        if (limit<=0) { limit = files.length; }
        short[][] ret = new short[limit][];
        System.out.println("Loading all BMP files in path `"+path+"`");
        int i;
        for (i=0; i<limit; i++) {
            System.out.print(".");
            // System.out.println("Loading "+files[i]); // too verbose
            ret[i] = readBMP(files[i]);
        }
        System.out.println("\nLoaded "+i+" files.");
        return ret;
    }

    public short[] average(short[][] images) {
        int imglen = images[0].length;
        short[] avg = new short[imglen];
        for (int pixidx=0; pixidx<imglen; pixidx++) {
            avg[pixidx] = 0;
            for (int imgidx=0; imgidx<images.length; imgidx++) {
                avg[pixidx] += images[imgidx][pixidx];
            }
            avg[pixidx] /= images.length;
        }

        return avg;
    }

    // Rescales an array of values to range [0,1]
    public short[] rescale(short[] image) {
        short min=image[0],max=image[0];
        // find min/max
        for (int i=0; i<image.length; i++) {
            if (image[i]<min) { min=image[i]; }
            if (image[i]>max) { max=image[i]; }
        }
        // rescale
        short[] ret = new short[image.length];
        for (int i=0; i<image.length; i++) {
            ret[i] = (short)((image[i] - min) / (max - min));
        }

        return ret;
    }
}
