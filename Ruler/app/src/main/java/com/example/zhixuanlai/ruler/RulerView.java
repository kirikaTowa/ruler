package com.example.zhixuanlai.ruler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.Iterator;

/**
 * Screen size independent ruler view.
 */
public class RulerView extends View {

    private Unit unit;

    private DisplayMetrics dm;

    private Paint scalePaint;//刻度线
    private Paint backgroundPaint;

    private float guideScaleTextSize;
    private float graduatedScaleWidth;
    private float graduatedScaleBaseLength;
    private int scaleColor;

    private String textUnit="cm";
    private int backgroundColor;


    private Bitmap bitmap;
    private int resPointer;
    private Paint mPaint;

    private static final float MOVE_DISTANCE = 100;
    int moveType = 0;

    PointF topPointer = null;
    PointF bottomPointer = null;
    int paddingTop=0;

    private MoveDistanceCallBack mMoveDistanceCallBack;

    /**
     * Creates a new RulerView.
     */
    public RulerView(Context context) {
        this(context, null);
        init(context, null, 0);
    }

    public RulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs, 0);
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, 0);
    }

    private void init(Context context, AttributeSet attrs, int s) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RulerView);

        guideScaleTextSize = typedArray.getDimension(R.styleable.RulerView_guideScaleTextSize, 40);
        graduatedScaleWidth = typedArray.getDimension(R.styleable.RulerView_graduatedScaleWidth, 8);
        graduatedScaleBaseLength =
                typedArray.getDimension(R.styleable.RulerView_graduatedScaleBaseLength, 100);
        scaleColor = typedArray.getColor(R.styleable.RulerView_scaleColor, 0xFFFACC31);
        String targetText=typedArray.getString(R.styleable.RulerView_scaleUnit);
       if (targetText!=null){
           textUnit=targetText;
       }

        backgroundColor = typedArray.getColor(R.styleable.RulerView_backgroundColor, 0xFFFACC31);
        resPointer = typedArray.getResourceId(R.styleable.RulerView_resPointer, R.drawable.pointer_icon);

        dm = getResources().getDisplayMetrics();
        unit = new Unit(dm.ydpi);
        unit.setType(typedArray.getInt(R.styleable.RulerView_unit, RulerView.Unit.CM));

        typedArray.recycle();


        initRulerView();
    }

    public void setUnitType(int type) {
        unit.type = type;
        invalidate();
    }

    public int getUnitType() {
        return unit.type;
    }

    private void initRulerView() {

        scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scalePaint.setStrokeWidth(graduatedScaleWidth);
        scalePaint.setTextSize(guideScaleTextSize);
        scalePaint.setColor(scaleColor);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        topPointer = new PointF(getWidth(), 70);
        bottomPointer = new PointF(getWidth(), 250);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                computerMoveDirection(new PointF(event.getX(), event.getY()));
            case MotionEvent.ACTION_MOVE:
                PointF pointF = new PointF(event.getX(), event.getY());
                computerAngle(pointF);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                break;
        }


        return true;
    }

    private void computerAngle(PointF pointF) {
        if (pointF.y<paddingTop){
            pointF.y=paddingTop;
        }

        if (moveType == 1) {
            topPointer.y = pointF.y;
            invalidate();
        } else if (moveType == 2) {
            bottomPointer.y = pointF.y;
            invalidate();
        }
    }

    private void computerMoveDirection(PointF downPoint) {
        moveType = 0;

        double distanceToLine1 = pointToLine(topPointer, downPoint);
        double distanceToLine2 = pointToLine(bottomPointer, downPoint);

        if (distanceToLine1 < MOVE_DISTANCE) {
            moveType = 1;
        }
        if (distanceToLine1 > distanceToLine2 && distanceToLine2 < MOVE_DISTANCE) {
            moveType = 2;
        }

    }

    private double pointToLine(PointF endPoint, PointF downPoint) {
        double space = 0;
        space = Math.abs(downPoint.y - endPoint.y);
        return space;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setStrokeWidth(5);
            mPaint.setColor(Color.RED);
            mPaint.setAntiAlias(true);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeResource(getResources(), resPointer, options);
            Matrix matrix = new Matrix();
            matrix.postRotate(-90, bitmap.getWidth(), bitmap.getHeight());
            Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            bitmap = rotateBitmap;
        }

        int width = getWidth();
        int height = getHeight();
        paddingTop = getPaddingTop();

        // Draw background.
        canvas.drawPaint(backgroundPaint);

        // Draw scale.
        Iterator<Unit.Graduation> pixelsIterator = unit.getPixelIterator(height - paddingTop);
        while (pixelsIterator.hasNext()) {
            Unit.Graduation graduation = pixelsIterator.next();

            float startX = width - graduation.relativeLength * graduatedScaleBaseLength;
            float startY = paddingTop + graduation.pixelOffset;
            float endX = width;
            float endY = startY;
            canvas.drawLine(startX, startY, endX, endY, scalePaint);

            if (graduation.value % 1 == 0) {
                String text = (int) graduation.value + textUnit;

                canvas.save();
                canvas.translate(
                        startX - guideScaleTextSize, startY - scalePaint.measureText(text) / 2);
                canvas.rotate(90);
                canvas.drawText(text, 0, 0, scalePaint);
                canvas.restore();
            }
        }

        if (topPointer != null) {
            Matrix matrix = new Matrix();
            int offsetX = bitmap.getWidth();
            int offsetY = bitmap.getHeight();
            matrix.preTranslate(width - offsetX, topPointer.y - offsetY / 2f);
            canvas.drawBitmap(bitmap, matrix, mPaint);

            Matrix matrixR = new Matrix();
            matrixR.preTranslate(width - offsetX, bottomPointer.y - offsetY / 2f);
            canvas.drawBitmap(bitmap, matrixR, mPaint);

        }

        // Draw Text label.
        String labelText = "1cm";
        if (topPointer != null) {
            float distanceInPixels = Math.abs(topPointer.y - bottomPointer.y);
            labelText = unit.getStringRepresentationPure(distanceInPixels / unit.getPixelsPerUnit());
        }
        mMoveDistanceCallBack.distanceCallBack(labelText);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return 200;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return 200;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int width = Math.max(minWidth, MeasureSpec.getSize(widthMeasureSpec));

        int minHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int height = Math.max(minHeight, MeasureSpec.getSize(heightMeasureSpec));

        setMeasuredDimension(width, height);
    }

    public class Unit {

        class Graduation {
            float value;
            int pixelOffset;
            float relativeLength;
        }

        public static final int INCH = 0;
        public static final int CM = 1;

        private int type = CM;
        private float dpi;

        Unit(float dpi) {
            this.dpi = dpi;
        }

        public void setType(int type) {
            if (type == INCH || type == CM) {
                this.type = type;
            }
        }

        public String getStringRepresentation(float value) {
            String suffix = "";
            if (type == INCH) {
                suffix = value > 1 ? "Inches" : "Inch";
            } else if (type == CM) {
                suffix = "CM";
            }
            return String.format("%.3f %s", value, suffix);
        }

        public String getStringRepresentationPure(float value) {
            return String.format("%.3f", value);
        }

        public Iterator<Graduation> getPixelIterator(final int numberOfPixels) {
            return new Iterator<Graduation>() {
                int graduationIndex = 0;
                Graduation graduation = new Graduation();

                private float getValue() {
                    return graduationIndex * getPrecision();
                }

                private int getPixels() {
                    return (int) (getValue() * getPixelsPerUnit());
                }

                @Override
                public boolean hasNext() {
                    return getPixels() <= numberOfPixels;
                }

                @Override
                public Graduation next() {
                    // Returns the same Graduation object to avoid allocation.
                    graduation.value = getValue();
                    graduation.pixelOffset = getPixels();
                    graduation.relativeLength = getGraduatedScaleRelativeLength(graduationIndex);

                    graduationIndex++;
                    return graduation;
                }

                @Override
                public void remove() {

                }
            };
        }

        public float getPixelsPerUnit() {
            if (type == INCH) {
                return dpi;
            } else if (type == CM) {
                return dpi / 2.54f;
            }
            return 0;
        }

        private float getPrecision() {
            if (type == INCH) {
                return 1 / 4f;
            } else if (type == CM) {
                return 1 / 10f;
            }
            return 0;
        }

        private float getGraduatedScaleRelativeLength(int graduationIndex) {
            if (type == INCH) {
                if (graduationIndex % 4 == 0) {
                    return 1f;
                } else if (graduationIndex % 2 == 0) {
                    return 3 / 4f;
                } else {
                    return 1 / 2f;
                }
            } else if (type == CM) {
                if (graduationIndex % 10 == 0) {
                    return 1;
                } else if (graduationIndex % 5 == 0) {
                    return 3 / 4f;
                } else {
                    return 1 / 2f;
                }
            }
            return 0;
        }

    }


    public void setMoveDistanceCallBack(MoveDistanceCallBack callBack) {
        mMoveDistanceCallBack = callBack;
    }

    public interface MoveDistanceCallBack {
        public void distanceCallBack(String distance);
    }
}
