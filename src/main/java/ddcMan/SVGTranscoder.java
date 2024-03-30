package ddcMan;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;

import java.awt.image.BufferedImage;
import java.io.*;

public class SVGTranscoder extends ImageTranscoder {
    private BufferedImage image = null;

    @Override
    public BufferedImage createImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage readImage(InputStream stream, int w, int h) throws IOException {
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, (float) w);
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) h);
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        this.setTranscodingHints(hints);
        try {
            TranscoderInput input = new TranscoderInput(stream);
            this.transcode(input, null);
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }
        stream.close();
        return getImage();
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {

    }
}
