package android.filterpacks.imageproc;

import android.bluetooth.BluetoothClass;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.net.UrlQuerySanitizer;
import android.os.BatteryManager;
import android.provider.Downloads;
import android.util.DisplayMetrics;
import android.widget.SpellChecker;
import com.android.ims.ImsReasonInfo;
import com.android.internal.R;

public class AutoFixFilter extends Filter {
    private static final int[] normal_cdf = {9, 33, 50, 64, 75, 84, 92, 99, 106, 112, 117, 122, 126, 130, 134, 138, 142, 145, 148, 150, 154, 157, 159, 162, 164, 166, 169, 170, 173, 175, 177, 179, 180, 182, 184, 186, 188, 189, 190, 192, 194, 195, 197, 198, 199, 200, 202, 203, 205, 206, 207, 208, 209, 210, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 229, 230, 231, 232, 233, 234, 235, 236, 236, 237, 238, 239, 239, 240, 240, 242, 242, 243, 244, 245, 245, 246, 247, 247, 248, 249, 249, 250, 250, 251, 252, 253, 253, 254, 255, 255, 256, 256, 257, 258, 258, 259, 259, 259, 260, 261, 262, 262, 263, 263, 264, 264, 265, 265, 266, 267, 267, 268, 268, 269, 269, 269, 270, 270, 271, 272, 272, 273, 273, 274, 274, 275, 275, 276, 276, 277, 277, 277, 278, 278, 279, 279, 279, 280, 280, 281, 282, 282, 282, 283, 283, 284, 284, 285, 285, 285, 286, 286, 287, 287, 288, 288, 288, 289, 289, 289, 290, 290, 290, 291, R.styleable.Theme_preferenceListStyle, R.styleable.Theme_preferenceListStyle, R.styleable.Theme_preferenceListStyle, R.styleable.Theme_preferenceFragmentListStyle, R.styleable.Theme_preferenceFragmentListStyle, R.styleable.Theme_preferenceFragmentPaddingSide, R.styleable.Theme_preferenceFragmentPaddingSide, R.styleable.Theme_preferenceFragmentPaddingSide, R.styleable.Theme_seekBarPreferenceStyle, R.styleable.Theme_seekBarPreferenceStyle, R.styleable.Theme_dialogTitleIconsDecorLayout, R.styleable.Theme_dialogTitleIconsDecorLayout, R.styleable.Theme_dialogTitleIconsDecorLayout, R.styleable.Theme_dialogCustomTitleDecorLayout, R.styleable.Theme_dialogCustomTitleDecorLayout, R.styleable.Theme_dialogCustomTitleDecorLayout, R.styleable.Theme_dialogTitleDecorLayout, R.styleable.Theme_dialogTitleDecorLayout, R.styleable.Theme_dialogTitleDecorLayout, R.styleable.Theme_toastFrameBackground, R.styleable.Theme_toastFrameBackground, R.styleable.Theme_toastFrameBackground, R.styleable.Theme_toastFrameBackground, 300, 300, R.styleable.Theme_preferenceFrameLayoutStyle, R.styleable.Theme_preferenceFrameLayoutStyle, R.styleable.Theme_pointerStyle, R.styleable.Theme_pointerStyle, R.styleable.Theme_pointerStyle, R.styleable.Theme_accessibilityFocusedDrawable, R.styleable.Theme_accessibilityFocusedDrawable, R.styleable.Theme_findOnPageNextDrawable, R.styleable.Theme_findOnPageNextDrawable, R.styleable.Theme_findOnPageNextDrawable, R.styleable.Theme_findOnPagePreviousDrawable, R.styleable.Theme_findOnPagePreviousDrawable, R.styleable.Theme_findOnPagePreviousDrawable, R.styleable.Theme_colorSwitchThumbNormal, R.styleable.Theme_colorSwitchThumbNormal, R.styleable.Theme_colorSwitchThumbNormal, R.styleable.Theme_lightY, R.styleable.Theme_lightY, R.styleable.Theme_lightY, R.styleable.Theme_lightZ, R.styleable.Theme_lightZ, R.styleable.Theme_lightZ, R.styleable.Theme_lightRadius, R.styleable.Theme_lightRadius, R.styleable.Theme_lightRadius, R.styleable.Theme_lightRadius, 310, 310, 310, 310, 311, 312, 312, 312, 313, 313, 313, 314, 314, 314, 315, 315, 315, 315, 316, 316, 316, 317, 317, 317, 318, 318, 318, 319, 319, 319, 319, 319, 320, 320, 320, ImsReasonInfo.CODE_SIP_REDIRECTED, ImsReasonInfo.CODE_SIP_REDIRECTED, 322, 322, 322, 323, 323, 323, 323, 324, 324, 324, 325, 325, 325, 325, 326, 326, 326, 327, 327, 327, 327, 328, 328, 328, 329, 329, 329, 329, 329, 330, 330, 330, 330, ImsReasonInfo.CODE_SIP_BAD_REQUEST, ImsReasonInfo.CODE_SIP_BAD_REQUEST, ImsReasonInfo.CODE_SIP_FORBIDDEN, ImsReasonInfo.CODE_SIP_FORBIDDEN, ImsReasonInfo.CODE_SIP_FORBIDDEN, ImsReasonInfo.CODE_SIP_NOT_FOUND, ImsReasonInfo.CODE_SIP_NOT_FOUND, ImsReasonInfo.CODE_SIP_NOT_FOUND, ImsReasonInfo.CODE_SIP_NOT_FOUND, ImsReasonInfo.CODE_SIP_NOT_SUPPORTED, ImsReasonInfo.CODE_SIP_NOT_SUPPORTED, ImsReasonInfo.CODE_SIP_NOT_SUPPORTED, ImsReasonInfo.CODE_SIP_NOT_SUPPORTED, ImsReasonInfo.CODE_SIP_REQUEST_TIMEOUT, ImsReasonInfo.CODE_SIP_REQUEST_TIMEOUT, ImsReasonInfo.CODE_SIP_REQUEST_TIMEOUT, ImsReasonInfo.CODE_SIP_TEMPRARILY_UNAVAILABLE, ImsReasonInfo.CODE_SIP_TEMPRARILY_UNAVAILABLE, ImsReasonInfo.CODE_SIP_TEMPRARILY_UNAVAILABLE, ImsReasonInfo.CODE_SIP_TEMPRARILY_UNAVAILABLE, ImsReasonInfo.CODE_SIP_BAD_ADDRESS, ImsReasonInfo.CODE_SIP_BAD_ADDRESS, ImsReasonInfo.CODE_SIP_BAD_ADDRESS, ImsReasonInfo.CODE_SIP_BAD_ADDRESS, ImsReasonInfo.CODE_SIP_BUSY, ImsReasonInfo.CODE_SIP_BUSY, ImsReasonInfo.CODE_SIP_BUSY, ImsReasonInfo.CODE_SIP_REQUEST_CANCELLED, ImsReasonInfo.CODE_SIP_REQUEST_CANCELLED, ImsReasonInfo.CODE_SIP_REQUEST_CANCELLED, ImsReasonInfo.CODE_SIP_REQUEST_CANCELLED, ImsReasonInfo.CODE_SIP_REQUEST_CANCELLED, ImsReasonInfo.CODE_SIP_REQUEST_CANCELLED, ImsReasonInfo.CODE_SIP_NOT_ACCEPTABLE, ImsReasonInfo.CODE_SIP_NOT_ACCEPTABLE, ImsReasonInfo.CODE_SIP_NOT_ACCEPTABLE, ImsReasonInfo.CODE_SIP_NOT_ACCEPTABLE, ImsReasonInfo.CODE_SIP_NOT_REACHABLE, ImsReasonInfo.CODE_SIP_NOT_REACHABLE, ImsReasonInfo.CODE_SIP_CLIENT_ERROR, ImsReasonInfo.CODE_SIP_CLIENT_ERROR, ImsReasonInfo.CODE_SIP_CLIENT_ERROR, ImsReasonInfo.CODE_SIP_CLIENT_ERROR, 343, 343, 343, 344, 344, 344, 344, 345, 345, 345, 345, 346, 346, 346, 346, 347, 347, 347, 347, 348, 348, 348, 348, 349, 349, 349, 349, 349, 349, SpellChecker.WORD_ITERATOR_INTERVAL, SpellChecker.WORD_ITERATOR_INTERVAL, SpellChecker.WORD_ITERATOR_INTERVAL, SpellChecker.WORD_ITERATOR_INTERVAL, ImsReasonInfo.CODE_SIP_SERVER_INTERNAL_ERROR, ImsReasonInfo.CODE_SIP_SERVER_INTERNAL_ERROR, ImsReasonInfo.CODE_SIP_SERVICE_UNAVAILABLE, ImsReasonInfo.CODE_SIP_SERVICE_UNAVAILABLE, ImsReasonInfo.CODE_SIP_SERVICE_UNAVAILABLE, ImsReasonInfo.CODE_SIP_SERVICE_UNAVAILABLE, ImsReasonInfo.CODE_SIP_SERVER_TIMEOUT, ImsReasonInfo.CODE_SIP_SERVER_TIMEOUT, ImsReasonInfo.CODE_SIP_SERVER_TIMEOUT, ImsReasonInfo.CODE_SIP_SERVER_TIMEOUT, ImsReasonInfo.CODE_SIP_SERVER_ERROR, ImsReasonInfo.CODE_SIP_SERVER_ERROR, ImsReasonInfo.CODE_SIP_SERVER_ERROR, ImsReasonInfo.CODE_SIP_SERVER_ERROR, 355, 355, 355, 355, 356, 356, 356, 356, 357, 357, 357, 357, 358, 358, 358, 358, 359, 359, 359, 359, 359, 359, 359, 360, 360, 360, 360, ImsReasonInfo.CODE_SIP_USER_REJECTED, ImsReasonInfo.CODE_SIP_USER_REJECTED, ImsReasonInfo.CODE_SIP_GLOBAL_ERROR, ImsReasonInfo.CODE_SIP_GLOBAL_ERROR, ImsReasonInfo.CODE_SIP_GLOBAL_ERROR, ImsReasonInfo.CODE_SIP_GLOBAL_ERROR, 363, 363, 363, 363, 364, 364, 364, 364, 365, 365, 365, 365, 366, 366, 366, 366, 366, 367, 367, 367, 367, 368, 368, 368, 368, 369, 369, 369, 369, 369, 369, 370, 370, 370, 370, 370, 371, 371, 372, 372, 372, 372, 373, 373, 373, 373, 374, 374, 374, 374, 374, 375, 375, 375, 375, 376, 376, 376, 376, 377, 377, 377, 377, 378, 378, 378, 378, 378, 379, 379, 379, 379, 379, 379, 380, 380, 380, 380, 381, 381, 381, 382, 382, 382, 382, 383, 383, 383, 383, 384, 384, 384, 384, 385, 385, 385, 385, 385, 386, 386, 386, 386, 387, 387, 387, 387, 388, 388, 388, 388, 388, 389, 389, 389, 389, 389, 389, 390, 390, 390, 390, 391, 391, 392, 392, 392, 392, 392, 393, 393, 393, 393, 394, 394, 394, 394, 395, 395, 395, 395, 396, 396, 396, 396, 396, 397, 397, 397, 397, 398, 398, 398, 398, 399, 399, 399, 399, 399, 399, 400, 400, 400, 400, 400, ImsReasonInfo.CODE_MEDIA_INIT_FAILED, ImsReasonInfo.CODE_MEDIA_INIT_FAILED, ImsReasonInfo.CODE_MEDIA_NO_DATA, ImsReasonInfo.CODE_MEDIA_NO_DATA, ImsReasonInfo.CODE_MEDIA_NO_DATA, ImsReasonInfo.CODE_MEDIA_NO_DATA, ImsReasonInfo.CODE_MEDIA_NOT_ACCEPTABLE, ImsReasonInfo.CODE_MEDIA_NOT_ACCEPTABLE, ImsReasonInfo.CODE_MEDIA_NOT_ACCEPTABLE, ImsReasonInfo.CODE_MEDIA_NOT_ACCEPTABLE, 404, 404, 404, 404, UrlQuerySanitizer.IllegalCharacterValueSanitizer.URL_AND_SPACE_LEGAL, UrlQuerySanitizer.IllegalCharacterValueSanitizer.URL_AND_SPACE_LEGAL, UrlQuerySanitizer.IllegalCharacterValueSanitizer.URL_AND_SPACE_LEGAL, UrlQuerySanitizer.IllegalCharacterValueSanitizer.URL_AND_SPACE_LEGAL, Downloads.Impl.STATUS_NOT_ACCEPTABLE, Downloads.Impl.STATUS_NOT_ACCEPTABLE, Downloads.Impl.STATUS_NOT_ACCEPTABLE, Downloads.Impl.STATUS_NOT_ACCEPTABLE, Downloads.Impl.STATUS_NOT_ACCEPTABLE, 407, 407, 407, 407, 408, 408, 408, 408, 409, 409, 409, 409, 409, 409, 410, 410, 410, 410, Downloads.Impl.STATUS_LENGTH_REQUIRED, Downloads.Impl.STATUS_LENGTH_REQUIRED, Downloads.Impl.STATUS_PRECONDITION_FAILED, Downloads.Impl.STATUS_PRECONDITION_FAILED, Downloads.Impl.STATUS_PRECONDITION_FAILED, Downloads.Impl.STATUS_PRECONDITION_FAILED, 413, 413, 413, 413, 414, 414, 414, 414, 415, 415, 415, 415, 416, 416, 416, 416, 417, 417, 417, 417, 418, 418, 418, 418, 419, 419, 419, 419, 419, 419, 420, 420, 420, 420, 421, 421, 422, 422, 422, 422, 423, 423, 423, 423, 424, 424, 424, 425, 425, 425, 425, 426, 426, 426, 426, 427, 427, 427, 427, 428, 428, 428, 429, 429, 429, 429, 429, 429, 430, 430, 430, 430, 431, 431, 432, 432, 432, 433, 433, 433, 433, 434, 434, 434, 435, 435, 435, 435, 436, 436, 436, 436, 437, 437, 437, 438, 438, 438, 438, 439, 439, 439, 439, 439, 440, 440, 440, 441, 441, 442, 442, 442, 443, 443, 443, 443, 444, 444, 444, 445, 445, 445, 446, 446, 446, 446, 447, 447, 447, 448, 448, 448, 449, 449, 449, 449, 449, 450, 450, 450, 451, 451, 452, 452, 452, 453, 453, 453, 454, 454, 454, 455, 455, 455, 456, 456, 456, 457, 457, 457, 458, 458, 458, 459, 459, 459, 459, 460, 460, 460, 461, 461, 462, 462, 462, 463, 463, 463, 464, 464, 465, 465, 465, 466, 466, 466, 467, 467, 467, 468, 468, 469, 469, 469, 469, 470, 470, 470, 471, 472, 472, 472, 473, 473, 474, 474, 474, 475, 475, 476, 476, 476, 477, 477, 478, 478, 478, 479, 479, 479, DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_XXHIGH, 481, 482, 482, 483, 483, 484, 484, 484, 485, 485, 486, 486, 487, 487, 488, 488, 488, Downloads.Impl.STATUS_CANNOT_RESUME, Downloads.Impl.STATUS_CANNOT_RESUME, Downloads.Impl.STATUS_CANNOT_RESUME, Downloads.Impl.STATUS_CANCELED, Downloads.Impl.STATUS_CANCELED, Downloads.Impl.STATUS_UNKNOWN_ERROR, Downloads.Impl.STATUS_FILE_ERROR, Downloads.Impl.STATUS_FILE_ERROR, Downloads.Impl.STATUS_UNHANDLED_REDIRECT, Downloads.Impl.STATUS_UNHANDLED_REDIRECT, Downloads.Impl.STATUS_UNHANDLED_HTTP_CODE, Downloads.Impl.STATUS_UNHANDLED_HTTP_CODE, Downloads.Impl.STATUS_HTTP_DATA_ERROR, Downloads.Impl.STATUS_HTTP_DATA_ERROR, Downloads.Impl.STATUS_HTTP_EXCEPTION, Downloads.Impl.STATUS_HTTP_EXCEPTION, Downloads.Impl.STATUS_TOO_MANY_REDIRECTS, Downloads.Impl.STATUS_TOO_MANY_REDIRECTS, Downloads.Impl.STATUS_BLOCKED, Downloads.Impl.STATUS_BLOCKED, 499, 499, 499, 500, 501, ImsReasonInfo.CODE_USER_NOANSWER, ImsReasonInfo.CODE_USER_NOANSWER, ImsReasonInfo.CODE_USER_IGNORE, ImsReasonInfo.CODE_USER_IGNORE, ImsReasonInfo.CODE_USER_DECLINE, ImsReasonInfo.CODE_USER_DECLINE, 505, 505, 506, 507, 507, 508, 508, 509, 509, ImsReasonInfo.CODE_USER_TERMINATED_BY_REMOTE, ImsReasonInfo.CODE_USER_TERMINATED_BY_REMOTE, 511, 512, 513, 513, 514, 515, 515, 516, 517, 517, 518, 519, 519, 519, BluetoothClass.Device.PHONE_CORDLESS, 521, 522, 523, BluetoothClass.Device.PHONE_SMART, BluetoothClass.Device.PHONE_SMART, 525, 526, 526, 527, BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY, 529, 529, 530, 531, BluetoothClass.Device.PHONE_ISDN, 533, 534, 535, 535, 536, 537, 538, 539, 539, 540, 542, 543, 544, 545, 546, 547, 548, 549, 549, 550, 552, 553, 554, 555, 556, 558, 559, 559, 561, 562, 564, 565, 566, 568, 569, 570, 572, 574, 575, 577, 578, 579, 582, 583, 585, 587, 589, 590, 593, 595, 597, 599, 602, 604, 607, 609, 612, 615, 618, 620, 624, 628, 631, 635, 639, 644, 649, 654, 659, 666, 673, 680, 690, 700, 714};
    private final String mAutoFixShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float scale;\nuniform float shift_scale;\nuniform float hist_offset;\nuniform float hist_scale;\nuniform float density_offset;\nuniform float density_scale;\nvarying vec2 v_texcoord;\nvoid main() {\n  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = dot(color.rgb, weights);\n  float mask_value = energy - 0.5;\n  float alpha;\n  if (mask_value > 0.0) {\n    alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;\n  } else { \n    alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;\n  }\n  float index = energy * hist_scale + hist_offset;\n  vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));\n  float value = temp.g + temp.r * shift_scale;\n  index = value * density_scale + density_offset;\n  temp = texture2D(tex_sampler_2, vec2(index, 0.5));\n  value = temp.g + temp.r * shift_scale;\n  float dst_energy = energy * alpha + value * (1.0 - alpha);\n  float max_energy = energy / max(color.r, max(color.g, color.b));\n  if (dst_energy > max_energy) {\n    dst_energy = max_energy;\n  }\n  if (energy == 0.0) {\n    gl_FragColor = color;\n  } else {\n    gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);\n  }\n}\n";
    private Frame mDensityFrame;
    private int mHeight = 0;
    private Frame mHistFrame;
    private Program mNativeProgram;
    @GenerateFieldPort(name = BatteryManager.EXTRA_SCALE)
    private float mScale;
    private Program mShaderProgram;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = DisplayMetrics.DENSITY_XXXHIGH;
    private int mWidth = 0;

    public AutoFixFilter(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(3));
        addOutputBasedOnInput("image", "image");
    }

    @Override // android.filterfw.core.Filter
    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void initProgram(FilterContext context, int target) {
        switch (target) {
            case 3:
                ShaderProgram shaderProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float scale;\nuniform float shift_scale;\nuniform float hist_offset;\nuniform float hist_scale;\nuniform float density_offset;\nuniform float density_scale;\nvarying vec2 v_texcoord;\nvoid main() {\n  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = dot(color.rgb, weights);\n  float mask_value = energy - 0.5;\n  float alpha;\n  if (mask_value > 0.0) {\n    alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;\n  } else { \n    alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;\n  }\n  float index = energy * hist_scale + hist_offset;\n  vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));\n  float value = temp.g + temp.r * shift_scale;\n  index = value * density_scale + density_offset;\n  temp = texture2D(tex_sampler_2, vec2(index, 0.5));\n  value = temp.g + temp.r * shift_scale;\n  float dst_energy = energy * alpha + value * (1.0 - alpha);\n  float max_energy = energy / max(color.r, max(color.g, color.b));\n  if (dst_energy > max_energy) {\n    dst_energy = max_energy;\n  }\n  if (energy == 0.0) {\n    gl_FragColor = color;\n  } else {\n    gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);\n  }\n}\n");
                shaderProgram.setMaximumTileSize(this.mTileSize);
                this.mShaderProgram = shaderProgram;
                this.mTarget = target;
                return;
            default:
                throw new RuntimeException("Filter Sharpen does not support frames of target " + target + "!");
        }
    }

    private void initParameters() {
        this.mShaderProgram.setHostValue("shift_scale", Float.valueOf(0.00390625f));
        this.mShaderProgram.setHostValue("hist_offset", Float.valueOf(6.527415E-4f));
        this.mShaderProgram.setHostValue("hist_scale", Float.valueOf(0.99869454f));
        this.mShaderProgram.setHostValue("density_offset", Float.valueOf(4.8828125E-4f));
        this.mShaderProgram.setHostValue("density_scale", Float.valueOf(0.99902344f));
        this.mShaderProgram.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(this.mScale));
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Filter
    public void prepare(FilterContext context) {
        int[] densityTable = new int[1024];
        for (int i = 0; i < 1024; i++) {
            densityTable[i] = (int) ((((long) normal_cdf[i]) * 65535) / ((long) 766));
        }
        this.mDensityFrame = context.getFrameManager().newFrame(ImageFormat.create(1024, 1, 3, 3));
        this.mDensityFrame.setInts(densityTable);
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext context) {
        if (this.mDensityFrame != null) {
            this.mDensityFrame.release();
            this.mDensityFrame = null;
        }
        if (this.mHistFrame != null) {
            this.mHistFrame.release();
            this.mHistFrame = null;
        }
    }

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mShaderProgram != null) {
            this.mShaderProgram.setHostValue(BatteryManager.EXTRA_SCALE, Float.valueOf(this.mScale));
        }
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        if (this.mShaderProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
            initParameters();
        }
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
            createHistogramFrame(context, this.mWidth, this.mHeight, input.getInts());
        }
        Frame output = context.getFrameManager().newFrame(inputFormat);
        this.mShaderProgram.process(new Frame[]{input, this.mHistFrame, this.mDensityFrame}, output);
        pushOutput("image", output);
        output.release();
    }

    private void createHistogramFrame(FilterContext context, int width, int height, int[] data) {
        int[] histArray = new int[766];
        int y_border_thickness = (int) (((float) height) * 0.05f);
        int x_border_thickness = (int) (((float) width) * 0.05f);
        int pixels = (width - (x_border_thickness * 2)) * (height - (y_border_thickness * 2));
        for (int y = y_border_thickness; y < height - y_border_thickness; y++) {
            for (int x = x_border_thickness; x < width - x_border_thickness; x++) {
                int index = (y * width) + x;
                int energy = (data[index] & 255) + ((data[index] >> 8) & 255) + ((data[index] >> 16) & 255);
                histArray[energy] = histArray[energy] + 1;
            }
        }
        for (int i = 1; i < 766; i++) {
            histArray[i] = histArray[i] + histArray[i - 1];
        }
        for (int i2 = 0; i2 < 766; i2++) {
            histArray[i2] = (int) ((65535 * ((long) histArray[i2])) / ((long) pixels));
        }
        FrameFormat shaderHistFormat = ImageFormat.create(766, 1, 3, 3);
        if (this.mHistFrame != null) {
            this.mHistFrame.release();
        }
        this.mHistFrame = context.getFrameManager().newFrame(shaderHistFormat);
        this.mHistFrame.setInts(histArray);
    }
}
