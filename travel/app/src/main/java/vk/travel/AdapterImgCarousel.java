package vk.travel;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class AdapterImgCarousel extends PagerAdapter {

    private Context mCtx;
    private List<Bitmap> mImages;                                                                   // элементы для показа

    AdapterImgCarousel(Context context, List<Bitmap> carouselImages) {                                            // конструктор
        mCtx = context;
        mImages = carouselImages;
    }

    @Override
    public int getCount() {                                                                         // возвращает кол-во элементов для показа
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {                              // показать элемент

        ImageView imageView = new ImageView(mCtx);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(mImages.get(position));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {                     // скрыть элемент
        container.removeView((ImageView) object);
    }
}
