package ru.mashinis.mlkit;

import android.location.GnssAntennaInfo;
import android.media.Image;

import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextRecognitionAnalyzer implements ImageAnalysis.Analyzer {

    private static final long THROTTLE_TIMEOUT_MS = 1000L;
    private OnDetectedTextUpdatedListener listener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TextRecognizer textRecognizer;

    public TextRecognitionAnalyzer(OnDetectedTextUpdatedListener listener) {
        this.textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        this.listener = listener;
    }

    @ExperimentalGetImage
    @Override
    public void analyze(ImageProxy imageProxy) {
        executorService.execute(() -> {
            try {
                Image mediaImage = imageProxy.getImage();
                if (mediaImage == null) {
                    imageProxy.close();
                    return;
                }

                InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                textRecognizer.process(inputImage)
                        .addOnSuccessListener(visionText -> {
                            if (visionText != null && !visionText.getText().isEmpty()) {
                                listener.onDetectedTextUpdated(visionText);
                            }
                        })
                        .addOnCompleteListener(task -> {
                            imageProxy.close();
                        });
            } catch (Exception e) {
                e.printStackTrace();
                imageProxy.close();
            }
        });
    }

    public interface OnDetectedTextUpdatedListener {
        void onDetectedTextUpdated(Text detectedText);
    }
}
