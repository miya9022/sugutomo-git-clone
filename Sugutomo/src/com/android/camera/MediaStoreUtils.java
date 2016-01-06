package com.android.camera;
import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public final class MediaStoreUtils {
	public static final String EXTRA_OUTPUT_URI_NAME ="crop_image.png";
    private MediaStoreUtils() {
    }

    public static Intent getPickImageIntent(final Context context) {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, "Select picture");
    }
    public static Intent getCameraCaptureIntent(final Context context, final Uri uri) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return Intent.createChooser(intent, "Capture Image");
    }
}