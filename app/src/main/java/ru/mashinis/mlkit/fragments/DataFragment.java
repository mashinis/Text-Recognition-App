package ru.mashinis.mlkit.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.mlkit.vision.text.Text;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.mashinis.mlkit.LocationTracker;
import ru.mashinis.mlkit.Milestone;
import ru.mashinis.mlkit.R;
import ru.mashinis.mlkit.TextRecognitionAnalyzer;

public class DataFragment extends Fragment implements TextRecognitionAnalyzer.OnDetectedTextUpdatedListener {

    private static final String ARG_CURRENT_KILOMETER = "current_kilometer";
    private static final String ARG_PATH_NUMBER = "path_number";
    private static final String ARG_IS_POSITIVE = "is_positive";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private int currentKilometer;
    private String pathNumber;
    private boolean isPositive;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private TextRecognitionAnalyzer textRecognitionAnalyzer;
    private CameraSelector cameraSelector;
    private Preview preview;
    private TextView bottomLeftTextView;
    private LocationTracker locationTracker;
    private TextView gpsTextView;
    private List<Milestone> milestones;
    private double latitude, longitude, altitude;
    private long timestamp;

    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance(int currentKilometer, String pathNumber, boolean isPositive) {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_KILOMETER, currentKilometer);
        args.putString(ARG_PATH_NUMBER, pathNumber);
        args.putBoolean(ARG_IS_POSITIVE, isPositive);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textRecognitionAnalyzer = new TextRecognitionAnalyzer(this);
        milestones = new ArrayList<>();

        if (getArguments() != null) {
            currentKilometer = getArguments().getInt(ARG_CURRENT_KILOMETER);
            pathNumber = getArguments().getString(ARG_PATH_NUMBER);
            isPositive = getArguments().getBoolean(ARG_IS_POSITIVE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);
        previewView = view.findViewById(R.id.cameraPreviewView);

        // Оставить экран включенным
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        // Скрыть нижнюю строку навигации (NavigationBar)
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        bottomLeftTextView = view.findViewById(R.id.bottomLeftTextView);
        // Установка начального значения из переменной currentKilometer в bottomLeftTextView
        bottomLeftTextView.setText(String.valueOf(currentKilometer));

        gpsTextView = view.findViewById(R.id.gpsTextView); // Инициализация gpsTextView

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        // Инициализация locationTracker
        locationTracker = new LocationTracker(requireContext(), new LocationTracker.LocationListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude, long time, double altitude) {
                // Обновить текст в gpsTextView с полученными данными GPS
                updateGPSData(latitude, longitude, time, altitude);
            }
        });

        if (isLocationPermissionGranted()) {
            locationTracker.startLocationUpdates();
        } else {
            // Запрос разрешения на использование местоположения (GPS)
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        return view;
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Настройка Preview
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Выбор задней камеры
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

                // Получение CameraControl из Camera
                CameraControl cameraControl = camera.getCameraControl();

                // Настройка зума
                float zoomRatio = 2.0f; // Замените на желаемое значение зума
                cameraControl.setZoomRatio(zoomRatio);

                // Создание и запуск камеры
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

                // Настройка TextRecognitionAnalyzer
                startTextRecognitionAnalyzer(cameraControl);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void startTextRecognitionAnalyzer(CameraControl cameraControl) {
        // Получение ImageAnalysis из CameraControl
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        // Установка анализатора из TextRecognitionAnalyzer
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), new TextRecognitionAnalyzer(this));

        // Получение ProcessCameraProvider
        ProcessCameraProvider cameraProvider;
        try {
            cameraProvider = cameraProviderFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        // Привязка ImageAnalysis к жизненному циклу
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int previousKilometer = -1; // Переменная для хранения предыдущего километра

    @Override
    public void onDetectedTextUpdated(Text text) {
        // Обработка найденного текста внутри фрагмента
        Log.d("DataFragment", "Detected text: " + text);

        // Обновление текста в bottomLeftTextView
        String numericStr = processDetectedText(text);
        int numeric = getNextSequentialNumber(numericStr, isPositive);

        if (numeric != -1 && numeric != previousKilometer) {
            bottomLeftTextView.setText(String.valueOf(numeric));
            milestones.add(new Milestone(numeric, latitude, longitude, timestamp, altitude));
            previousKilometer = numeric; // Обновляем значение предыдущего километра
        }

        // Дополнительные действия по обновлению интерфейса или другие действия
    }


    private String processDetectedText(Text text) {
        String numeric = "";
        List<Text.TextBlock> textBlocks = text.getTextBlocks();
        for (Text.TextBlock textBlock : textBlocks) {
            List<Text.Line> lines = textBlock.getLines();
            for (Text.Line line : lines) {
                List<Text.Element> elements = line.getElements();
                for (Text.Element element : elements) {
                    String elementText = element.getText();
                    // Проверка, является ли текст числом
                    if (isNumeric(elementText)) {
                        numeric = elementText;
                        Log.d("Numeric Text", elementText);
                    }
                }
            }
        }

        return numeric;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d{4}");  // Проверка, содержит ли строка ровно 4 символа, все из которых являются числами
    }

    private int getNextSequentialNumber(String detectedNumberStr, boolean isPositive) {
        if (detectedNumberStr.isEmpty()) {
            return -1; // если строка пустая
        }

        try {
            int detectedNumber = Integer.parseInt(detectedNumberStr);

            // Проверяем, совпадает ли распознанное число с текущим километражом
            if (detectedNumber == currentKilometer) {
                // Если совпадает, увеличиваем текущий километраж на 1, если isPositive равен true,
                // в противном случае уменьшаем текущий километраж на 1
                currentKilometer += isPositive ? 1 : -1;
                return detectedNumber; // Возвращаем распознанное число
            } else {
                // Если распознанное число не совпадает с текущим километражом,
                // возвращаем текущий километраж без изменений
                return currentKilometer;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // Обработка ситуации, когда detectedNumberStr не может быть преобразована в число
            return currentKilometer; // Возвращаем текущее значение километража
        }
    }

    private void updateGPSData(double latitude, double longitude, long time, double altitude) {
        String gpsData = "Latitude: " + latitude + "\nLongitude: " + longitude + "\nTime: " + time + "\nAltitude: " + altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = time;
        this.altitude = altitude;

        gpsTextView.setText(gpsData);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationTracker != null) {
            locationTracker.startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationTracker != null) {
            locationTracker.stopLocationUpdates();
        }
        stopCamera();
        saveMilestonesToFile(); // Вызываем метод сохранения при закрытии приложения
    }

    private void stopCamera() {
        if (cameraProviderFuture != null) {
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, ContextCompat.getMainExecutor(requireContext()));
        }
    }

    private void saveMilestonesToFile() {
        try {
            // Получаем FileOutputStream для записи в файл "milestones.json" в папке "files"
            String fileName = "milestones_" + System.currentTimeMillis() + ".json";
            FileOutputStream fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            // Преобразуем список объектов Milestone в JSON и записываем в файл
            String json = new Gson().toJson(milestones);
            fos.write(json.getBytes());
            fos.close();
            Log.d("DataFragment", "File saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("DataFragment", "Error saving file: " + e.getMessage());
        }
    }
}
