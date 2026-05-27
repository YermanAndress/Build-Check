package co.edu.uceva.buildcheck.modules.clasificador.service;

import ai.onnxruntime.*;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.*;

@Service
public class ClasificadorService {

    private OrtEnvironment env;
    private OrtSession session;

    private static final String MODEL_PATH = "/models/materiales_proyecto.onnx";
    private static final int HIST_SIZE = 65536; // 256 * 256

    @PostConstruct
    public void init() throws Exception {
        env = OrtEnvironment.getEnvironment();
        try (InputStream is = getClass().getResourceAsStream(MODEL_PATH)) {
            if (is == null) {
                throw new RuntimeException(
                    "Modelo ONNX no encontrado en " + MODEL_PATH +
                    ". Copia el .onnx a src/main/resources/models/"
                );
            }
            byte[] modelBytes = is.readAllBytes();
            session = env.createSession(modelBytes, new OrtSession.SessionOptions());
        }
        System.out.println("✅ Clasificador de materiales cargado correctamente.");
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
        } catch (OrtException e) {
            // ignorar al cerrar
        }
    }

    /**
     * Equivalente a extraer_hist() de Python:
     * calcHist sobre canales G y B, luego np.hstack
     */
    private float[] extraerHistograma(byte[] imageBytes) {
        byte[] copy = java.util.Arrays.copyOf(imageBytes, imageBytes.length);
        Mat buf = new Mat(1, copy.length, opencv_core.CV_8UC1);
        buf.data().put(copy, 0, copy.length);
        Mat img = opencv_imgcodecs.imdecode(buf, opencv_imgcodecs.IMREAD_COLOR);
        buf.close();
        if (img.empty()) {
            throw new IllegalArgumentException("No se pudo decodificar la imagen");
        }
        MatVector srcVec = new MatVector(img);
        Mat hist = new Mat();
        IntPointer channels = new IntPointer(1, 2);
        IntPointer histSize = new IntPointer(256, 256);
        FloatPointer ranges = new FloatPointer(0f, 256f, 0f, 256f);
        opencv_imgproc.calcHist(
            srcVec,
            channels,
            new Mat(),
            hist,
            histSize,
            ranges
        );

        float[] features = new float[HIST_SIZE];
        FloatIndexer idx = hist.createIndexer();
        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                features[i * 256 + j] = idx.get(i, j);
            }
        }
        idx.close();
        hist.close();
        img.close();
        srcVec.close();
        channels.close();
        histSize.close();
        ranges.close();
        return features;
    }

    /**
     * Clasifica una imagen y retorna predicción y confianza.
     */
    public Map<String, Object> clasificar(byte[] imageBytes) throws OrtException {
    float[] features = extraerHistograma(imageBytes);

    long[] shape = {1, HIST_SIZE};
    OnnxTensor inputTensor = OnnxTensor.createTensor(
        env, FloatBuffer.wrap(features), shape
    );

    String inputName = session.getInputNames().iterator().next();

    try (OrtSession.Result result = session.run(
            Collections.singletonMap(inputName, inputTensor))) {

        // Primera salida: label predicho
        Object rawLabel = result.get(0).getValue();
        String prediction;
        if (rawLabel instanceof String[]) {
            prediction = ((String[]) rawLabel)[0];
        } else if (rawLabel instanceof List) {
            prediction = ((List<?>) rawLabel).get(0).toString();
        } else {
            prediction = rawLabel.toString();
        }

        // Segunda salida: probabilidades como OnnxMap
        float confidence = 0f;
        OnnxValue probValue = result.get(1);

        if (probValue instanceof OnnxSequence) {
            // Es una secuencia de mapas {clase -> probabilidad}
            OnnxSequence seq = (OnnxSequence) probValue;
            List<?> seqList = (List<?>) seq.getValue();

            if (!seqList.isEmpty()) {
                Object first = seqList.get(0);
                if (first instanceof OnnxMap) {
                    // ✅ Leer OnnxMap correctamente
                    OnnxMap onnxMap = (OnnxMap) first;
                    Map<?, ?> probMap = (Map<?, ?>) onnxMap.getValue();
                    confidence = probMap.values().stream()
                        .map(v -> ((Number) v).floatValue())
                        .max(Float::compareTo)
                        .orElse(0f);
                } else if (first instanceof Map) {
                    Map<?, ?> probMap = (Map<?, ?>) first;
                    confidence = probMap.values().stream()
                        .map(v -> ((Number) v).floatValue())
                        .max(Float::compareTo)
                        .orElse(0f);
                }
            }
        } else if (probValue instanceof OnnxMap) {
            // Directamente un mapa
            OnnxMap onnxMap = (OnnxMap) probValue;
            Map<?, ?> probMap = (Map<?, ?>) onnxMap.getValue();
            confidence = probMap.values().stream()
                .map(v -> ((Number) v).floatValue())
                .max(Float::compareTo)
                .orElse(0f);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("prediction", prediction);
        response.put("confidence", Math.round(confidence * 10000.0) / 10000.0);
        return response;

    } finally {
        inputTensor.close();
    }
}

    /**
     * Clasifica y valida si coincide con el material esperado.
     */
    public Map<String, Object> validar(byte[] imageBytes, String materialEsperado)
            throws OrtException {
        Map<String, Object> clasificacion = clasificar(imageBytes);
        String prediccion = clasificacion.get("prediction").toString()
                .toLowerCase().strip();
        boolean coincide = prediccion.equals(materialEsperado.toLowerCase().strip());

        Map<String, Object> response = new HashMap<>();
        response.put("coincide", coincide);
        response.put("prediccion", clasificacion.get("prediction"));
        response.put("confianza", clasificacion.get("confidence"));
        response.put("materialEsperado", materialEsperado);
        return response;
    }
}