package android.hardware.camera2.legacy;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.legacy.ParameterUtils;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.utils.ListUtils;
import android.hardware.camera2.utils.ParamsUtils;
import android.location.Location;
import android.os.BatteryStats;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class LegacyRequestMapper {
    private static final byte DEFAULT_JPEG_QUALITY = 85;
    private static final String TAG = "LegacyRequestMapper";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);

    public static void convertRequestMetadata(LegacyRequest legacyRequest) {
        String legacyMode;
        String modeToSet;
        CameraCharacteristics characteristics = legacyRequest.characteristics;
        CaptureRequest request = legacyRequest.captureRequest;
        Size previewSize = legacyRequest.previewSize;
        Camera.Parameters params = legacyRequest.parameters;
        Rect activeArray = (Rect) characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        ParameterUtils.ZoomData zoomData = ParameterUtils.convertScalerCropRegion(activeArray, (Rect) request.get(CaptureRequest.SCALER_CROP_REGION), previewSize, params);
        if (params.isZoomSupported()) {
            params.setZoom(zoomData.zoomIndex);
        } else if (VERBOSE) {
            Log.v(TAG, "convertRequestToMetadata - zoom is not supported");
        }
        int aberrationMode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, 1)).intValue();
        if (aberrationMode != 1) {
            Log.w(TAG, "convertRequestToMetadata - Ignoring unsupported colorCorrection.aberrationMode = " + aberrationMode);
        }
        Integer antiBandingMode = (Integer) request.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE);
        if (antiBandingMode != null) {
            legacyMode = convertAeAntiBandingModeToLegacy(antiBandingMode.intValue());
        } else {
            legacyMode = (String) ListUtils.listSelectFirstFrom(params.getSupportedAntibanding(), new String[]{"auto", "off", Camera.Parameters.ANTIBANDING_50HZ, Camera.Parameters.ANTIBANDING_60HZ});
        }
        if (legacyMode != null) {
            params.setAntibanding(legacyMode);
        }
        MeteringRectangle[] aeRegions = (MeteringRectangle[]) request.get(CaptureRequest.CONTROL_AE_REGIONS);
        if (request.get(CaptureRequest.CONTROL_AWB_REGIONS) != null) {
            Log.w(TAG, "convertRequestMetadata - control.awbRegions setting is not supported, ignoring value");
        }
        int maxNumMeteringAreas = params.getMaxNumMeteringAreas();
        List<Camera.Area> meteringAreaList = convertMeteringRegionsToLegacy(activeArray, zoomData, aeRegions, maxNumMeteringAreas, "AE");
        if (maxNumMeteringAreas > 0) {
            params.setMeteringAreas(meteringAreaList);
        }
        int maxNumFocusAreas = params.getMaxNumFocusAreas();
        List<Camera.Area> focusAreaList = convertMeteringRegionsToLegacy(activeArray, zoomData, (MeteringRectangle[]) request.get(CaptureRequest.CONTROL_AF_REGIONS), maxNumFocusAreas, "AF");
        if (maxNumFocusAreas > 0) {
            params.setFocusAreas(focusAreaList);
        }
        Range<Integer> aeFpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
        if (aeFpsRange != null) {
            int[] legacyFps = convertAeFpsRangeToLegacy(aeFpsRange);
            boolean supported = false;
            Iterator i$ = params.getSupportedPreviewFpsRange().iterator();
            while (true) {
                if (!i$.hasNext()) {
                    break;
                }
                int[] range = i$.next();
                if (legacyFps[0] == range[0] && legacyFps[1] == range[1]) {
                    supported = true;
                    break;
                }
            }
            if (supported) {
                params.setPreviewFpsRange(legacyFps[0], legacyFps[1]);
            } else {
                Log.w(TAG, "Unsupported FPS range set [" + legacyFps[0] + "," + legacyFps[1] + "]");
            }
        }
        int compensation = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0)).intValue();
        if (!((Range) characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)).contains(Integer.valueOf(compensation))) {
            Log.w(TAG, "convertRequestMetadata - control.aeExposureCompensation is out of range, ignoring value");
            compensation = 0;
        }
        params.setExposureCompensation(compensation);
        Boolean aeLock = (Boolean) getIfSupported(request, CaptureRequest.CONTROL_AE_LOCK, false, params.isAutoExposureLockSupported(), false);
        if (aeLock != null) {
            params.setAutoExposureLock(aeLock.booleanValue());
        }
        if (VERBOSE) {
            Log.v(TAG, "convertRequestToMetadata - control.aeLock set to " + aeLock);
        }
        mapAeAndFlashMode(request, params);
        int afMode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_AF_MODE, 0)).intValue();
        String focusMode = LegacyMetadataMapper.convertAfModeToLegacy(afMode, params.getSupportedFocusModes());
        if (focusMode != null) {
            params.setFocusMode(focusMode);
        }
        if (VERBOSE) {
            Log.v(TAG, "convertRequestToMetadata - control.afMode " + afMode + " mapped to " + focusMode);
        }
        Integer awbMode = (Integer) getIfSupported(request, CaptureRequest.CONTROL_AWB_MODE, 1, params.getSupportedWhiteBalance() != null, 1);
        String whiteBalanceMode = null;
        if (awbMode != null) {
            whiteBalanceMode = convertAwbModeToLegacy(awbMode.intValue());
            params.setWhiteBalance(whiteBalanceMode);
        }
        if (VERBOSE) {
            Log.v(TAG, "convertRequestToMetadata - control.awbMode " + awbMode + " mapped to " + whiteBalanceMode);
        }
        Boolean awbLock = (Boolean) getIfSupported(request, CaptureRequest.CONTROL_AWB_LOCK, false, params.isAutoWhiteBalanceLockSupported(), false);
        if (awbLock != null) {
            params.setAutoWhiteBalanceLock(awbLock.booleanValue());
        }
        int captureIntent = filterSupportedCaptureIntent(((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_CAPTURE_INTENT, 1)).intValue());
        params.setRecordingHint(captureIntent == 3 || captureIntent == 4);
        Integer stabMode = (Integer) getIfSupported(request, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, 0, params.isVideoStabilizationSupported(), 0);
        if (stabMode != null) {
            params.setVideoStabilization(stabMode.intValue() == 1);
        }
        boolean infinityFocusSupported = ListUtils.listContains(params.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_INFINITY);
        Float focusDistance = (Float) getIfSupported(request, CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(0.0f), infinityFocusSupported, Float.valueOf(0.0f));
        if (focusDistance == null || focusDistance.floatValue() != 0.0f) {
            Log.w(TAG, "convertRequestToMetadata - Ignoring android.lens.focusDistance " + infinityFocusSupported + ", only 0.0f is supported");
        }
        if (params.getSupportedSceneModes() != null) {
            int controlMode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_MODE, 1)).intValue();
            switch (controlMode) {
                case 1:
                    modeToSet = "auto";
                    break;
                case 2:
                    int sceneMode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_SCENE_MODE, 0)).intValue();
                    String legacySceneMode = LegacyMetadataMapper.convertSceneModeToLegacy(sceneMode);
                    if (legacySceneMode == null) {
                        modeToSet = "auto";
                        Log.w(TAG, "Skipping unknown requested scene mode: " + sceneMode);
                        break;
                    } else {
                        modeToSet = legacySceneMode;
                        break;
                    }
                default:
                    Log.w(TAG, "Control mode " + controlMode + " is unsupported, defaulting to AUTO");
                    modeToSet = "auto";
                    break;
            }
            params.setSceneMode(modeToSet);
        }
        if (params.getSupportedColorEffects() != null) {
            int effectMode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_EFFECT_MODE, 0)).intValue();
            String legacyEffectMode = LegacyMetadataMapper.convertEffectModeToLegacy(effectMode);
            if (legacyEffectMode != null) {
                params.setColorEffect(legacyEffectMode);
            } else {
                params.setColorEffect(Camera.Parameters.EFFECT_NONE);
                Log.w(TAG, "Skipping unknown requested effect mode: " + effectMode);
            }
        }
        int testPatternMode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.SENSOR_TEST_PATTERN_MODE, 0)).intValue();
        if (testPatternMode != 0) {
            Log.w(TAG, "convertRequestToMetadata - ignoring sensor.testPatternMode " + testPatternMode + "; only OFF is supported");
        }
        Location location = (Location) request.get(CaptureRequest.JPEG_GPS_LOCATION);
        if (location == null) {
            params.removeGpsData();
        } else if (checkForCompleteGpsData(location)) {
            params.setGpsAltitude(location.getAltitude());
            params.setGpsLatitude(location.getLatitude());
            params.setGpsLongitude(location.getLongitude());
            params.setGpsProcessingMethod(location.getProvider().toUpperCase());
            params.setGpsTimestamp(location.getTime());
        } else {
            Log.w(TAG, "Incomplete GPS parameters provided in location " + location);
        }
        Integer orientation = (Integer) request.get(CaptureRequest.JPEG_ORIENTATION);
        params.setRotation(((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.JPEG_ORIENTATION, Integer.valueOf(orientation == null ? 0 : orientation.intValue()))).intValue());
        params.setJpegQuality(((Byte) ParamsUtils.getOrDefault(request, CaptureRequest.JPEG_QUALITY, Byte.valueOf((byte) DEFAULT_JPEG_QUALITY))).byteValue() & BatteryStats.HistoryItem.CMD_NULL);
        params.setJpegThumbnailQuality(((Byte) ParamsUtils.getOrDefault(request, CaptureRequest.JPEG_THUMBNAIL_QUALITY, Byte.valueOf((byte) DEFAULT_JPEG_QUALITY))).byteValue() & BatteryStats.HistoryItem.CMD_NULL);
        List<Camera.Size> sizes = params.getSupportedJpegThumbnailSizes();
        if (sizes != null && sizes.size() > 0) {
            Size s = (Size) request.get(CaptureRequest.JPEG_THUMBNAIL_SIZE);
            boolean invalidSize = s == null ? false : !ParameterUtils.containsSize(sizes, s.getWidth(), s.getHeight());
            if (invalidSize) {
                Log.w(TAG, "Invalid JPEG thumbnail size set " + s + ", skipping thumbnail...");
            }
            if (s == null || invalidSize) {
                params.setJpegThumbnailSize(0, 0);
            } else {
                params.setJpegThumbnailSize(s.getWidth(), s.getHeight());
            }
        }
        int mode = ((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.NOISE_REDUCTION_MODE, 1)).intValue();
        if (mode != 1) {
            Log.w(TAG, "convertRequestToMetadata - Ignoring unsupported noiseReduction.mode = " + mode);
        }
    }

    private static boolean checkForCompleteGpsData(Location location) {
        return (location == null || location.getProvider() == null || location.getTime() == 0) ? false : true;
    }

    static int filterSupportedCaptureIntent(int captureIntent) {
        switch (captureIntent) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return captureIntent;
            case 5:
            case 6:
                Log.w(TAG, "Unsupported control.captureIntent value " + 1 + "; default to PREVIEW");
                break;
        }
        Log.w(TAG, "Unknown control.captureIntent value " + 1 + "; default to PREVIEW");
        return 1;
    }

    private static List<Camera.Area> convertMeteringRegionsToLegacy(Rect activeArray, ParameterUtils.ZoomData zoomData, MeteringRectangle[] meteringRegions, int maxNumMeteringAreas, String regionName) {
        if (meteringRegions != null && maxNumMeteringAreas > 0) {
            List<MeteringRectangle> meteringRectangleList = new ArrayList<>();
            for (MeteringRectangle rect : meteringRegions) {
                if (rect.getMeteringWeight() != 0) {
                    meteringRectangleList.add(rect);
                }
            }
            if (meteringRectangleList.size() == 0) {
                Log.w(TAG, "Only received metering rectangles with weight 0.");
                return Arrays.asList(ParameterUtils.CAMERA_AREA_DEFAULT);
            }
            int countMeteringAreas = Math.min(maxNumMeteringAreas, meteringRectangleList.size());
            List<Camera.Area> meteringAreaList = new ArrayList<>(countMeteringAreas);
            for (int i = 0; i < countMeteringAreas; i++) {
                meteringAreaList.add(ParameterUtils.convertMeteringRectangleToLegacy(activeArray, meteringRectangleList.get(i), zoomData).meteringArea);
            }
            if (maxNumMeteringAreas < meteringRectangleList.size()) {
                Log.w(TAG, "convertMeteringRegionsToLegacy - Too many requested " + regionName + " regions, ignoring all beyond the first " + maxNumMeteringAreas);
            }
            if (!VERBOSE) {
                return meteringAreaList;
            }
            Log.v(TAG, "convertMeteringRegionsToLegacy - " + regionName + " areas = " + ParameterUtils.stringFromAreaList(meteringAreaList));
            return meteringAreaList;
        } else if (maxNumMeteringAreas <= 0) {
            return null;
        } else {
            return Arrays.asList(ParameterUtils.CAMERA_AREA_DEFAULT);
        }
    }

    private static void mapAeAndFlashMode(CaptureRequest r, Camera.Parameters p) {
        int flashMode = ((Integer) ParamsUtils.getOrDefault(r, CaptureRequest.FLASH_MODE, 0)).intValue();
        int aeMode = ((Integer) ParamsUtils.getOrDefault(r, CaptureRequest.CONTROL_AE_MODE, 1)).intValue();
        List<String> supportedFlashModes = p.getSupportedFlashModes();
        String flashModeSetting = null;
        if (ListUtils.listContains(supportedFlashModes, "off")) {
            flashModeSetting = "off";
        }
        if (aeMode == 1) {
            if (flashMode == 2) {
                if (ListUtils.listContains(supportedFlashModes, Camera.Parameters.FLASH_MODE_TORCH)) {
                    flashModeSetting = Camera.Parameters.FLASH_MODE_TORCH;
                } else {
                    Log.w(TAG, "mapAeAndFlashMode - Ignore flash.mode == TORCH;camera does not support it");
                }
            } else if (flashMode == 1) {
                if (ListUtils.listContains(supportedFlashModes, Camera.Parameters.FLASH_MODE_ON)) {
                    flashModeSetting = Camera.Parameters.FLASH_MODE_ON;
                } else {
                    Log.w(TAG, "mapAeAndFlashMode - Ignore flash.mode == SINGLE;camera does not support it");
                }
            }
        } else if (aeMode == 3) {
            if (ListUtils.listContains(supportedFlashModes, Camera.Parameters.FLASH_MODE_ON)) {
                flashModeSetting = Camera.Parameters.FLASH_MODE_ON;
            } else {
                Log.w(TAG, "mapAeAndFlashMode - Ignore control.aeMode == ON_ALWAYS_FLASH;camera does not support it");
            }
        } else if (aeMode == 2) {
            if (ListUtils.listContains(supportedFlashModes, "auto")) {
                flashModeSetting = "auto";
            } else {
                Log.w(TAG, "mapAeAndFlashMode - Ignore control.aeMode == ON_AUTO_FLASH;camera does not support it");
            }
        } else if (aeMode == 4) {
            if (ListUtils.listContains(supportedFlashModes, Camera.Parameters.FLASH_MODE_RED_EYE)) {
                flashModeSetting = Camera.Parameters.FLASH_MODE_RED_EYE;
            } else {
                Log.w(TAG, "mapAeAndFlashMode - Ignore control.aeMode == ON_AUTO_FLASH_REDEYE;camera does not support it");
            }
        }
        if (flashModeSetting != null) {
            p.setFlashMode(flashModeSetting);
        }
        if (VERBOSE) {
            Log.v(TAG, "mapAeAndFlashMode - set flash.mode (api1) to " + flashModeSetting + ", requested (api2) " + flashMode + ", supported (api1) " + ListUtils.listToString(supportedFlashModes));
        }
    }

    private static String convertAeAntiBandingModeToLegacy(int mode) {
        switch (mode) {
            case 0:
                return "off";
            case 1:
                return Camera.Parameters.ANTIBANDING_50HZ;
            case 2:
                return Camera.Parameters.ANTIBANDING_60HZ;
            case 3:
                return "auto";
            default:
                return null;
        }
    }

    private static int[] convertAeFpsRangeToLegacy(Range<Integer> fpsRange) {
        return new int[]{fpsRange.getLower().intValue(), fpsRange.getUpper().intValue()};
    }

    private static String convertAwbModeToLegacy(int mode) {
        switch (mode) {
            case 1:
                return "auto";
            case 2:
                return Camera.Parameters.WHITE_BALANCE_INCANDESCENT;
            case 3:
                return Camera.Parameters.WHITE_BALANCE_FLUORESCENT;
            case 4:
                return Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT;
            case 5:
                return Camera.Parameters.WHITE_BALANCE_DAYLIGHT;
            case 6:
                return Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT;
            case 7:
                return Camera.Parameters.WHITE_BALANCE_TWILIGHT;
            case 8:
                return Camera.Parameters.WHITE_BALANCE_SHADE;
            default:
                Log.w(TAG, "convertAwbModeToLegacy - unrecognized control.awbMode" + mode);
                return "auto";
        }
    }

    private static <T> T getIfSupported(CaptureRequest r, CaptureRequest.Key<T> key, T defaultValue, boolean isSupported, T allowedValue) {
        T val = (T) ParamsUtils.getOrDefault(r, key, defaultValue);
        if (isSupported) {
            return val;
        }
        if (!Objects.equals(val, allowedValue)) {
            Log.w(TAG, key.getName() + " is not supported; ignoring requested value " + ((Object) val));
        }
        return null;
    }
}
