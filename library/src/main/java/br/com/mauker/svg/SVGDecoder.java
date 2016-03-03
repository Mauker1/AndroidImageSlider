package br.com.mauker.svg;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes an SVG internal representation from an {@link InputStream}.
 */
public class SVGDecoder implements ResourceDecoder<InputStream, SVG> {

    private PreserveAspectRatio mode;

    public SVGDecoder() {
        mode = PreserveAspectRatio.LETTERBOX;
    }

    public SVGDecoder(PreserveAspectRatio mode) {
        this.mode = mode;
    }

    public Resource<SVG> decode(InputStream source, int width, int height) throws IOException {
        try {
            SVG svg = SVG.getFromInputStream(source);

            svg.setDocumentWidth(width);
            svg.setDocumentHeight(height);
            svg.setDocumentPreserveAspectRatio(mode);

            return new SimpleResource<>(svg);
        } catch (SVGParseException ex) {
            throw new IOException("Cannot load SVG from stream.", ex);
        }
    }

    @Override
    public String getId() {
        return "SvgDecoder.org.saitestore.decoder";
    }
}