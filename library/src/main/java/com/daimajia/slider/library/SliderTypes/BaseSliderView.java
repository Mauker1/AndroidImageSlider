package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.daimajia.slider.library.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
import java.io.InputStream;

import br.com.mauker.svg.SVGDecoder;
import br.com.mauker.svg.SvgDrawableTranscoder;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * I provide two example: {@link com.daimajia.slider.library.SliderTypes.DefaultSliderView} and
 * {@link com.daimajia.slider.library.SliderTypes.TextSliderView}
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;

    /**
     * Error place holder image.
     */
    private int mErrorPlaceHolderRes;

    private Drawable mErrorPlaceHolder = null;


    /**
     * Empty imageView placeholder.
     */
    private int mEmptyPlaceHolderRes;

    private String mUrl;

    private String mSmallImageUrl;

    protected OnSliderClickListener mOnSliderClickListener;

    private boolean mErrorDisappear;

    private ImageLoadListener mLoadListener;

    private String mDescription;

    /**
     * Scale type of the image.
     */
    private ScaleType mScaleType = ScaleType.Fit;

    public enum ScaleType{
        CenterCrop, CenterInside, Fit, FitCenterCrop
    }

    private boolean isSVG = false;

    protected BaseSliderView(Context context) {
        mContext = context;
    }

    /**
     * the placeholder image when loading image from url or file.
     * @param resId Image resource id
     * @return the updated SliderView.
     */
    public BaseSliderView empty(int resId){
        mEmptyPlaceHolderRes = resId;
        return this;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     * @param disappear
     * @return the updated SliderView.
     */
    public BaseSliderView errorDisappear(boolean disappear){
        mErrorDisappear = disappear;
        return this;
    }

    /**
     * if you set errorDisappear false, this will set a error placeholder image.
     * @param resId image resource id
     * @return the updated SliderView.
     */
    public BaseSliderView error(int resId){
        mErrorPlaceHolderRes = resId;
        return this;
    }

    /**
     *
     * @param d
     * @return
     */
    public BaseSliderView error(Drawable d) {
        mErrorPlaceHolder = d;
        return this;
    }

    /**
     * the description of a slider image.
     * @param description
     * @return the updated SliderView.
     */
    public BaseSliderView description(String description){
        mDescription = description;
        return this;
    }

    /**
     * Set a url as an image that is preparing to load.
     * @param url A Fresco compatible URI, as you can see on: http://frescolib.org/docs/supported-uris.html
     * @return the updated SliderView.
     */
    public BaseSliderView image(String url){
        mUrl = url;
        return this;
    }

    /**
     * Get a resource id, transform it to a Fresco compatible Uri and set it as the image to load.
     * @param resId Android Resource ID e.g.: R.drawable.yourDrawable
     * @return Returns a BaseSliderView with the image resource set.
     */
    public BaseSliderView image(Integer resId) {
        mUrl = "res:///" + resId;
        return this;
    }

    /**
     * Get a resource id, transform it to a Fresco compatible Uri and set it as the image to load.
     * @param file A File object.
     * @return Returns the BaseSliderView with the image resource set.
     * TODO - Check for null or valid files.
     */
    public BaseSliderView image(File file) {
        mUrl = Uri.fromFile(file).toString();
        Log.d("Slider",mUrl);
        return this;
    }

    /**
     * set a url as a image that preparing to load, http://frescolib.org/docs/supported-uris.html#_
     * @param smallImageUrl
     * @return the updated SliderView.
     */
    public BaseSliderView imageLowRes(String smallImageUrl){
        mSmallImageUrl = smallImageUrl;
        return this;
    }

    /**
     * lets users add a bundle of additional information
     * @param bundle
     * @return the updated SliderView.
     */
    public BaseSliderView bundle(Bundle bundle){
        mBundle = bundle;
        return this;
    }

    public BaseSliderView setSVG(boolean isSVG) {
        this.isSVG = isSVG;
        return this;
    }

    public String getUrl(){
        return mUrl;
    }

    public boolean isErrorDisappear(){
        return mErrorDisappear;
    }

    public int getEmpty(){
        return mEmptyPlaceHolderRes;
    }

    public int getError(){
        return mErrorPlaceHolderRes;
    }

    public Drawable getErrorDrawable() { return mErrorPlaceHolder; }

    public String getDescription(){
        return mDescription;
    }

    public Context getContext(){
        return mContext;
    }

    /**
     * set a slider image click listener
     * @param l
     * @return the updated SliderView.
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l){
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     * @param v the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShow(final View v, SimpleDraweeView targetImageView){
        if (targetImageView == null)
            return;

        final BaseSliderView me = this;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mOnSliderClickListener != null){
                mOnSliderClickListener.onSliderClick(me);
            }
            }
        });


        if (mLoadListener != null) {
            mLoadListener.onStart(me);
        }

        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if(v.findViewById(R.id.loading_bar) != null){
                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                if(v.findViewById(R.id.loading_bar) != null){
                    Log.d("lib", "Failed to load " + id);
                    Log.w("lib", throwable.getMessage());
                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                }
            }
        };

        GenericDraweeHierarchy hierarchy = targetImageView.getHierarchy();

        DraweeController controller = null;
        if(mUrl!=null && mSmallImageUrl == null) {
            controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener)
                    .setImageRequest(ImageRequest.fromUri(mUrl))
                    .build();
        }
        else if(mUrl!=null) {
            controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener)
                    .setLowResImageRequest(ImageRequest.fromUri(mSmallImageUrl))
                    .setImageRequest(ImageRequest.fromUri(mUrl))
                    .build();
        }
        else {
            return;
        }

        switch (mScaleType){
            case Fit:
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
                break;
            case FitCenterCrop: 
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                break;
            case CenterCrop:
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                break;
            case CenterInside:
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
                break;
        }

        // If an empty placeholder is set, use it.
        if(getEmpty() !=  0) {
            hierarchy.setPlaceholderImage(getEmpty());
        }

        Drawable d = getErrorDrawable();

        if (d != null) {
            hierarchy.setFailureImage(d);
        }

        targetImageView.setHierarchy(hierarchy);
        targetImageView.setController(controller);
   }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     * @param v the whole view
     * @param target The ImageView where you want to place the image
     */
    protected void bindEventAndShow(final View v, ImageView target) {
        if (target == null) {
            return;
        }

        final BaseSliderView me = this;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(me);
                }
            }
        });

        if (mLoadListener != null) {
            mLoadListener.onStart(me);
        }

        GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;
        SVGDecoder decoder;

        RequestListener<Object,PictureDrawable> rl = new RequestListener<Object, PictureDrawable>() {
            @Override
            public boolean onException(Exception e, Object model, Target<PictureDrawable> target, boolean isFirstResource) {
                ImageView view = ((ImageViewTarget<?>) target).getView();
                if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
                    view.setLayerType(ImageView.LAYER_TYPE_NONE, null);
                }

                if (mLoadListener != null) {
                    mLoadListener.onEnd(false, me);
                }
                if (v.findViewById(R.id.loading_bar) != null) {
                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                }

                return false;
            }

            @Override
            public boolean onResourceReady(PictureDrawable resource, Object model, Target<PictureDrawable> target,
                                           boolean isFromMemoryCache, boolean isFirstResource) {
                ImageView view = ((ImageViewTarget<?>) target).getView();
                if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
                    view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
                }

                if (v.findViewById(R.id.loading_bar) != null) {
                    v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                }

                return false;
            }
        };

        switch (mScaleType) {
            case Fit:
                decoder = new SVGDecoder(PreserveAspectRatio.STRETCH);
                break;
            case CenterCrop:
                decoder = new SVGDecoder(PreserveAspectRatio.FULLSCREEN);
                break;
            case FitCenterCrop:
                decoder = new SVGDecoder(PreserveAspectRatio.FULLSCREEN_START);
                break;
            case CenterInside:
                decoder = new SVGDecoder(PreserveAspectRatio.TOP);
                break;
            default:
                decoder = new SVGDecoder();
                break;
        }

        requestBuilder = Glide.with(mContext)
                .using(Glide.buildStreamModelLoader(Uri.class, mContext), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<>(decoder))
                .decoder(decoder)
                .animate(android.R.anim.fade_in)
                .listener(rl);

        if (getEmpty() != 0) {
            requestBuilder.placeholder(getEmpty());
        }

        if (getError() != 0) {
            requestBuilder.error(getError());
        }

        if (mUrl != null) {
            requestBuilder = requestBuilder.load(Uri.parse(mUrl));
        } else {
            return;
        }

        requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(target);
    }

    public BaseSliderView setScaleType(ScaleType type){
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType(){
        return mScaleType;
    }

    public boolean isSVG() {
        return isSVG;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     * @return The View.
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     * @param l
     */
    public void setOnImageLoadListener(ImageLoadListener l){
        mLoadListener = l;
    }

    public interface OnSliderClickListener {
        void onSliderClick(BaseSliderView slider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     * @return The Bundle with the extra info for the slider.
     */
    public Bundle getBundle(){
        return mBundle;
    }

    public interface ImageLoadListener{
        void onStart(BaseSliderView target);
        void onEnd(boolean result,BaseSliderView target);
    }
}
